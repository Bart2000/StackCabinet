#include <SCCP.h>
#include <stdio.h>

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
    {NULL, 100},
    {&SCCP::ack, 100},
};

SCCP::SCCP()
{
    initialize();
    xTaskCreate(this->receive_loop, "UART_receive_loop", 2048, (void *)this, 1, &SCCP::handle);
    this->buffer[HEADER_SIZE + DATA_SIZE] = {0};
    SCCP::control_flag = 0;
    this->id = 1;
}

void SCCP::initialize()
{
    // Config specific for GM67
    uart_config_t uart_config =
        {
            .baud_rate = BAUDRATE, // Baudrate of GM67
            .data_bits = UART_DATA_8_BITS,
            .parity = UART_PARITY_DISABLE,
            .stop_bits = UART_STOP_BITS_1,
            .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
            .source_clk = UART_SCLK_APB,
        };

    // Configure UART
    uart_set_pin(UART_NUM, TX_GPIO, RX_GPIO, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    uart_param_config(UART_NUM, &uart_config);
    uart_driver_install(UART_NUM, BUF_SIZE, BUF_SIZE, 20, &SCCP::uart_queue, 0);    

    // Setup GPIO
    gpio_set_direction(GATE_GPIO, GPIO_MODE_OUTPUT);
    gpio_set_direction(RELAY_GPIO, GPIO_MODE_OUTPUT);
    gpio_set_direction(RESET_GPIO, GPIO_MODE_INPUT);
    gpio_pullup_en(RESET_GPIO);

    // Set initial GPIO levels
    gpio_set_level(RELAY_GPIO, 1);
    gpio_set_level(GATE_GPIO, 1);
}

uint8_t SCCP::identify()
{
    sccp_packet_t response;
    uint8_t cab_obj[MEMBERS] = {0};
    this->graph.clear();
    this->graph.push_back({255, 1});
    this->id = 1;

    // Power cycle cabinets
    gpio_set_level(RELAY_GPIO, 0);
    vTaskDelay(100);
    gpio_set_level(RELAY_GPIO, 1);
    vTaskDelay(100);

    // Pull gate low for first cabinet identification
    gpio_set_level(GATE_GPIO, 0);

    vTaskDelay(10);

    // Identify first cabinet with broadcasted ICAB message
    uint8_t data[] = {this->id};
    send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data), data));

    // If response is IACK
    if (get_response(&response) && response.cmd_id == IACK)
    {
        uint8_t id = response.data[0];
        uint8_t gate = response.data[1];
        uint8_t product_id = response.data[3];

        // Add cabinet to grid
        cab_obj[gate] = BASE_ID;
        cab_obj[4] = product_id;
        std::vector<uint8_t> cab_vector(cab_obj, cab_obj + sizeof(cab_obj));
        this->graph.push_back(cab_vector);

        // Flush buffers
        std::fill_n(this->buffer, sizeof(this->buffer), 0);
        std::fill_n(cab_obj, MEMBERS, 0);
        this->id += 1;
    }
    else
    {
        gpio_set_level(GATE_GPIO, 1);
        return 0;
    }

    // Pull gate high
    gpio_set_level(GATE_GPIO, 1);

    uint8_t size = this->graph.size();

    // Breadth First Search through grid
    for (uint8_t c = 1; c < size; ++c)
    {
        for (uint8_t g = 0; g < 4; g++)
        {
            uint8_t neighbour = this->graph.at(c).at(g);

            // Do not process 0's
            if (neighbour != 0)
                continue;

            // Activate gate with AGAT message
            uint8_t data[] = {g};
            send(sccp_packet_t(c, AGAT, sizeof(data), data));
            vTaskDelay(1);

            // Check if message is received and acknowledged
            if (get_response(&response) && response.cmd_id == ACK)
            {
                // Identify cabinet with broadcasted ICAB message
                uint8_t data2[] = {this->id};
                send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data2), data2));

                // Check if cabinet at gate responded with IACK
                if (get_response(&response) && response.cmd_id == IACK)
                {
                    uint8_t id = response.data[0];
                    uint8_t gate = response.data[1];
                    uint8_t product_id = response.data[3];

                    // Add cabinet to graph
                    cab_obj[gate] = c;
                    cab_obj[4] = product_id;
                    std::vector<uint8_t> cab_vector(cab_obj, cab_obj + sizeof(cab_obj));
                    this->graph.push_back(cab_vector);

                    // Update parent cabinet
                    this->graph.at(c).at(g) = id;
                    std::fill_n(cab_obj, MEMBERS, 0);
                    this->id++;
                    size++;
                }
                else if (response.cmd_id == INACK)
                {
                    // printf("asdasd\n");
                    uint8_t id = response.data[0];
                    uint8_t gate = response.data[1];

                    // Update graph accordingly if cabinet already identified
                    this->graph.at(c).at(g) = id;
                    this->graph.at(id).at(gate) = c;
                }

                // Deactivate gate
                send(sccp_packet_t(c, DGAT, sizeof(data), data));

                // Stop identification process if gate is not closed
                if (!get_response(&response) || response.cmd_id != ACK)
                    return 0;
            }
            else
            {
                // Stop identification if gate is not opened
                return 0;
            }
        }
    }

    return 1;
}

