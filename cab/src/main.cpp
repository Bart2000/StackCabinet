#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <string.h>
#include <SCCP.h>

int main(void) {
    SCCP sccp;
    sccp.init();

    while(1) {
        uint8_t data[1] = {1};

        sccp_packet_t packet = {
            .cab_id = 1,
            .cmd_id = AGAT,
            .data_len = sizeof(data),
        };

        memcpy(packet.data, data, packet.data_len);

        //sccp.send(packet);
        uint8_t tmp[3] = {
            0xF0,
            0x01,
            0x00
        };

        sccp.handle_command(tmp);
        
        _delay_ms(10000);
    }
}