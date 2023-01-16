#include "esp_log.h"
#include "esp_bt.h"
#include "esp_bt_main.h"
#include "esp_gap_bt_api.h"
#include "esp_bt_device.h"
#include "esp_spp_api.h"
#include "nvs.h"
#include "nvs_flash.h"

#define SPP_SERVER_NAME "SPP_SERVER"
#define DEVICE_NAME "StackCabinet"

static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_AUTHENTICATE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;

class Bluetooth
{
private:
    static void spp_init_evt(const char *name);
    static void esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param);
    static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param);
    char *bda2str(uint8_t *bda, char *str, size_t size);

public:
    Bluetooth();
};