void SCCP::graph_to_json(string *result)
{
    std::stringstream stream;
    size_t s = this->graph.size();

    // Construct JSON string
    stream << "REQUEST_GRID|[";
    for (size_t i = 0; i < s; i++)
    {
        size_t s2 = this->graph.at(i).size();
        stream << "[";

        // Loop through neighbours
        for (size_t j = 0; j < s2; j++)
        {
            stream << (int)this->graph.at(i).at(j) << ", ";
        }
        stream.seekp(-2, std::ios_base::end);
        stream << "], ";
    }
    stream.seekp(-2, std::ios_base::end);
    stream << "]";

    std::cout << esp_get_free_heap_size() << std::endl;

    std::cout << stream.str() << std::endl;

    // Update string with result
    *result = stream.str();
}

void SCCP::send(sccp_packet_t packet)
{
    // Calculate packet length
    uint8_t packet_length = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_length] = {0};

    encode(data, &packet);
    uart_disable_rx_intr(UART_NUM);
    uart_write_bytes(UART_NUM_1, (const char *)data, sizeof(data));
    uart_enable_rx_intr(UART_NUM);
}

void SCCP::encode(uint8_t *data, sccp_packet_t *packet)
{
    // Set header
    data[0] = packet->cab_id;
    data[1] = (packet->cmd_id << 4) | packet->data_len;

    // Set data
    for (uint8_t i = 0; i < packet->data_len; i++)
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

void SCCP::decode(uint8_t *data, sccp_packet_t *packet)
{
    // Clear packet data
    std::fill_n(packet->data, DATA_SIZE, 0);

    // Set header
    packet->cab_id = data[0];
    packet->cmd_id = data[1] >> CMD_ID_SHIFT;
    packet->data_len = data[1] & DATA_LEN_MASK;

    // Set data
    for (uint8_t i = 0; i < packet->data_len; i++)
    {
        packet->data[i] = *(data + HEADER_SIZE + i);
    }
}

void SCCP::receive_loop(void *handle)
{
    uart_event_t event;

    SCCP *sccp = (SCCP *)handle;

    while (1)
    {
        if (xQueueReceive(SCCP::uart_queue, (void *)&event, (TickType_t)portMAX_DELAY))
        {
            switch (event.type)
            {
            case UART_DATA:
                printf("A\n");
                uart_read_bytes(UART_NUM, sccp->buffer, event.size, portMAX_DELAY);

                if ((sccp->buffer[HEADER_SIZE - 1] & DATA_LEN_MASK) + HEADER_SIZE == event.size)
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

uint8_t SCCP::get_response(sccp_packet_t *response)
{
    unsigned long time1 = esp_timer_get_time() / 1000ULL;

    while (!this->control_flag)
    {
        unsigned long time2 = esp_timer_get_time() / 1000ULL;

        if (time2 - time1 > TIMEOUT_MS)
            return 0;

        vTaskDelay(1);
    }

    decode(this->buffer, response);

    this->control_flag = 0;
    return 1;
}

void SCCP::sprod(uint8_t id, uint8_t product_id)
{
    uint8_t data[] = {product_id};
    send(sccp_packet_t(id, SPROD, sizeof(data), data));
}

void SCCP::iack(uint8_t *data)
{
    this->control_flag = 1;
}

void SCCP::inack(uint8_t *data)
{
    this->control_flag = 1;
}

void SCCP::ack(uint8_t *data)
{
    this->control_flag = 1;
}