#ifndef NVM_H
#define NVM_H

#include <stdio.h>
#include <avr/io.h>
#include <avr/eeprom.h>

#define PAGE_SIZE 32
#define PRODUCT_ADDR_FLAG 0
#define LED_ADDR_FLAG 1
#define PRODUCT_ADDR 2 
#define LED_ADDR 3

class NVM 
{
    public:
        NVM();
        uint8_t write_byte(uint8_t address, uint8_t data);
        uint8_t read_byte(uint8_t address);

    private:
        uint8_t eeprom_is_busy();
};

#endif