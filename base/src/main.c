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

    uint8_t packets[][3] = {
        {0x00, 0x21, 0x01}, // ICAB
        {0x01, 0x01, 0x00}, // AGAT
        {0x00, 0x21, 0x02}, // ICAB
        {0x01, 0x11, 0x00}, // DGAT
    };

    // Set pins to UART 1
    uart_set_pin(UART_NUM, TX_GPIO, RX_GPIO, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    
    // Configure UART 1
    uart_param_config(UART_NUM, &uart_config);
    
    // Install UART drivers for UART 1
    uart_driver_install(UART_NUM, BUF_SIZE, BUF_SIZE, 0, NULL, 0);

    gpio_set_direction(GPIO_NUM_13, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_12, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_14, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_27, GPIO_MODE_INPUT);

    gpio_set_level(GPIO_NUM_12, 0);
    
    uint8_t count = 0;

    while(1) 
    {
        if(!gpio_get_level(GPIO_NUM_13)) 
        {
            if(count >= sizeof(packets) / sizeof(packets[0])) break;
            uint8_t* packet = packets[count++];
            uart_write_bytes(UART_NUM, (const  char*)packet, 3);
            vTaskDelay(200);
        }
        if(!gpio_get_level(GPIO_NUM_14)) 
        {
            uint8_t sled[2] = {0x01, 0x60};
            uart_write_bytes(UART_NUM, (const  char*)sled, sizeof(sled));
            vTaskDelay(200);
        }
        if(!gpio_get_level(GPIO_NUM_27)) 
        {
            uint8_t sled[2] = {0x02, 0x60};
            uart_write_bytes(UART_NUM, (const  char*)sled, sizeof(sled));
            vTaskDelay(200);
        }
        vTaskDelay(1);
    }
}