#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <avr/interrupt.h>
#include <SCCP.h>
#include <string.h>

SCCP sccp;

int main(void) {
    PORTA.DIR = PIN7_bm;
    PORTA.OUT |= PIN7_bm;

    PORTC.DIRCLR = GATES; 
    PORTC.PIN0CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN1CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN2CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN3CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;

    SREG |= CPU_I_bm;
    sccp.init();

    while(1);
}

ISR(USART0_RXC_vect) 
{
    sccp.receive_byte(USART0_RXDATAL);
}