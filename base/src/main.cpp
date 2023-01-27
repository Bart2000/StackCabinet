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
#include <led_strip.h>

extern "C"
{
    void app_main(void);
}

static const rgb_t colors[] = {
    {.r = 0x0f, .g = 0x0f, .b = 0x0f},
    {.r = 0xff, .g = 0x00, .b = 0x00}, // red
    {.r = 0x00, .g = 0xff, .b = 0x00}, // green
    {.r = 0x00, .g = 0x00, .b = 0xff}, // blue
};

void app_main()
{
    led_strip_t strip = {
        .type = LED_STRIP_SK6812,
        .brightness = 255,
        .length = 3,
        .gpio = GPIO_NUM_18,
        .buf = NULL,
    };

    led_strip_install();
    led_strip_init(&strip);

    led_strip_set_pixel(&strip, 0, colors[1]);
    led_strip_set_pixel(&strip, 1, colors[1]);
    led_strip_set_pixel(&strip, 2, colors[1]);
    led_strip_flush(&strip);

    SCCP sccp(&strip);
    Bluetooth esp_bt(&sccp, &strip);

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