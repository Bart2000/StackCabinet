#include <NVM.h>

NVM::NVM() 
{
    // Initialize to 0 to prevent unexpected values
    //if(read_byte(PRODUCT_SET_ADDR) != 1) write_byte(PRODUCT_ADDR, 0);
    //if(read_byte(LED_SET_ADDR) != 1) write_byte(LED_ADDR, 0);
}

uint8_t NVM::write_byte(uint8_t address, uint8_t data) 
{
    if(address >= PAGE_SIZE) return 0;

    while(!is_eeprom_ready());

    uint8_t addr = EEPROM_START + address;
    eeprom_write_byte((uint8_t*)&addr, data);

    return 1;
}

uint8_t NVM::read_byte(uint8_t address) 
{
    if(address >= PAGE_SIZE) return 0;

    while(!is_eeprom_ready());

    uint8_t addr = EEPROM_START + address;
    return eeprom_read_byte((uint8_t*)&addr);
}

uint8_t NVM::is_eeprom_ready() 
{
    return !(NVMCTRL.STATUS & NVMCTRL_EEREADY_bm);
}