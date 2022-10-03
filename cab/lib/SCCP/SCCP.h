#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <avr/io.h>

#define HEADER_SIZE 2

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
    uint8_t* data;

} sccp_packet;

class SCCP 
{
    public: 
        SCCP();
        void init();
        void send(sccp_packet cmd);

    private:
        uint8_t tx_ready();
};

#endif