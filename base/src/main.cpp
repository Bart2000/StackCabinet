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
    SCCP sccp;
    Bluetooth esp_bt(&sccp);

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
        if (!gpio_get_level(RESET_GPIO))
        {
            unsigned long time1 = esp_timer_get_time() / 1000ULL;
            std::string grid;
            std::cout << esp_get_free_heap_size() << std::endl;
            sccp.identify();
            std::cout << esp_get_free_heap_size() << std::endl;
            sccp.graph_to_json(&grid);
            std::cout << grid;
            printf("Time: %ld\n", (long int)((esp_timer_get_time() / 1000ULL) - time1));
            vTaskDelay(100);
        }
        vTaskDelay(1);
    }
}