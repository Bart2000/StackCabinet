#define F_CPU 3333333

#include <stdio.h>
#include <avr/io.h>
#include <avr/delay.h>

#define USART0_BAUD_RATE(BAUD_RATE) ((float)(F_CPU * 64 / (16 * (float)BAUD_RATE)) + 0.5)

int main(void) {
    PORTA.DIR |= PIN7_bm;

    USART0.CTRLA = USART_LBME_bm;
    USART0.CTRLB = USART_ODME_bm | USART_TXEN_bm | USART_RXEN_bm;
    USART0.CTRLC = USART_CHSIZE_8BIT_gc | USART_SBMODE_1BIT_gc;
    USART0.BAUD = (uint16_t)USART0_BAUD_RATE(115200);

    while(1) {
        if(USART0.STATUS & USART_DREIF_bm) 
        {
            USART0.TXDATAL = 'C';
            _delay_ms(500);
        }
    }
}