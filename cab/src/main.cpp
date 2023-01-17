#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <avr/interrupt.h>
#include <SCCP.h>
#include <string.h>
#include <NVM.h>

void setup();

SCCP sccp;

int main(void) {
    setup();
    sccp.init();

    while(1);
}

void setup() 
{
    PORTB.DIR |= PIN5_bm;

    // Set all gates as inputs and enable pullups with inverted logic
    PORTA.DIRCLR = GATES; 
    PORTA.PIN1CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTA.PIN2CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTA.PIN6CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTA.PIN7CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;

    // Enable global interrupts
    SREG |= CPU_I_bm;
}

// Interrupt Service Routine for RX receive interrupt to load byte into SCCP buffer
ISR(USART0_RXC_vect) 
{
    sccp.receive_byte(USART0_RXDATAL);
}