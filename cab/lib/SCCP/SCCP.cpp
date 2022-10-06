#include <SCCP.h>

#define F_CPU 3333333

#define USART0_BAUD_RATE(BAUD_RATE) ((float)(F_CPU * 64 / (16 * (float)BAUD_RATE)) + 0.5)

typedef void (SCCP::*MP)(uint8_t*); 

MP commands[] = {
    &SCCP::agat,
    &SCCP::icab
};

SCCP::SCCP() 
{
    // Constructor
}

void SCCP::init() 
{
    PORTB.PIN2CTRL = PORT_PULLUPEN_bm;
    USART0.CTRLA = USART_LBME_bm;
    USART0.CTRLB = USART_ODME_bm | USART_TXEN_bm | USART_RXEN_bm;
    USART0.CTRLC = USART_CHSIZE_8BIT_gc | USART_SBMODE_1BIT_gc;
    USART0.BAUD = (uint16_t)USART0_BAUD_RATE(115200);
}

void SCCP::send(sccp_packet_t packet) 
{
    uint8_t packet_lenght = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_lenght];

    encode(data, &packet);

    for(uint8_t i = 0; i < packet_lenght; i++) 
    {
        while(!SCCP::tx_ready());
        USART0.TXDATAL = data[i];
    }
}

void SCCP::agat(uint8_t* data) 
{
    sccp_packet s = {
        .cab_id = 128,
        .cmd_id = 1,
        .data_len = 0
    };

    send(s);
}

void SCCP::icab(uint8_t* data) 
{
    sccp_packet s = {
        .cab_id = 128,
        .cmd_id = 15,
        .data_len = 0,
    };

    send(s);
}

void SCCP::handle_command(uint8_t* raw) 
{
    sccp_packet_t packet;
    decode(raw, &packet);

    (this->*commands[packet.cmd_id])(raw);
}

void SCCP::encode(uint8_t* data, sccp_packet_t* packet) 
{
    data[0] = packet->cab_id;
    data[1] = (packet->cmd_id << 4) | packet->data_len;

    for(uint8_t i = 0; i < packet->data_len; i++) 
    {
        data[i + HEADER_SIZE] = *(packet->data + i);
    }
}

void SCCP::decode(uint8_t* data, sccp_packet_t* packet) 
{
    packet->cab_id = data[0];
    packet->cmd_id = data[1] & 0xF0;
    packet->data_len = data[1] & 0x0F;

    for(uint8_t i = 0; i < packet->data_len; i++) 
    {
        packet->data[i] = *(data + HEADER_SIZE + i);
    }
}

uint8_t SCCP::tx_ready() 
{
    return USART0.STATUS & USART_DREIF_bm;
}