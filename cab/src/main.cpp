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
        uint8_t buffer[HEADER_SIZE + DATA_SIZE];

        if(USART0.STATUS & USART_RXCIF_bm) 
        {
            uint8_t i = 0;
            while(USART0.STATUS & USART_RXCIF_bm) 
            {
                if(i > sizeof(buffer)) break;
                buffer[i++] = USART0_RXDATAL;
            }

            sccp.handle_command(buffer);
        }
    }
}