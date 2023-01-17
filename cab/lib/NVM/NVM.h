#ifndef NVM_H
#define NVM_H

#include <stdio.h>
#include <avr/io.h>
#include <avr/eeprom.h>

#define PAGE_SIZE 32
#define PRODUCT_SET_ADDR 0
#define LED_SET_ADDR 4
#define PRODUCT_ADDR 8 
#define LED_ADDR 12

uint8_t EEMEM product_id = 0;

class NVM 
{
    public:
        NVM();
        uint8_t write_byte(uint8_t address, uint8_t data);
        uint8_t read_byte(uint8_t address);

    private:
        uint8_t is_eeprom_ready();
};

#endif