#include "Bluetooth.h"

Bluetooth::Bluetooth(SCCP* sccp, LED* led)
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

    printf("Own address:[%s]", bda2str((uint8_t *)esp_bt_dev_get_address(), bda_str, sizeof(bda_str)));
}

/**
 * Setup the Bluetooth SPP service
 * @param name pointer to the name for the Bluetooth device
 */
void Bluetooth::spp_init_evt(const char *name)
{
    esp_bt_dev_set_device_name(name);
    esp_bt_gap_set_scan_mode(ESP_BT_CONNECTABLE, ESP_BT_GENERAL_DISCOVERABLE);
    esp_spp_start_srv(sec_mask, role_slave, 0, SPP_SERVER_NAME);
}

void Bluetooth::esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param)
{

    // ESP_LOGI(GAP_CB_TAG, "Fuction: esp_bt_gap_cb()");
    switch (event)
    {
    case ESP_BT_GAP_AUTH_CMPL_EVT:
    {
        if (param->auth_cmpl.stat == ESP_BT_STATUS_SUCCESS)
        {
            // ESP_LOGI(GAP_CB_TAG, "authentication success: %s", param->auth_cmpl.device_name);
            // esp_log_buffer_hex(GAP_CB_TAG, param->auth_cmpl.bda, ESP_BD_ADDR_LEN);
        }
        else
        {
            // ESP_LOGE(GAP_CB_TAG, "authentication failed, status:%d", param->auth_cmpl.stat);
        }
        break;
    }
    // Must be set in sdkconfig.h: CONFIG_BT_SSP_ENABLED == true
    // This enables the Secure Simple Pairing.
    case ESP_BT_GAP_CFM_REQ_EVT:
        // ESP_LOGI(GAP_CB_TAG, "ESP_BT_GAP_CFM_REQ_EVT Please compare the numeric value: %d", param->cfm_req.num_val);
        esp_bt_gap_ssp_confirm_reply(param->cfm_req.bda, true);
        break;
    default:
    {
        // ESP_LOGI(GAP_CB_TAG, "event: %d", event);
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

void Bluetooth::esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param)
{
    switch (event)
    {
    case ESP_SPP_INIT_EVT:
        spp_init_evt(DEVICE_NAME);
        break;
    case ESP_SPP_DATA_IND_EVT: // When SPP connection received data, the event comes, only for ESP_SPP_MODE_CB

        esp_log_buffer_hex("Received HEX Data", param->data_ind.data, param->data_ind.len);
        esp_log_buffer_char("Received String Data", param->data_ind.data, param->data_ind.len);
        // saveData(RECEIVED_DATA, param->data_ind.len, param->data_ind.data);
        // processCommand(param, checkCommand(charArrayLastSendData));

        // ESP_LOGI(SPP_CB_TAG, "Call esp_spp_write(param->write.handle, 8, Received)");
        // char *c = "RECEIVED";
        // uint8_t *u = (uint8_t *)c;
        // uint8_t x = u[1];
        // esp_spp_write(param->srv_open.handle, strlen(c), u);
        // saveData(SEND_DATA, strlen(c), u);
        break;

    // case ESP_SPP_SRV_OPEN_EVT: // After connection is established, short before data is received
    // When SPP Server connection open, the event comes
    // In use in Acceptor
    //     ESP_LOGI(SPP_CB_TAG, "ESP_SPP_SRV_OPEN_EVT");

    //     if (bWriteAfterSvrOpenEvt)
    //     {
    //         char *c = "REQUEST_GRID|{{255}, {0, 255, 2, 3, 0, 0}, {0, 0, 4, 1, 0, 0}, {0, 1, 4, 0, 0, 0}, {0, 3, 2, 0, 0, 0}}";
    //         uint8_t *u = (uint8_t *)c;

    //         esp_spp_write(param->srv_open.handle, strlen(c), u);
    //         saveData(SEND_DATA, strlen(c), u);

    //     }
    //     break;
    default:
        break;
    }
}

/**
 * Convert the Bluetooth Device Address to a String
 * @param bda pointer to the Bluetooth Device Address
 * @param str pointer to the location to store the string of the Bluetooth Device Address
 * @param size size of the Bluetooth Device Address
 */
char* Bluetooth::bda2str(uint8_t *bda, char *str, size_t size)
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