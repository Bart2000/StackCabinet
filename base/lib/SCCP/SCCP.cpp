#include "SCCP.h"
#include "stdio.h"

QueueHandle_t SCCP::uart_queue;
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

SCCP::SCCP() 
{
    initialize();
    xTaskCreate(this->receive_loop, "UART_receive_loop", 2048, (void*)this, 1, &SCCP::handle);
    this->buffer[HEADER_SIZE + DATA_SIZE] = {0};
    SCCP::control_flag = 0;
    this->id = 1;
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

uint8_t SCCP::identify() 
{
    sccp_packet_t response;
    uint8_t cab_obj[MEMBERS] = {0};
    this->graph.clear();
    this->graph.push_back({255});
    this->id = 1;

    // Identify first cabinet
    gpio_set_level(GPIO_NUM_12, 0);

    uint8_t data[] = {this->id};
    send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data), data));

    if(get_response(&response) && response.cmd_id == IACK) 
    {
        uint8_t id = response.data[0];
        uint8_t gate = response.data[1];

        cab_obj[gate] = BASE_ID;
        std::vector<uint8_t> cab_vector(cab_obj, cab_obj + sizeof(cab_obj));
        this->graph.push_back(cab_vector);
        std::fill_n(this->buffer, sizeof(this->buffer), 0);
        std::fill_n(cab_obj, MEMBERS, 0);
        this->id += 1;
    }
    else 
    {
        return 0;
    }

    gpio_set_level(GPIO_NUM_12, 1);

    uint8_t size = this->graph.size();

    for(uint8_t c = 1; c < size; ++c) 
    {
        for(uint8_t g = 0; g < 4; g++) 
        {
            uint8_t neighbour = this->graph.at(c).at(g);

            if(neighbour != 0) continue;

            uint8_t data[] = {g};
            send(sccp_packet_t(c, AGAT, sizeof(data), data));

            if(get_response(&response) && response.cmd_id == ACK) 
            {
                uint8_t data2[] = {this->id};
                send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data2), data2));

                if(get_response(&response) && response.cmd_id == IACK) 
                {
                    uint8_t id = response.data[0];
                    uint8_t gate = response.data[1];

                    cab_obj[gate] = c;
                    std::vector<uint8_t> cab_vector(cab_obj, cab_obj + sizeof(cab_obj));
                    this->graph.push_back(cab_vector);
                    this->graph.at(c).at(g) = id;
                    std::fill_n(cab_obj, MEMBERS, 0);
                    this->id++;
                    size++;
                }
                else if(response.cmd_id == INACK) 
                {
                    uint8_t id = response.data[0];
                    this->graph.at(c).at(g) = id;
                }

                send(sccp_packet_t(c, DGAT, sizeof(data), data));

                if(!get_response(&response) || response.cmd_id != ACK) return 0;
            }
            else 
            {
                //printf("Failed to open gate %d\n", gate);
                return 0;
            }
        }
    }

    printf("{");
    for(auto cab = this->graph.begin(); cab != this->graph.end(); ++cab) 
    {
        printf("{");
        std::vector<uint8_t> c = *cab;
        
        for(auto neighbour = c.begin(); neighbour != c.end(); ++neighbour) 
        {
            if(&*neighbour == &c.back()) printf("%d", *neighbour);
            else if(&*neighbour != &c.back() || *neighbour == 0) printf("%d, ", *neighbour);
            else printf("%d, ", *neighbour);
        }
        
        if(c != this->graph.back()) printf("}, ");
        else printf("}");
        
    }
    printf("}\n");

    

    for(auto it = this->graph.at(1).begin(); it != this->graph.at(1).end(); ++it) 
    {
        printf("%d\n", *it);
    }
    return 0;
    std::stringstream stream;
    size_t s = this->graph.size();

    for(size_t i = 0; i < s; i++) 
    {
        size_t s2 = this->graph.at(i).size(); 

        stream << "[";
        for(size_t j = 0; j < s2-1; j++) 
        {
            stream << this->graph.at(i).at(j);
        }

        stream << "]";
    }

    std::cout << stream.str() << std::endl;

    return 1;
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
    // Clear packet data
    std::fill_n(packet->data, DATA_SIZE, 0);

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

        if(time2-time1 > TIMEOUT_MS) return 0;

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