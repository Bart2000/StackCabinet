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

static void UART_receive_loop(void* handle) 
{
    uart_event_t event;
    uint8_t buffer[HEADER_SIZE + DATA_SIZE];
    uint8_t buffer_count;

    while(1) 
    {
        if(xQueueReceive(uart_queue, (void*)&event, (TickType_t)portMAX_DELAY)) 
        {
            switch(event.type) 
            {
                case UART_DATA:
                    uart_read_bytes(UART_NUM, buffer, event.size, portMAX_DELAY);

                    if((buffer[HEADER_SIZE-1] & DATA_LEN_MASK) + HEADER_SIZE == event.size)
                    {
                        //sccp.handle_command(buffer);
                    }
                    break;
                default:
                    break;
            }
        }
        vTaskDelay(1);
    }
    vTaskDelete(handle);
}

void app_main() 
{   
    //sccp.initialize();
    //xTaskCreate(sccp.receive_loop, "UART_receive_loop", 2048, NULL, 1, NULL);
    //xTaskCreate(UART_receive_loop, "UART receive interrupt", 2048, NULL, 1, &handle);

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
            sccp.identify();
        }

        if(!gpio_get_level(GPIO_NUM_13)) 
        {
            printf("Bruh\n");
            vTaskDelay(200);
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