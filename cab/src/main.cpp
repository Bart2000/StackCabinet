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
    //sccp.init();

    while(1);
}

void setup() 
{
    PORTB.DIR |= PIN0_bm;
    PORTB.OUT |= PIN0_bm;
    
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD;
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD_RESET_gc;
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD_RESTART_gc;
   
   TCA0.SINGLE.CTRLA &= ~(TCA_SINGLE_ENABLE_bm);  
   
    TCA0.SINGLE.CTRLESET = TCA_SINGLE_CMD_RESET_gc; 

    TCA0.SPLIT.CTRLD = TCA_SPLIT_SPLITM_bm;
    TCA0.SPLIT.LPER = 0xCF;
    TCA0.SPLIT.HPER = 0xCF;
    TCA0.SPLIT.LCMP0 = 0x40;
    TCA0.SPLIT.HCMP0 = 0x40;
    TCA0.SPLIT.CTRLB |= TCA_SPLIT_LCMP0EN_bm | TCA_SPLIT_HCMP0EN_bm;
    TCA0.SPLIT.CTRLA |= TCA_SPLIT_CLKSEL_DIV16_gc | TCA_SPLIT_ENABLE_bm;

    

    return;

    // Set PA7 as output
    PORTA.DIR = PIN7_bm;
    PORTA.OUT |= PIN7_bm;

    // Set all gates as inputs and enable pullups with inverted logic
    PORTC.DIRCLR = GATES; 
    PORTC.PIN0CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN1CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN2CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;
    PORTC.PIN3CTRL |= PORT_INVEN_bm | PORT_PULLUPEN_bm;

    // Enable global interrupts
    SREG |= CPU_I_bm;
}

// Interrupt Service Routine for RX receive interrupt to load byte into SCCP buffer
ISR(USART0_RXC_vect) 
{
    sccp.receive_byte(USART0_RXDATAL);
}