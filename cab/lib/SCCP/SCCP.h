#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <avr/io.h>
#include <stdlib.h>

#define HEADER_SIZE 2
#define DATA_SIZE 16
#define GATE_PORT PORTC
#define GATE1 PIN0
#define GATE2 PIN1
#define GATE3 PIN2
#define GATE4 PIN3

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

typedef struct sccp_packet {
    uint8_t cab_id;
    uint8_t cmd_id;
    uint8_t data_len;
    uint8_t data[DATA_SIZE];

} sccp_packet_t;

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
        void encode(uint8_t* data, sccp_packet_t* packet);
        void decode(uint8_t* data, sccp_packet_t* packet);
        uint8_t tx_ready();
};

#endif