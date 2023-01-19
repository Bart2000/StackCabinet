## sdkconfig Setup
To enable Bluetooth Classic with SPP, we need to make some changes:
- Set correct Bluetooth Controller mode:
`Component config --> Bluetooth --> Bluetooth controller --> Bluetooth controller mode --> BR/EDR Only`
- Limit the number of connected devices:
`Component config --> Bluetooth --> Bluetooth controller --> BR/EDR ACL Max Connections --> 1`
`Component config --> Bluetooth --> Bluedroid Options --> BT/BLE MAX ACL CONNECTIONS --> 1`
- Enable the SPP functionality by choosing the path as following:
`Component config --> Bluetooth --> Bluedroid Options --> SPP`
- Setup legacy pairing:
`Component config --> Bluetooth--> Bluedroid Options --> Secure Simple Pair`