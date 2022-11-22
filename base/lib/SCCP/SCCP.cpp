#include "SCCP.h"
#include "stdio.h"

SCCP::SCCP() 
{
    
}

void SCCP::identify() 
{
    // Identify first cabinet
    gpio_set_level(GPIO_NUM_12, 0);
    uint8_t data[] = {1};
    send(sccp_packet_t(BROADCAST_ID, ICAB, sizeof(data), data));


}

void SCCP::send(sccp_packet_t packet) 
{
    // Calculate packet length
    uint8_t packet_length = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_length] = {0};

    encode(data, &packet);

    uart_write_bytes(UART_NUM_1, (const  char*)data, sizeof(data));
    vTaskDelay(100);
    
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