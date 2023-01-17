#ifndef SCCP_H
#define SCCP_H

#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <driver/gpio.h>
#include <driver/uart.h>
#include <vector>
#include <iostream>
#include <algorithm>
#include <sstream>
#include <iterator>

#define HEADER_SIZE 2
#define DATA_SIZE 16
#define CMD_ID_SHIFT 4
#define DATA_LEN_MASK 0x0F
#define BASE_ID 255
#define GATE GPIO_NUM_12
#define BROADCAST_ID 0
#define BAUDRATE 115200
#define BUF_SIZE 2048
#define UART_NUM UART_NUM_1
#define TX_GPIO 13
#define RX_GPIO 12
#define TIMEOUT_MS 10
#define MEMBERS 6

#define BAUDRATE 115200

using Graph = std::vector<std::vector<uint8_t>>;
//using json = nlohmann::json;

class SCCP;

// SCCP commands
enum Command {
    AGAT,
    DGAT,
    ICAB,
    IACK,
    INACK,
    OCAB,
    SLED,
    ACK,
    NACK
};

// Type of the cabinet
enum Type {
    SQUARE,
    RECTANGLE,
    BIGSQUARE
};

// Type definition for class method pointer
typedef void (SCCP::*MP)(uint8_t*); 

// Typedef struct for SCCP packets
typedef struct sccp_packet 
{
    uint8_t cab_id;
    uint8_t cmd_id;
    uint8_t data_len;
    uint8_t data[DATA_SIZE];

    sccp_packet() 
    {
        *data = {0};
    };

    sccp_packet(uint8_t cab_id, uint8_t cmd_id, uint8_t data_len, uint8_t* data) 
    {
        this->cab_id = cab_id;
        this->cmd_id = cmd_id;
        this->data_len = data_len; 
        memcpy(this->data, data, data_len);
    }
} sccp_packet_t;

// Typedef struct for SCCP handler commands
typedef struct sccp_command 
{
    MP handler;
    uint32_t timeout;
} sccp_command_t;

class SCCP 
{
    public: 
        static QueueHandle_t uart_queue;
        static TaskHandle_t handle;
        uint8_t control_flag;
        SCCP();
        uint8_t identify(std::string* result);
        Graph graph;
        void send(sccp_packet_t packet);
        static void receive_loop(void* handle);
        void iack(uint8_t* packet_data);
        void inack(uint8_t* packet_data);
        void ack(uint8_t* packet_data);

    private:
        uint8_t buffer[HEADER_SIZE + DATA_SIZE];
        uint8_t id;

        void initialize();
        void encode(uint8_t* data, sccp_packet_t* packet);
        void decode(uint8_t* data, sccp_packet_t* packet);
        void handle_command();
        uint8_t get_response(sccp_packet_t* response);
};

#endif