#include <SCCP.h>

// SCCP handler commands
sccp_command_t commands[] = {
    {&SCCP::agat, 0},
    {&SCCP::dgat, 0},
    {&SCCP::icab, 100},
    {&SCCP::iack, 0},
    {&SCCP::inack, 0},
    {&SCCP::ocab, 0},
    {&SCCP::sled, 100},
    {&SCCP::sprod, 0}
};

uint8_t gate_map[] = {GATE0, GATE1, GATE2, GATE3};

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

    this->product_id = nvm.read_byte(PRODUCT_ADDR);
    tmp_led(this->product_id);
}

/**
 * Method to send an SCCP packet. USART receive interrupt is disabled during operation to prevent the handling of sent commands.
 * @param packet The SCCP packet to be sent, represented by the sccp_packet_struct. 
 */
void SCCP::send(sccp_packet_t packet) 
{
    // Calcualte packet length
    uint8_t packet_length = HEADER_SIZE + packet.data_len;
    uint8_t data[packet_length];

    // Encode packet
    encode(data, &packet);

    // Reset TX and disable USART receive interrupt
    reset_tx();
    disable_rx();

    //_delay_ms(5);

    // Send data
    for(uint8_t i = 0; i < packet_length; i++) 
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
void SCCP::agat(uint8_t* packet_data) 
{
    // The gate to be activated
    uint8_t gate = packet_data[0];

    // Check if gate is out of bounds

    if(gate >= 4) return;
    
    // Pull gate low
    PORTA.DIRSET = gate_map[gate];
    PORTA.OUT |= gate_map[gate];

    // Send ACK
    uint8_t data[] = {id, AGAT};
    send(sccp_packet_t(BASE_ID, ACK, sizeof(data), data));
}

/**
 * Method to deactivate the given gate (DGAT).
 * @param data The data from the sccp_packet_t. This will contain the gate to be deactivated.
 */ 
void SCCP::dgat(uint8_t* packet_data) 
{
    // The gate to be deactivated
    uint8_t gate = packet_data[0];

    // Check if gate is out of bounds
    if(gate >= 4) return;

    // Set gate as input again
    PORTA.DIRCLR |= gate_map[gate];

    // Send ACK
    uint8_t data[] = {id, DGAT};
    send(sccp_packet_t(BASE_ID, ACK, sizeof(data), data));
}

/**
 * Method to identify the cabinet (ICAB). This succeeds if a gate is activated and an id has not been assigned yet.
 * @param The data from the sccp_packet_t. This will contain the assigned id.
 */ 
void SCCP::icab(uint8_t* packet_data) 
{
    // Get activated gate states
    uint8_t gates = PORTA.IN & GATES;

    // Check if gate is activated and no gate is configured as output
    if(!gates || PORTA.OUT & GATES) return;
    
    // Get activated gate
    uint8_t gate = get_gate(gates);

    // Check if id has not been assigned yet
    if(!id) 
    {
        this->id = packet_data[0];
        uint8_t data[] = {this->id, gate, this->cab_type, this->product_id};
        send(sccp_packet_t(BASE_ID, IACK, sizeof(data), data));
    }
    else 
    {
        uint8_t data[] = {this->id, gate};
        send(sccp_packet_t(BASE_ID, INACK, sizeof(data), data));
    }
}

/**
 *  * Method for SCCP IACK. Currently, this method has no implementation. Its only use is to prevent offset issues in the command list.
 * @param The data from the sccp_packet_t. This will be empty.
 */ 
void SCCP::iack(uint8_t* packet_data) 
{
    // No implementation
}

/**
 * Method for SCCP INACK. Currently, this method has no implementation. Its only use is to prevent offset issues in the command list.
 * @param The data from the sccp_packet_t. This will be empty.
 */ 
void SCCP::inack(uint8_t* packet_data) 
{
    // No implementation
}

/**
 * Method to open cabinet (OCAB).
 * @param The data from the sccp_packet_t. This will be empty.
 */ 
void SCCP::ocab(uint8_t* packet_data) 
{
    // TODO : implement OCAB
}

/**
 * Method for SCCP ACK. Currently, this method has no implementation. Its only use is to prevent offset issues in the command list.
 * @param The data from the sccp_packet_t. This will contain the RGB values and the leds to be turned on.
 */ 
void SCCP::sled(uint8_t* packet_data) 
{
    // TODO : implement SLED with RGB
    tmp_led(1);
    uint8_t data[] = {id, SLED};
    send(sccp_packet_t(BASE_ID, ACK, sizeof(data), data));
}

/**
 * Method for SCCP SPROD. Sets the product id in Non Volatile Memory EEPROM. 
 * @param packet_data The data from the sccp_packet_t. This will contain the product id to set. 
 */
void SCCP::sprod(uint8_t* packet_data) 
{
    // Get product id from packet data
    uint8_t product_id = packet_data[0];
    this->product_id = product_id;
    uint8_t data[] = {id, SPROD};
    
    // Write to EEPROM
    if(nvm.write_byte(PRODUCT_ADDR_FLAG, 1) && nvm.write_byte(PRODUCT_ADDR, this->product_id))
    {
        send(sccp_packet_t(BASE_ID, ACK, sizeof(data), data));
    }
    else 
    {
        send(sccp_packet_t(BASE_ID, NACK, sizeof(data), data));
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
 * Method to derive the activated gate from agiven input
 */ 
uint8_t SCCP::get_gate(uint8_t input)  
{    
    for(uint8_t i = 0; i < 4; i++) 
    {
        if(gate_map[i] & input) return i;
    }
}

/**
 * Temporary method to blink a LED for debugging purposes.
 */ 
void SCCP::tmp_led(uint8_t n) 
{
    for(uint8_t i = 0; i < n; i++)
    {
        PORTB.OUT &= ~PIN5_bm;
        _delay_ms(100);
        PORTB.OUT |= PIN5_bm;
        _delay_ms(100);
    }
}