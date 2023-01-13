#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <avr/interrupt.h>
#include <SCCP.h>
#include <string.h>
#include <NVM.h>
#include <eeprom.h>

void setup();

SCCP sccp;


int main(void) {
    setup();
    sccp.init();
    uint8_t addr = 0x0A;
    uint8_t val = 2;

    //uint8_t bruh = eeprom_read_byte((uint8_t*)addr);
    //sccp.tmp_led(bruh);
    //eeprom_update_byte((uint8_t*)addr, (uint8_t)3);
    //eeprom_write_byte((uint8_t*)&addr2, 3);

    //uint8_t test = eeprom_read_byte((uint8_t*)addr);
    //eeprom_write_byte((uint8_t*)&addr, 0x03);

    //NVM nvm;
    
    //nvm.write_byte(PRODUCT_ADDR, (uint8_t)4);
    //nvm.write_byte(PRODUCT_SET_ADDR, (uint8_t)2);
    
    //uint8_t test = nvm.read_byte(PRODUCT_ADDR);

    //uint8_t test = eeprom_read_byte((uint8_t*)&addr);

    //sccp.tmp_led(test);

    uint8_t bruh = 0;
    memcpy(&bruh, (uint8_t*)0x140A, 1);

    sccp.tmp_led(bruh);

    memcpy((uint8_t*)0x140A, &val, 1);
    CCP = CCP_SPM_gc;
    NVMCTRL.CTRLA = NVMCTRL_CMD_PAGEWRITE_gc;

    while (NVMCTRL.STATUS & NVMCTRL_EEBUSY_bm);

    uint8_t test = 0;
    memcpy(&test, (uint8_t*)0x140A, 1);

    sccp.tmp_led(test);

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