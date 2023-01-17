#include <stdio.h>
#include <driver/uart.h>
#include <driver/gpio.h>
#include <SCCP.h>
#include <Bluetooth.h>
#include <esp_err.h>
#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "driver/uart.h"
#include <soc/uart_reg.h>


extern "C"
{
    void app_main(void);
}

void app_main()
{
    printf("Hello world\n");
    esp_setup_bt();
    SCCP sccp;
    // Bluetooth esp_bt;

    // led_strip_t *test = led_strip_init(RMT_CHANNEL_0, GPIO_NUM_18, 3);
    // gpio_set_direction(GPIO_NUM_18, GPIO_MODE_OUTPUT);
    // gpio_set_level(GPIO_NUM_5, 1);

    // Pixels* pixels = new Pixels(GPIO_NUM_18, 1, Pixels::StripType::ws6812, RMT_CHANNEL_0, 2.8);
    // Effect* effect = EffectFactory::CreateEffect("rainbow", 1, 5);
    // Pixel red = {255, 0, 0, 255};
    // pixels->SetPixel(0, red);
    // pixels->Write();

    // while(true)
    // {
    //     test->set_pixel(test, 0, 255, 0, 0);
    //     test->set_pixel(test, 1, 0, 255, 0);
    //     test->set_pixel(test, 2, 0, 0, 255);
    //     test->refresh(test, 200);
    //     vTaskDelay(100);
    //     test->set_pixel(test, 2, 255, 0, 0);
    //     test->set_pixel(test, 0, 0, 255, 0);
    //     test->set_pixel(test, 1, 0, 0, 255);
    //     test->refresh(test, 200);
    //     vTaskDelay(100);
    //     test->set_pixel(test, 1, 255, 0, 0);
    //     test->set_pixel(test, 2, 0, 255, 0);
    //     test->set_pixel(test, 0, 0, 0, 255);
    //     test->refresh(test, 200);
    //     vTaskDelay(100);
    // }

    while (1)
    {
        if (!gpio_get_level(GPIO_NUM_23))
        {
            gpio_set_level(GPIO_NUM_5, 0);
            vTaskDelay(100);
            gpio_set_level(GPIO_NUM_5, 1);
            vTaskDelay(100);

            unsigned long time1 = esp_timer_get_time() / 1000ULL;
            std::string grid;
            sccp.identify(&grid);
            std::cout << grid;
            printf("Time: %ld\n", (long int)((esp_timer_get_time() / 1000ULL) - time1));
            vTaskDelay(100);
        }
        if (!gpio_get_level(GPIO_NUM_23))
        {
            gpio_set_level(GPIO_NUM_5, 0);
            vTaskDelay(100);
            gpio_set_level(GPIO_NUM_5, 1);
            // After reboot start SCCP identify for all cabs
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