#include "SCCP.h"
#include "stdio.h"

uint8_t SCCP::buffer[HEADER_SIZE + DATA_SIZE] = {0};
QueueHandle_t SCCP::uart_queue;
uint8_t SCCP::buffer_length = 2;
TaskHandle_t SCCP::handle;

sccp_command_t commands[] = {
    {NULL, 0},
    {NULL, 0},
    {NULL, 100},
    {&SCCP::iack, 0},
    {&SCCP::inack, 0},
    {NULL, 0},
    {NULL, 100},
    {&SCCP::ack, 100},
};

#define BAUDRATE 115200
#define BUF_SIZE 2048
#define UART_NUM UART_NUM_1
#define TX_GPIO 10
#define RX_GPIO 9
#define TIMOUT_MS 20

SCCP::SCCP() 
{
    initialize();
    xTaskCreate(this->receive_loop, "UART_receive_loop", 2048, (void*)this, 1, &SCCP::handle);
    SCCP::control_flag = 0;
    this->id = 1;
    printf("Pointer task1: %p\n", this);
}

void SCCP::initialize() 
{
    // Config specific for GM67
    uart_config_t uart_config =
    {
        .baud_rate = BAUDRATE,              // Baudrate of GM67
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_APB,
    };

    // Set pins to UART 1
    uart_set_pin(UART_NUM, TX_GPIO, RX_GPIO, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    
    // Configure UART 1
    uart_param_config(UART_NUM, &uart_config);
    
    // Install UART drivers for UART 1
    uart_driver_install(UART_NUM, BUF_SIZE, BUF_SIZE, 20, &SCCP::uart_queue, 0);

    //uart_driver_install(UART_NUM_1, 256, 256, 0, NULL, 0);
    gpio_set_direction(GPIO_NUM_13, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_12, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_14, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_27, GPIO_MODE_INPUT);
}

void SCCP::identify() 
{
    std::vector<std::vector<uint8_t>> graph;
    graph.push_back({255});

    // Identify first cabinet
    gpio_set_level(GPIO_NUM_12, 0);
    uint8_t data[] = {this->id};
    send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data), data));

    sccp_packet_t response;
    int r = get_response(&response);

    if(r && response.cmd_id == IACK) 
    {
        uint8_t id = response.data[0];
        uint8_t gate = response.data[1];
        uint8_t type = response.data[2];

        uint8_t t[5] = {0,0,0,0,0};
        t[gate] = 255;
        std::vector<uint8_t> v(t, t + sizeof(t));
        graph.push_back(v);
        //printf("%d\n", response.data[1]);
        std::fill_n(this->buffer, sizeof(this->buffer), 0);
        this->id += 1;
    }
    else 
    {
        printf("stopped identification: %d\n", r);
        return;
    }

    gpio_set_level(GPIO_NUM_12, 1);

    uint8_t size = graph.size();

    for(uint8_t cab = 1; cab < size; ++cab) 
    {
        for(uint8_t gate = 0; gate < 4; gate++) 
        {
            printf("cab: %d\ngate: %d\n", cab, gate);
            int test = graph.at(cab).at(gate);

            if(test == 0) 
            {
                uint8_t data[] = {gate};
                send(sccp_packet_t(cab, AGAT, sizeof(data), data));

                sccp_packet_t response2;
                if(get_response(&response2) && response2.cmd_id == ACK) 
                {
                    uint8_t data2[] = {this->id};
                    send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data2), data2));
                    sccp_packet_t response3;

                    if(get_response(&response3) && response3.cmd_id == IACK) 
                    {
                        uint8_t t[5] = {0,0,0,0,0};
                        t[response3.data[1]] = cab;
                        std::vector<uint8_t> e(t, t + sizeof(t));
                        graph.push_back(e);
                        graph.at(cab).at(gate) = response3.data[0];
                        //printf("There is cab on gate %d\n", gate);
                        this->id++;
                        size++;
                    }

                    sccp_packet_t response4;
                    send(sccp_packet_t(cab, DGAT, sizeof(data), data));

                    if(get_response(&response4) && response4.cmd_id == ACK) 
                    {
                    }
                }
                else 
                {
                    //printf("Failed to open gate %d\n", gate);
                    return;
                }
                //printf("yes?: %d\n", r);
                // return;
            }
        }
    }

    printf("{");
    for(auto cab = graph.begin(); cab != graph.end(); ++cab) 
    {
        printf("{");
        std::vector<uint8_t> c = *cab;
        
        for(auto neighbour = c.begin(); neighbour != c.end(); ++neighbour) 
        {
            if(&*neighbour == &c.back()) printf("%d", *neighbour);
            else if(&*neighbour != &c.back() || *neighbour == 0) printf("%d, ", *neighbour);
            else printf("%d, ", *neighbour);
        }
        
        if(c != graph.back()) printf("}, ");
        else printf("}");
        
    }
    printf("}\n");

}

void SCCP::send(sccp_packet_t packet) 
{
    // Calculate packet length
    uint8_t packet_length = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_length] = {0};

    encode(data, &packet);

    uart_write_bytes(UART_NUM_1, (const  char*)data, sizeof(data));
}

void SCCP::encode(uint8_t* data, sccp_packet_t* packet) 
{
    // Set header
    data[0] = packet->cab_id;
    data[1] = (packet->cmd_id << 4) | packet->data_len;

    // Set data
    for(uint8_t i = 0; i < packet->data_len; i++) 
    {
        data[i + HEADER_SIZE] = *(packet->data + i);
    }
}

void SCCP::handle_command() 
{
    sccp_packet_t packet;
    decode(buffer, &packet);

    (this->*commands[packet.cmd_id].handler)(packet.data);
}

void SCCP::decode(uint8_t* data, sccp_packet_t* packet) 
{
    // Set header
    packet->cab_id = data[0];
    packet->cmd_id = data[1] >> CMD_ID_SHIFT;
    packet->data_len = data[1] & DATA_LEN_MASK;

    // Set data
    for(uint8_t i = 0; i < packet->data_len; i++) 
    {
        packet->data[i] = *(data + HEADER_SIZE + i);
    }
}

void SCCP::receive_loop(void* handle) 
{
    uart_event_t event;

    SCCP* sccp = (SCCP*)handle;

    while(1) 
    {
        if(xQueueReceive(SCCP::uart_queue, (void*)&event, (TickType_t)portMAX_DELAY)) 
        {
            switch(event.type) 
            {
                case UART_DATA:
                    uart_read_bytes(UART_NUM, sccp->buffer, event.size, portMAX_DELAY);

                    if((sccp->buffer[HEADER_SIZE-1] & DATA_LEN_MASK) + HEADER_SIZE == event.size)
                    {
                        sccp->handle_command();
                        uart_flush(UART_NUM);
                    }
                    break;
                default:
                    break;
            }
        }
        vTaskDelay(1);
    }

    vTaskDelete(NULL);
}

uint8_t SCCP::get_response(sccp_packet_t* response) 
{
    unsigned long time1 = esp_timer_get_time() / 1000ULL;

    while(!this->control_flag) 
    {
        unsigned long time2 = esp_timer_get_time() / 1000ULL;

        if(time2-time1 > 5) return 0;

        vTaskDelay(1);
    } 
    
    decode(this->buffer, response);

    this->control_flag = 0;
    return 1;
}

void SCCP::iack(uint8_t* data) 
{
    this->control_flag = 1;
}

void SCCP::inack(uint8_t* data) 
{
    this->control_flag = 1;
}

void SCCP::ack(uint8_t* data) 
{
    this->control_flag = 1;
}