#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <avr/io.h>
#include <stdlib.h>
#include <avr/delay.h>
#include <string.h>

#define HEADER_SIZE 2
#define DATA_SIZE 16
#define GATE_PORT PORTC
#define GATES (PIN0_bm | PIN1_bm | PIN2_bm | PIN3_bm)

#define F_CPU 3333333
#define USART0_BAUD_RATE(BAUD_RATE) ((float)(F_CPU * 64 / (16 * (float)BAUD_RATE)) + 0.5)

class SCCP;

enum Commands {
    AGAT,
    ICAB,
    IACK,
    INACK,
    OCAB,
    SLED,
    ACK,
    NACK
};

typedef void (SCCP::*MP)(uint8_t*); 

typedef struct sccp_packet 
{
    uint8_t cab_id;
    uint8_t cmd_id;
    uint8_t data_len;
    uint8_t data[DATA_SIZE];

    sccp_packet() {};

    sccp_packet(uint8_t cab_id, uint8_t cmd_id, uint8_t data_len, uint8_t* data) 
    {
        this->cab_id = cab_id;
        this->cmd_id = cmd_id;
        this->data_len = data_len; 
        memcpy(this->data, data, data_len);
    }
} sccp_packet_t;

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
        void handle_command(uint8_t* raw);
        void agat(uint8_t* data);
        void icab(uint8_t* data);

    private:
        uint8_t id;
        void encode(uint8_t* data, sccp_packet_t* packet);
        void decode(uint8_t* data, sccp_packet_t* packet);
        void tmp_led(uint8_t n);
        uint8_t tx_ready();
};

#endif