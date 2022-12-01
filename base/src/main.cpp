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

#define BAUDRATE 115200
#define BUF_SIZE 2048
#define UART_NUM UART_NUM_1
#define TX_GPIO 10
#define RX_GPIO 9
#define TIMOUT_MS 20

SCCP sccp;
TaskHandle_t handle;
static intr_handle_t handle_console;
static QueueHandle_t uart_queue;

extern "C"
{
    void app_main(void);
}

void app_main() 
{   
    uint8_t packets[][3] = {
        {0x00, 0x21, 0x01}, // ICAB
        {0x01, 0x01, 0x00}, // AGAT
        {0x00, 0x21, 0x02}, // ICAB
        {0x01, 0x11, 0x00}, // DGAT
    };    
    
    uint8_t count = 0;

    gpio_set_level(GPIO_NUM_12, 0);

    while(1) 
    {
        if(!gpio_get_level(GPIO_NUM_13)) 
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