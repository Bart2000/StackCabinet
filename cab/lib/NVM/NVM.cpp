#include <NVM.h>

NVM::NVM() 
{
    // Initialize to 0 to prevent unexpected values
    if(read_byte(PRODUCT_ADDR_FLAG) != 1) write_byte(PRODUCT_ADDR, 0);
    if(read_byte(LED_ADDR_FLAG) != 1) write_byte(LED_ADDR, 0);
}

uint8_t NVM::write_byte(uint8_t address, uint8_t data) 
{
    if(address >= PAGE_SIZE) return 0;

    while(eeprom_is_busy());

    *(uint16_t*)(EEPROM_START + address) = data;

    _PROTECTED_WRITE_SPM(NVMCTRL.CTRLA, NVMCTRL_CMD_PAGEERASEWRITE_gc);

    return 1;
}

uint8_t NVM::read_byte(uint8_t address) 
{
    if(address >= PAGE_SIZE) return 0;

    while(eeprom_is_busy());

    return *(uint16_t*)(EEPROM_START + address);
}

uint8_t NVM::eeprom_is_busy() 
{
    return NVMCTRL.STATUS & NVMCTRL_EEBUSY_bm;
}