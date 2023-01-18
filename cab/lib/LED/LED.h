#ifndef LED_H
#define LED_H

#include <stdint.h>
#include <avr/io.h>

typedef struct color
{
    uint8_t r;
    uint8_t g;
    uint8_t b;
} color_t;

typedef struct pattern 
{
    register8_t** leds;
    uint8_t size;
} pattern_t;

class LED 
{
    public:
        LED();
        void init();
        void set_led(color_t color, uint8_t brightness, uint8_t pattern);
};

#endif