#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>
#include <SCCP.h>

int main(void) {
    SCCP sccp;
    sccp.init();
    char c = 'A';

    while(1) {
        sccp.send(c);
        _delay_ms(500);
    }
}