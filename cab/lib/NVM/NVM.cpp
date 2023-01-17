#include <NVM.h>

NVM::NVM() 
{
    // Initialize to 0 to prevent unexpected values
    if(read_byte(PRODUCT_ADDR_FLAG) != 1) write_byte(PRODUCT_ADDR, 0);
    if(read_byte(LED_ADDR_FLAG) != 1) write_byte(LED_ADDR, 0);
}

/**
 * Method to write a byte in the EEPROM. Automatically appends EEPROM address offset. 
 * @param address The address to write to
 * @param data The byte to be written
 * @return Boolean integer that tells if the value has been written
 */
uint8_t NVM::write_byte(uint8_t address, uint8_t data) 
{
    // Do not write if address is outside of custom page size
    if(address >= PAGE_SIZE) return 0;

    // Wait until EEPROM is ready to write
    while(eeprom_is_busy());

    // Load page buffer with data
    *(uint16_t*)(EEPROM_START + address) = data;

    // Write to protected register NVMCTRL.CTRLA to issue page erase write command in NVM controller
    _PROTECTED_WRITE_SPM(NVMCTRL.CTRLA, NVMCTRL_CMD_PAGEERASEWRITE_gc);

    return 1;
}

/**
 * Method to read a byte from the EEPROM. Automatically appends EEPROM address offset. 
 * @param address The address to read from
 * @return The byte that has been read from the EEPROM
 */
uint8_t NVM::read_byte(uint8_t address) 
{
    // Do not read if address is outside of custom page size
    if(address >= PAGE_SIZE) return 0;

    // Wait until EEPROM is ready to read
    while(eeprom_is_busy());

    // Read and return value
    return *(uint16_t*)(EEPROM_START + address);
}

/**
 * Method to check if EEPROM is busy. Checks EEBUSY value in the NVMCTRL.STATUS register.
 * @return Boolean integer that determines if the EEPROM is busy or not 
 */
uint8_t NVM::eeprom_is_busy() 
{
    return NVMCTRL.STATUS & NVMCTRL_EEBUSY_bm;
}