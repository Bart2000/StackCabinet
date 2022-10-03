#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <SCCP.h>

int main(void) {
    SCCP sccp;
    sccp.init();
    char c = 'A';

    while(1) {
        uint8_t rgb[3] = {255, 0, 0};

        sccp_packet t = {
            .cab_id = 128,
            .cmd_id = SLED,
            .data_len = sizeof(rgb),
            .data = rgb
        };

        sccp.send(t);
        _delay_ms(1000);
    }
}