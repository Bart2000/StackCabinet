#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <avr/io.h>



class SCCP 
{
    public: 
        SCCP();
        void init();
        void send(char c);

    private:
        uint8_t tx_ready();
};

#endif