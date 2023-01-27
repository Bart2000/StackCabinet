#ifndef BLEUTOOTH_H // To make sure you don't declare the function more than once by including the header multiple times.
#define BLEUTOOTH_H

#include <stdio.h>
#include <esp_log.h>
#include "esp_bt.h"
#include "esp_bt_main.h"
#include "esp_gap_bt_api.h"
#include "esp_bt_device.h"
#include "esp_spp_api.h"
#include "nvs.h"
#include "nvs_flash.h"
#include <led_strip.h>

#include <led_strip.h>
#include <SCCP.h>

#define SPP_CB_TAG "ESP_SPP_CB"
#define GAP_CB_TAG "ESP_BT_GAP_CB"
#define SETUP_BT_TAG "ESP_SETUP_BT"

#define SPP_SERVER_NAME "SPP_SERVER"
#define DEVICE_NAME "StackCabinet"

static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_AUTHENTICATE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;

#define HEADER_SIZE 2
#define DATA_SIZE 16

class Bluetooth;

// SCBP commands
enum command_bluetooth
{
    REQUEST_GRID,
    SET_COM,
    SET_LED,
    OPEN_CAB
};

// Type definition for class method pointer
typedef void (*MPB)(uint8_t *);

// Typedef struct for SCBP packets
typedef struct bluetooth_packet
{
    uint8_t cab_id;
    uint8_t cmd_id;
    uint8_t data_len;
    uint8_t data[DATA_SIZE];

    bluetooth_packet()
    {
        *data = {0};
    };

    bluetooth_packet(uint8_t cab_id, uint8_t cmd_id, uint8_t data_len, uint8_t *data)
    {
        this->cab_id = cab_id;
        this->cmd_id = cmd_id;
        this->data_len = data_len;
        memcpy(this->data, data, data_len);
    }
} bluetooth_packet_t;

typedef struct spp_param
{
    esp_spp_cb_param_t spp_cb_param;
    bool spp_conn;
    char buffer[HEADER_SIZE + DATA_SIZE];
} spp_param_t;

// Typedef struct for SCBP handler commands
typedef struct scbp_command
{
    MPB handler;
} scbp_command_t;

class Bluetooth
{
private:
    static void spp_init_evt(const char *name);
    static void esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param);
    static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param);
    char *bda2str(uint8_t *bda, char *str, size_t size);
    static void handle_command();
    static void decode(char *data, bluetooth_packet *packet);
public:
    Bluetooth(SCCP *sccp, led_strip_t *strip);
    static SCCP *sccp;
    static led_strip_t *strip;

    static void send_data(int len, uint8_t *data);
    static void request_grid(uint8_t *data);
    static void set_component(uint8_t *data);
    static void set_led(uint8_t *data);
    static void open_drawer(uint8_t *data);
};

#endif