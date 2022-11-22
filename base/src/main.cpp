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
#include "esp_log.h"
#include "driver/gpio.h"
#include "sdkconfig.h"
#include "esp_intr_alloc.h"
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

extern "C"
{
    void app_main(void);
}

static void IRAM_ATTR UART_receive_isr(void* handle) 
{
    printf("AAAA\n");
    uart_clear_intr_status(UART_AT_CMD_CHAR_DET_INT_CLR|UART_NUM, UART_RXFIFO_FULL_INT_CLR|UART_RXFIFO_TOUT_INT_CLR);
    //uart_clear_intr_status(UART_INTR_MASK);
}

void setup() 
{
    // Config specific for GM67
    uart_config_t uart_config =
    {
        .baud_rate = BAUDRATE,              // Baudrate of GM67
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        //.source_clk = UART_SCLK_APB,
    };

    // Set pins to UART 1
    uart_set_pin(UART_NUM, TX_GPIO, RX_GPIO, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    
    // Configure UART 1
    uart_param_config(UART_NUM, &uart_config);
    
    // Install UART drivers for UART 1
    uart_driver_install(UART_NUM, BUF_SIZE, BUF_SIZE, 0, NULL, 0);
    //uart_driver_install(UART_NUM_1, 256, 256, 0, NULL, 0);
    gpio_set_direction(GPIO_NUM_13, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_12, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_14, GPIO_MODE_INPUT);
    gpio_set_direction(GPIO_NUM_27, GPIO_MODE_INPUT);

    //esp_err_t e = uart_isr_free(UART_NUM);
    //printf("%d\n", e);
    uart_isr_register(UART_NUM, UART_receive_isr, NULL, ESP_INTR_FLAG_IRAM, &handle_console);
    //printf("%d\n", e);
    uart_enable_rx_intr(UART_NUM);

    uart_intr_config_t uart_int_config2 = {
    .intr_enable_mask = BIT0,
    .rx_timeout_thresh = 20, //threshold UNIT is the time passed equal to time taken for 1 bytes to arrive
    .txfifo_empty_intr_thresh = 0,
    .rxfifo_full_thresh = 1
    };

    uart_intr_config(UART_NUM, &uart_int_config2);
    //uart_set_mode(UART_NUM_2, UART_MODE_RS485_HALF_DUPLEX);	

    //printf("%d\n", e);
    //uart_disable_tx_intr(UART_NUM);
    uart_set_mode(UART_NUM, UART_MODE_RS485_HALF_DUPLEX
    );
}

void UART_receive_loop(void* handle) 
{
    while(1) 
    {
        vTaskDelay(100);
        printf("Bruh\n");
    }
    vTaskDelete(handle);
}

void app_main() 
{   
    setup();
    //xTaskCreate(UART_receive_loop, "UART receive interrupt", 2048, NULL, 1, &handle);

    uint8_t packets[][3] = {
        {0x00, 0x21, 0x01}, // ICAB
        {0x01, 0x01, 0x00}, // AGAT
        {0x00, 0x21, 0x02}, // ICAB
        {0x01, 0x11, 0x00}, // DGAT
    };    
    
    uint8_t count = 0;

    

    while(1) 
    {
        if(!gpio_get_level(GPIO_NUM_13)) 
        {
            sccp.identify();
        }

        // if(!gpio_get_level(GPIO_NUM_13)) 
        // {
        //     if(count >= sizeof(packets) / sizeof(packets[0])) break;
        //     uint8_t* packet = packets[count++];
        //     uart_write_bytes(UART_NUM, (const  char*)packet, 3);
        //     vTaskDelay(200);
        // }
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