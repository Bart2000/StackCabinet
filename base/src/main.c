#include <stdio.h>
#include <driver/uart.h>
#include <driver/gpio.h>

#define BAUDRATE 115200
#define BUF_SIZE 2048
#define UART_NUM UART_NUM_1
#define TX_GPIO 10
#define RX_GPIO 9
#define TIMOUT_MS 20

void app_main() 
{
    // Config specific for GM67
    uart_config_t uart_config =
    {
        .baud_rate = BAUDRATE,              // Baudrate of GM67
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_APB,
    };

    // Set pins to UART 1
    uart_set_pin(UART_NUM, TX_GPIO, RX_GPIO, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    
    // Configure UART 1
    uart_param_config(UART_NUM, &uart_config);
    
    // Install UART drivers for UART 1
    uart_driver_install(UART_NUM, BUF_SIZE, BUF_SIZE, 0, NULL, 0);

    gpio_set_direction(GPIO_NUM_13, GPIO_MODE_INPUT);
    
    while(1) 
    {
        int level = gpio_get_level(GPIO_NUM_13);
        uint8_t icab[3] = {0x00, 0x21, 0x01};

        if(!level) 
        {
            uart_write_bytes(UART_NUM, (const  char*)icab, sizeof(icab));
            vTaskDelay(200);
        }
        vTaskDelay(1);
    }
}