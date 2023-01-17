#include <stdio.h>
#include <driver/uart.h>
#include <driver/gpio.h>
#include <SCCP.h>
#include <esp_err.h>
#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "driver/uart.h"
#include <soc/uart_reg.h>

SCCP sccp;

extern "C"
{
    void app_main(void);
}

void app_main() 
{   
    uint8_t count = 0;

    while(1) 
    {
        if(!gpio_get_level(GPIO_NUM_23)) 
        {
            unsigned long time1 = esp_timer_get_time() / 1000ULL;
            std::string grid;
            sccp.identify(&grid);
            std::cout << grid;
            printf("Time: %ld\n", (long int)((esp_timer_get_time() / 1000ULL) - time1));
            vTaskDelay(100);
        }

        // if(!gpio_get_level(GPIO_NUM_14)) 
        // {
        //     uint8_t sled[2] = {0x01, 0x60};
        //     uart_write_bytes(UART_NUM, (const  char*)sled, sizeof(sled));
        //     vTaskDelay(200);
        // }
        // if(!gpio_get_level(GPIO_NUM_27)) 
        // {
        //     uint8_t sled[2] = {0x02, 0x60};
        //     uart_write_bytes(UART_NUM, (const  char*)sled, sizeof(sled));
        //     vTaskDelay(200);
        // }
        vTaskDelay(1);
    }
}