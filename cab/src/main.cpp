#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>

int main(void) {
    PORTA.DIR |= PIN7_bm;

    for(int i = 0; i < 1000; i++) {
        PORTA.OUT |= PIN7_bm;
        _delay_ms(500);
        PORTA.OUT &= ~PIN7_bm;
        _delay_ms(500);
    }
}