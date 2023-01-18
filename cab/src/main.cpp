#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <avr/interrupt.h>
#include <SCCP.h>
#include <string.h>
#include <LED.h>

void setup();

SCCP sccp;
LED led;

int main(void) {
    //setup();
    //sccp.init();

    led.init();
    color_t red = {255, 255, 0};
    led.set_led(red, 255, 4);

    while(1);
}

void setup() 
{
    PORTB.DIR |= PIN0_bm | PIN1_bm | PIN5_bm;
    PORTA.DIR |= PIN3_bm | PIN4_bm | PIN5_bm;
    PORTC.DIR |= PIN0_bm;

    //PORTB.OUT |= PIN0_bm;
    
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD;
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD_RESET_gc;
    // TCA0_SPLIT_CTRLESET |= TCA_SPLIT_CMD_RESTART_gc;
   
    TCA0.SINGLE.CTRLA &= ~(TCA_SINGLE_ENABLE_bm);  
    TCA0.SINGLE.CTRLESET = TCA_SINGLE_CMD_RESET_gc; 

    PORTMUX.CTRLC |= PORTMUX_TCA02_bm;
    PORTMUX.CTRLD |= PORTMUX_TCB0_bm;

    TCA0.SPLIT.CTRLD = TCA_SPLIT_SPLITM_bm;
    TCA0.SPLIT.LPER = 0xFF;
    TCA0.SPLIT.HPER = 0xFF;
    TCA0.SPLIT.LCMP0 = 0x00;    // Power (PNP)
    TCA0.SPLIT.LCMP1 = 0xFF;    //G
    TCA0.SPLIT.LCMP2 = 0xFF;    //B
    TCA0.SPLIT.HCMP0 = 0xFF;    //R
    TCA0.SPLIT.HCMP1 = 0x00;    // Power 2
    TCA0.SPLIT.HCMP2 = 0x00;    // Power 3
    TCA0.SPLIT.CTRLB |= TCA_SPLIT_LCMP0EN_bm | TCA_SPLIT_LCMP1EN_bm | TCA_SPLIT_LCMP2EN_bm | TCA_SPLIT_HCMP0EN_bm | TCA_SPLIT_HCMP1EN_bm | TCA_SPLIT_HCMP2EN_bm;
    TCA0.SPLIT.CTRLA |= TCA_SPLIT_CLKSEL_DIV16_gc | TCA_SPLIT_ENABLE_bm;

    // Configure TCB0 
    TCB0.CCMP = 0x00FF;                                 
    TCB0.CTRLA |= TCB_CLKSEL_CLKDIV2_gc | TCB_ENABLE_bm;
    TCB0.CTRLB |= TCB_CCMPEN_bm;
    TCB0.CTRLB |= TCB_CNTMODE_PWM8_gc;

    return;

    int count = 0;
    int color = 0;
    while(1) 
    {
        _delay_ms(8);
        if(count++ >= 255) 
        {
            count = 0;
            color += 1;
        }
        
        if(color == 0) {
            TCA0.SPLIT.LCMP1 = count;
        }
        if(color == 1) {
            TCA0.SPLIT.LCMP2 = count;
        }
        if(color == 2) {
            TCA0.SPLIT.HCMP0 = count;
        }
        if(color == 3) {
            break;
        }
    }

    count = 0;
    while(1) 
    {
        _delay_ms(8);
        if(count++ >= 255) 
        {
            count = 0;
        }

        TCA0.SPLIT.LCMP0 = count;
        TCA0.SPLIT.HCMP1 = count;
        TCA0.SPLIT.HCMP2 = count;
        TCB0.CCMPH = count;
    }

    

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