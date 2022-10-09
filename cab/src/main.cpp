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
        uint8_t icab[3] = {
            0x00,
            0x21,
            0x01
        };

        sccp.handle_command(icab);
        _delay_ms(1000);

        if(USART0.STATUS & USART_RXCIF_bm) 
        {
            PORTA.OUT &= ~PIN7_bm;
            _delay_ms(500);
            PORTA.OUT |= PIN7_bm;
            _delay_ms(500);
        }


    //     uint8_t agat[3] = {
    //         0x01,
    //         0x01,
    //         0x03
    //     };

    //     sccp.handle_command(agat);
    //     _delay_ms(4000);

    //     uint8_t dgat[3] = {
    //         0x01,
    //         0x11,
    //         0x03
    //     };

    //     sccp.handle_command(dgat);
    //     _delay_ms(4000);
    }
}