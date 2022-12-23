#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <avr/interrupt.h>
#include <SCCP.h>
#include <string.h>

void setup();

SCCP sccp;

int main(void) {
    setup();
    sccp.init();

    while(1);
    // while(1) 
    // {
    //     uint8_t gates = PORTA.IN & GATES;
    //     uint8_t gate = sccp.get_gate(gates);
    //     char buff[8];
    //     sprintf(buff, "%d\n", gate);
    //     uint8_t length = strlen(buff);
    //     for(uint8_t i = 0; i < length; i++) 
    //     {
    //         USART0.TXDATAL = buff[i];
    //     }
    //     _delay_ms(500);
    // }
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