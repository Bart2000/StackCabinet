#include <LED.h>

// register8_t** patterns[] = { 
//     (register8_t*[]){&TCA0.SPLIT.HCMP0}, 
//     (register8_t*[]){&TCA0.SPLIT.HCMP1}, 
//     (register8_t*[]){&TCA0.SPLIT.HCMP2}, 
//     (register8_t*[]){&TCB0.CCMPH}, 
// };

pattern_t patterns[] = 
{
    {(register8_t*[]){&TCA0.SPLIT.HCMP0}, 1},
    {(register8_t*[]){&TCA0.SPLIT.HCMP1}, 1},
    {(register8_t*[]){&TCA0.SPLIT.HCMP2}, 1},
    {(register8_t*[]){&TCB0.CCMPH}, 1},
    {(register8_t*[]){&TCA0.SPLIT.HCMP0, &TCA0.SPLIT.HCMP1, &TCA0.SPLIT.HCMP2, &TCB0.CCMPH}, 4},
};

LED::LED() 
{
    this->init();
}

void LED::init() 
{
    // Set PORT outputs for PWM channels
    PORTB.DIR |= PIN0_bm | PIN1_bm | PIN5_bm;
    PORTA.DIR |= PIN3_bm | PIN4_bm | PIN5_bm;
    PORTC.DIR |= PIN0_bm;

    // Reset TCA
    TCA0.SINGLE.CTRLA &= ~(TCA_SINGLE_ENABLE_bm);  
    TCA0.SINGLE.CTRLESET = TCA_SINGLE_CMD_RESET_gc; 

    // Use alternative pins in port multiplexer
    PORTMUX.CTRLC |= PORTMUX_TCA02_bm;      // To PB5
    PORTMUX.CTRLD |= PORTMUX_TCB0_bm;       // To PC0

    // Enable TCA0 split mode
    TCA0.SPLIT.CTRLD = TCA_SPLIT_SPLITM_bm;

    // Set period of high and low compares
    TCA0.SPLIT.LPER = 0xFF;
    TCA0.SPLIT.HPER = 0xFF;

    // Enable low/high compare 0, 1 and 3
    TCA0.SPLIT.CTRLB |= TCA_SPLIT_LCMP0EN_bm | TCA_SPLIT_LCMP1EN_bm | TCA_SPLIT_LCMP2EN_bm | TCA_SPLIT_HCMP0EN_bm | TCA_SPLIT_HCMP1EN_bm | TCA_SPLIT_HCMP2EN_bm;

    // Set compares to 0
    TCA0.SPLIT.LCMP0 = 0x00;    // B
    TCA0.SPLIT.LCMP1 = 0x00;    // G
    TCA0.SPLIT.LCMP2 = 0x00;    // R

    // Turn LEDS off at startup
    TCA0.SPLIT.HCMP0 = 0x00;    // Power LED 0
    TCA0.SPLIT.HCMP1 = 0x00;    // Power LED 1
    TCA0.SPLIT.HCMP2 = 0x00;    // Power LED 2

    // Set prescaler to 16 and enable TCA0
    TCA0.SPLIT.CTRLA |= TCA_SPLIT_CLKSEL_DIV16_gc | TCA_SPLIT_ENABLE_bm;

    // Configure TCB0                                       
    TCB0.CCMP = 0x00FF;                                     // 0% duty cycle, period 255, Power LED 3
    TCB0.CTRLA |= TCB_CLKSEL_CLKDIV2_gc | TCB_ENABLE_bm;    // Prescaler 2, enable TCB0
    TCB0.CTRLB |= TCB_CCMPEN_bm;                            // Enable comparator
    TCB0.CTRLB |= TCB_CNTMODE_PWM8_gc;                      // Configure 8-bit PWM mode
}

void LED::set_led(color_t color, uint8_t brightness, uint8_t pattern) 
{
    uint8_t size = sizeof(patterns) / sizeof(patterns[0]);
    uint8_t leds = patterns[pattern].size;

    if(pattern > size-1) return;

    for(uint8_t i = 0; i < leds; i++) 
    {
        register8_t* cmp = patterns[pattern].leds[i];
        *cmp = brightness;
    }
    
    // RGB
    TCA0.SPLIT.LCMP0 = color.b;
    TCA0.SPLIT.LCMP1 = color.g;
    TCA0.SPLIT.LCMP2 = color.r;
}