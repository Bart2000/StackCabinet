#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include "nvs.h"
#include "nvs_flash.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_bt.h"
#include "esp_bt_main.h"
#include "esp_gap_bt_api.h"
#include "esp_bt_device.h"
#include "esp_spp_api.h"

#include "time.h"
#include "sys/time.h"

#define button_gpio 32
#define led_gpio 33

#define SPP_CB_TAG "ESP_SPP_CB"
#define GAP_CB_TAG "ESP_BT_GAP_CB"
#define SETUP_BT_TAG "ESP_SETUP_BT"

#define SETUP_TAG "SETUP"
#define SPP_SERVER_NAME "SPP_SERVER"
#define DEVICE_NAME "StackCabinet"

static bool bWriteAfterOpenEvt = true;
static bool bWriteAfterWriteEvt = false;
static bool bWriteAfterSvrOpenEvt = true;
static bool bWriteAfterDataReceived = true;

static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_AUTHENTICATE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;

static char charArrayLastReceivedData[20];
static char charArrayLastSendData[20];

typedef enum
{
    SEND_DATA = 0,
    RECEIVED_DATA = 1
} char_array_store_t;

static void spp_init_evt();

static char *bda2str(uint8_t *bda, char *str, size_t size);

static void saveData(char_array_store_t operation, int len, uint8_t *p_data)
{
    if (operation)
    {
        strncpy(charArrayLastReceivedData, (char*)p_data, len);
        printf("Data stored in charArrayLastReceivedData: %s\r\n", charArrayLastReceivedData);
    }
    else
    {
        strncpy(charArrayLastSendData, (char*)p_data, len);
        printf("Data stored in charArrayLastSendData: %s\r\n", charArrayLastSendData);
    }
}

static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param)
{
    switch (event)
    {
    case ESP_SPP_INIT_EVT:
        spp_init_evt(DEVICE_NAME);
        break;
    case ESP_SPP_DATA_IND_EVT: // When SPP connection received data, the event comes, only for ESP_SPP_MODE_CB
        ESP_LOGI(SPP_CB_TAG, "ESP_SPP_DATA_IND_EVT len=%d handle=%d", param->data_ind.len, param->data_ind.handle);
        ESP_LOGI(SPP_CB_TAG, "Call esp_log_buffer_hex("
                             ",param->data_ind.data,param->data_ind.len)");

        // ESP_LOG_BUFFER_HEX(tag, buffer, buff_len)
        // tag: description tag
        // buffer: Pointer to the buffer array
        // buff_len: length of buffer in bytes

        esp_log_buffer_hex("Received HEX Data", param->data_ind.data, param->data_ind.len);
        esp_log_buffer_char("Received String Data", param->data_ind.data, param->data_ind.len);
        saveData(RECEIVED_DATA, param->data_ind.len, param->data_ind.data);

        // New data
        // abc
        // for (int i = 0; i < SPP_DATA_LEN; ++i)
        // {
        // spp_data[i] = i;
        //     if (param->data_ind.data[i] != 0){
        //         spp_data[i] = param->data_ind.data[i];
        //     } else {
        //         spp_data[i] = 0xf;
        //     }

        // }
        // ESP_LOGI(SPP_TAG, "Call esp_spp_write(param->write.handle, SPP_DATA_LEN, spp_data)");
        // esp_spp_write(param->write.handle, SPP_DATA_LEN, spp_data);

        ESP_LOGI(SPP_CB_TAG, "Call esp_spp_write(param->write.handle, 8, Received)");
        char *c = "Received";
        uint8_t *u = (uint8_t *)c;
        // uint8_t x = u[1];
        esp_spp_write(param->srv_open.handle, 8, u);
        saveData(SEND_DATA, sizeof(u), u);
        break;

    case ESP_SPP_WRITE_EVT:
        // When SPP write operation completes, the event comes, only for ESP_SPP_MODE_CB
        // In use in Initiator

        // Original Acceptor Code - Start
        // ESP_LOGI(SPP_CB_TAG, "ESP_SPP_WRITE_EVT");
        // Original Acceptor Code - End

        // Code copied from Initiator - Start
        // ESP_LOGI(SPP_CB_TAG, "ESP_SPP_WRITE_EVT len=%d cong=%d", param->write.len, param->write.cong);

        // esp_log_buffer_hex("HEX Data was sent", spp_data, SPP_DATA_LEN);

        // ESP_LOGI(SPP_CB_TAG, "if param->write.cong ...");
        // if (param->write.cong == 0)
        // {
        // ESP_LOGI(SPP_CB_TAG, "param->write.cong == 0");
        //     if (bWriteAfterWriteEvt)
        //     {
        // ESP_LOGI(SPP_CB_TAG, "bWriteAfterWriteEvt = true");
        // ESP_LOGI(SPP_CB_TAG, "Call esp_spp_write(param->write.handle, SPP_DATA_LEN, spp_data)");
        //         esp_spp_write(param->write.handle, SPP_DATA_LEN, spp_data);
        //     }

        // }

        break;
    case ESP_SPP_SRV_OPEN_EVT: // After connection is established, short before data is received
        // When SPP Server connection open, the event comes
        // In use in Acceptor
        ESP_LOGI(SPP_CB_TAG, "ESP_SPP_SRV_OPEN_EVT");

        if (bWriteAfterSvrOpenEvt)
        {
            char *c = "Hello_new_connection";
            uint8_t *u = (uint8_t *)c;

            esp_spp_write(param->srv_open.handle, 21, u);
            saveData(SEND_DATA, sizeof(u), u);
        }
        break;
    default:
        break;
    }
}

