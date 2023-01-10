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
#include <neopixel.h>
#include <led_strip.h>
#include "effects/rainbow.h"
#include "effects/effectFactory.h"


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
    //gpio_set_direction(GPIO_NUM_18, GPIO_MODE_OUTPUT);
    //gpio_set_level(GPIO_NUM_5, 1);

    // Pixels* pixels = new Pixels(GPIO_NUM_18, 1, Pixels::StripType::ws6812, RMT_CHANNEL_0, 2.8);
    // Effect* effect = EffectFactory::CreateEffect("rainbow", 1, 5);
    // Pixel red = {255, 0, 0, 255};
    // pixels->SetPixel(0, red);
    // pixels->Write();

    //led_strip_t* test = led_strip_init(RMT_CHANNEL_0, GPIO_NUM_18, 1);
    //test->set_pixel(test, 0, 255, 0, 0);
    //test->refresh(test, 200);
        
    // while(true) 
    // {
    //     effect->Run(pixels);
    // }

    while(1) 
    {
        if(!gpio_get_level(GPIO_NUM_14)) 
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