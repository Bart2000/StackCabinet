#include <SCCP.h>

// SCCP handler commands
sccp_command_t commands[] = {
    {&SCCP::agat, 0},
    {&SCCP::dgat, 0},
    {&SCCP::icab, 100}
};

/**
 * Default constructor for SCCP class. Initializes properties to prevent inderterminacy. 
 */ 
SCCP::SCCP() 
{
    *buffer = {0};
    buffer_length = 0;
    id = 0;
    cab_type = SQUARE;
}

/**
 * Method to initialize USART and set baudrate with error correction.
 */ 
void SCCP::init() 
{
    PORTB.PIN2CTRL = PORT_PULLUPEN_bm;                              // Configure pullup for TX
    USART0.CTRLA = USART_LBME_bm | USART_RXCIE_bm;                  // Connect RX and TX with Loop-Back Mode, enable RX receive interrupt
    USART0.CTRLB = USART_ODME_bm | USART_TXEN_bm | USART_RXEN_bm;   // Enable Open-Drain mode, enable TX/RX
    USART0.CTRLC = USART_CHSIZE_8BIT_gc | USART_SBMODE_1BIT_gc;     // Configure USART in 8-bit, 1 stop bit mode

    int8_t  sigrow_value = SIGROW.OSC20ERR3V;                       // Read signed error
	int32_t baud = (F_CPU * 64) / (BAUDRATE * 16);                  // Calculate ideal baud rate
	baud *= (1024 + sigrow_value);                                  // Sum resolution + error
	baud /= 1024;                                                   // Divide by resolution
	USART0.BAUD = (int16_t)baud;                                    // Set adjusted baudrate
}

/**
 * Method to send an SCCP packet. USART receive interrupt is disabled during operation to prevent the handling of sent commands.
 * @param packet The SCCP packet to be sent, represented by the sccp_packet_struct. 
 */
void SCCP::send(sccp_packet_t packet) 
{
    // Calcualte packet length
    uint8_t packet_lenght = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_lenght];

    // Encode packet
    encode(data, &packet);

    // Reset TX and disable USART receive interrupt
    reset_tx();
    disable_rx();

    // Send data
    for(uint8_t i = 0; i < packet_lenght; i++) 
    {
        while(!SCCP::tx_ready());
        USART0.TXDATAL = data[i];
    }
    // Wait until TX is available again
    while(!(USART0.STATUS & USART_TXCIF_bm));
    enable_rx();
}

/**
 * Method to load a byte into the SCCP buffer. When a command has been fully sent, the buffer is cleared.
 * @param byte The byte to be loaded into the SCCP buffer.
 */ 
void SCCP::receive_byte(uint8_t byte) 
{
    // Load byte into buffer and icrement length
    this->buffer[this->buffer_length++] = byte;

    // Check if command has been fully sent
    if(this->buffer_length == HEADER_SIZE + (this->buffer[1] & DATA_LEN_MASK)) 
    {
        // Execute command
        handle_command();
        
        // Clear buffer
        memset(buffer, 0, sizeof(buffer));
        buffer_length = 0;
    }
}

/**
 * Method to activate the given gate (AGAT). 
 * @param data The data from the sccp_packet_t. This will contain the gate to be activated.
 */ 
void SCCP::agat(uint8_t* data) 
{
    // The gate to be activated
    uint8_t gate = data[0];

    // Check if gate is out of bounds
    if(gate >= 4) return;
    
    // Pull gate low
    PORTC.DIRSET |= 1 << gate;
    PORTC.OUT |= 1 << gate;
}

/**
 * Method to deactivate the given gate (DGAT).
 * @param data The data from the sccp_packet_t. This will contain the gate to be deactivated.
 */ 
void SCCP::dgat(uint8_t* data) 
{
    // The gate to be deactivated
    uint8_t gate = data[0];

    // Check if gate is out of bounds
    if(gate >= 4) return;

    // Set gate as input again
    PORTC.DIRCLR |= 1 << gate;
}

/**
 * Method to identify the cabinet (ICAB). This succeeds if a gate is activated and an id has not been assigned yet.
 * @param The data from the sccp_packet_t. This will contain the assigned id.
 */ 
void SCCP::icab(uint8_t* data) 
{
    // Get activated gates
    uint8_t gates = PORTC.IN & GATES;

    // Check if gate is activated and no gate is configured as output
    if(!gates || PORTC.OUT & GATES) return;

    // Check if id has not been assigned yet
    if(!id) 
    {
        id = data[0];
        uint8_t gate = log(gates) / log(2);
        uint8_t data[] = {id, gate, cab_type};
        send(sccp_packet_t(255, IACK, sizeof(data), data));
    }
    else 
    {
        uint8_t data[] = {id};
        send(sccp_packet_t(255, INACK, sizeof(data), data));
    }
}

/**
 * Method to handle the loaded command in the SCCP buffer.
 */ 
void SCCP::handle_command() 
{
    // Decode buffer into SCCP packet
    sccp_packet_t packet;
    decode(buffer, &packet);

    // Not intended for this cabinet
    if(packet.cab_id != id && packet.cab_id != 0) return;

    // Command does not exist
    if(packet.cmd_id > sizeof(commands) / sizeof(commands[0])) return;

    // Exectute command handler
    (this->*commands[packet.cmd_id].handler)(packet.data);
}

/**
 * Method to encode an sccp_packet_t to raw data. 
 * @param data Pointer to data buffer.
 * @param packet Pointer to packet to be encoded.
 */ 
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

/**
 * Method to decode raw data to an sccp_packet_t. 
 * @param data Pointer to data buffer.
 * @param packet Pointer to packet to be encoded.
 */ 
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

/**
 * Method to enable the USART RX.
 */ 
void SCCP::enable_rx() 
{
    USART0.CTRLB |= USART_RXEN_bm;
}

/**
 * Method to disable the RXC interrupt.
 */ 
void SCCP::disable_rx() 
{
    USART0.CTRLB &= ~USART_RXCIF_bm;
}

/**
 * Method to reset the USART TX.
 */ 
void SCCP::reset_tx()
{
	USART0.STATUS = USART_TXCIF_bm;
}

/**
 * Method to see if TX buffer is empty and ready to send new data.
 */ 
uint8_t SCCP::tx_ready() 
{
    return USART0.STATUS & USART_DREIF_bm;
}

/**
 * Temporary method to blink a LED for debugging purposes.
 */ 
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