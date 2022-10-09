#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <SCCP.h>

int main(void) {
    PORTA.DIR = PIN7_bm;
    PORTA.OUT |= PIN7_bm;

    PORTC.DIRCLR = GATES; 
    PORTC.PIN0CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN1CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN2CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN3CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;

    SCCP sccp;
    sccp.init();

    while(1) {
        uint8_t data[1] = {1};

        // sccp_packet_t packet = {
        //     .cab_id = 1,
        //     .cmd_id = AGAT,
        //     .data_len = sizeof(data),
        // };

        //memcpy(packet.data, data, packet.data_len);

        //sccp.send(packet);
        uint8_t icab[3] = {
            0x00,
            0x10,
            0x00
        };

        sccp.handle_command(icab);
        
        _delay_ms(5000);
    }
}