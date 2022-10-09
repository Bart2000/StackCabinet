#include <SCCP.h>

sccp_command_t commands[] = {
    {&SCCP::agat, 0},
    {&SCCP::dgat, 0},
    {&SCCP::icab, 100}
};

SCCP::SCCP() 
{
    id = 0;
    cab_type = SQUARE;
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
    uint8_t gate = data[0];

    // Check if gate is out of bounds
    if(gate >= 4) return;
    
    // Pull gate low
    PORTC.DIRSET |= 1 << gate;
    PORTC.OUT |= 1 << gate;
}

void SCCP::dgat(uint8_t* data) 
{
    uint8_t gate = data[0];

    // Check if gate is out of bounds
    if(gate >= 4) return;

    // Set gate as input again
    PORTC.DIRCLR |= 1 << gate;
    //PORTC.PIN3CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
}

void SCCP::icab(uint8_t* data) 
{
    uint8_t gates = PORTC.IN & GATES;

    // Check if gate is activated and no gate is configured as output
    if(!gates || PORTC.OUT & GATES) return;

    if(!id) 
    {
        id = data[0];
        uint8_t gate = log(gates) / log(2);
        uint8_t data[] = {id, gate, cab_type};
        send(sccp_packet_t(255, IACK, sizeof(data), data));
        tmp_led(1);
    }
    else 
    {
        uint8_t data[] = {id};
        send(sccp_packet_t(255, INACK, sizeof(data), data));
    }
}

void SCCP::handle_command(uint8_t* raw) 
{
    sccp_packet_t packet;
    decode(raw, &packet);

    // Not intended for this cabinet
    if(packet.cab_id != id && packet.cab_id != 0) return;

    // Command does not exist
    if(packet.cmd_id > sizeof(commands) / sizeof(commands[0])) return;

    // Exectute command handler
    (this->*commands[packet.cmd_id].handler)(packet.data);
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
    packet->cmd_id = data[1] >> 4;
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

void SCCP::tmp_led(uint8_t n) 
{
    for(uint8_t i = 0; i < n; i++)
    {
        PORTA.OUT &= ~PIN7_bm;
        _delay_ms(100);
        PORTA.OUT |= PIN7_bm;
        _delay_ms(100);
    }
}