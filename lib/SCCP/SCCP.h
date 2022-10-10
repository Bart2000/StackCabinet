#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <avr/io.h>
#include <stdlib.h>
#include <avr/delay.h>
#include <string.h>

#define HEADER_SIZE 2
#define DATA_SIZE 16
#define CMD_ID_SHIFT 4
#define DATA_LEN_MASK 0x0F
#define GATE_PORT PORTC
#define GATES (PIN0_bm | PIN1_bm | PIN2_bm | PIN3_bm)
#define BASE_ID 255

#define F_CPU 3333333
#define BAUDRATE 115200
#define USART0_BAUD_RATE(BAUD_RATE) ((float)(F_CPU * 64 / (16 * (float)BAUD_RATE)) + 0.5)

class SCCP;

// SCCP commands
enum Commands {
    AGAT,
    DGAT,
    ICAB,
    IACK,
    INACK,
    OCAB,
    SLED,
    ACK,
    NACK
};

// Type of the cabinet
enum Type {
    SQUARE,
    RECTANGLE,
    BIGSQUARE
};

// Type definition for class method pointer
typedef void (SCCP::*MP)(uint8_t*); 

// Typedef struct for SCCP packets
typedef struct sccp_packet 
{
    uint8_t cab_id;
    uint8_t cmd_id;
    uint8_t data_len;
    uint8_t data[DATA_SIZE];

    sccp_packet() 
    {
        *data = {0};
    };

    sccp_packet(uint8_t cab_id, uint8_t cmd_id, uint8_t data_len, uint8_t* data) 
    {
        this->cab_id = cab_id;
        this->cmd_id = cmd_id;
        this->data_len = data_len; 
        memcpy(this->data, data, data_len);
    }
} sccp_packet_t;

// Typedef struct for SCCP handler commands
typedef struct sccp_command 
{
    MP handler;
    uint32_t timeout;
} sccp_command_t;

class SCCP 
{
    public: 
        SCCP();
        void init();
        void send(sccp_packet_t packet);
        void receive_byte(uint8_t byte);
        void agat(uint8_t* data);
        void dgat(uint8_t* data);
        void icab(uint8_t* data);

    private:
        uint8_t buffer[HEADER_SIZE + DATA_SIZE];
        uint8_t buffer_length;
        uint8_t id;
        Type cab_type;
        void handle_command();
        void encode(uint8_t* data, sccp_packet_t* packet);
        void decode(uint8_t* data, sccp_packet_t* packet);
        void enable_rx();
        void disable_rx();
        void reset_tx();
        uint8_t tx_ready();
        void tmp_led(uint8_t n);
};

#endif