/**
 * Setup the Bluetooth SPP service
 * @param name pointer to the name for the Bluetooth device
 */
static void spp_init_evt(const char *name)
{
    esp_bt_dev_set_device_name(name);
    esp_bt_gap_set_scan_mode(ESP_BT_CONNECTABLE, ESP_BT_GENERAL_DISCOVERABLE);
    esp_spp_start_srv(sec_mask, role_slave, 0, SPP_SERVER_NAME);
}

static char *bda2str(uint8_t *bda, char *str, size_t size)
{
    if (bda == NULL || str == NULL || size < 18)
    {
        return NULL;
    }

    uint8_t *p = bda;
    sprintf(str, "%02x:%02x:%02x:%02x:%02x:%02x",
            p[0], p[1], p[2], p[3], p[4], p[5]);
    return str;
}

void esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param)
{

    ESP_LOGI(GAP_CB_TAG, "Fuction: esp_bt_gap_cb()");
    switch (event)
    {
    case ESP_BT_GAP_AUTH_CMPL_EVT:
    {
        if (param->auth_cmpl.stat == ESP_BT_STATUS_SUCCESS)
        {
            ESP_LOGI(GAP_CB_TAG, "authentication success: %s", param->auth_cmpl.device_name);
            esp_log_buffer_hex(GAP_CB_TAG, param->auth_cmpl.bda, ESP_BD_ADDR_LEN);
        }
        else
        {
            ESP_LOGE(GAP_CB_TAG, "authentication failed, status:%d", param->auth_cmpl.stat);
        }
        break;
    }
    // Must be set in sdkconfig.h: CONFIG_BT_SSP_ENABLED == true
    // This enables the Secure Simple Pairing.
    case ESP_BT_GAP_CFM_REQ_EVT:
        ESP_LOGI(GAP_CB_TAG, "ESP_BT_GAP_CFM_REQ_EVT Please compare the numeric value: %d", param->cfm_req.num_val);
        esp_bt_gap_ssp_confirm_reply(param->cfm_req.bda, true);
        break;
    default:
    {
        ESP_LOGI(GAP_CB_TAG, "event: %d", event);
        //  0 ESP_BT_GAP_DISC_RES_EVT
        //  1 ESP_BT_GAP_DISC_STATE_CHANGED_EVT
        //  2 ESP_BT_GAP_RMT_SRVCS_EVT
        //  3 ESP_BT_GAP_RMT_SRVC_REC_EVT
        //  4 ESP_BT_GAP_AUTH_CMPL_EVT
        //  5 ESP_BT_GAP_PIN_REQ_EVT
        //  6 ESP_BT_GAP_CFM_REQ_EVT
        //  7 ESP_BT_GAP_KEY_NOTIF_EVT
        //  8 ESP_BT_GAP_KEY_REQ_EVT
        //  9 ESP_BT_GAP_READ_RSSI_DELTA_EVT
        // 10 ESP_BT_GAP_CONFIG_EIR_DATA_EVT
        // 11 ESP_BT_GAP_EVT_MAX
        break;
    }
    }
    return;
}

void esp_setup_bt()
{
    char bda_str[18] = {0};
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    ESP_ERROR_CHECK(esp_bt_controller_mem_release(ESP_BT_MODE_BLE));

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();

    esp_bt_controller_init(&bt_cfg);

    esp_bt_controller_enable(ESP_BT_MODE_CLASSIC_BT);

    esp_bluedroid_init();

    esp_bluedroid_enable();

    esp_bt_gap_register_callback(esp_bt_gap_cb);

    esp_spp_register_callback(esp_spp_cb);

    esp_spp_init(esp_spp_mode);

#if (CONFIG_BT_SSP_ENABLED == true)
    /* Set default parameters for Secure Simple Pairing */
    esp_bt_sp_param_t param_type = ESP_BT_SP_IOCAP_MODE;
    esp_bt_io_cap_t iocap = ESP_BT_IO_CAP_IO;
    esp_bt_gap_set_security_param(param_type, &iocap, sizeof(uint8_t));
#endif

    /*
     * Set default parameters for Legacy Pairing
     * Use variable pin, input pin code when pairing
     */
    esp_bt_pin_type_t pin_type = ESP_BT_PIN_TYPE_VARIABLE;
    esp_bt_pin_code_t pin_code;
    esp_bt_gap_set_pin(pin_type, 0, pin_code);

    ESP_LOGI(SETUP_BT_TAG, "Own address:[%s]", bda2str((uint8_t *)esp_bt_dev_get_address(), bda_str, sizeof(bda_str)));
}

void app_main(void)
{
    esp_setup_bt();
}