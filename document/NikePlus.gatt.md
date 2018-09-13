
# NikePlus gatt sensor profile
  
|Service|Characteristic|Permission|Description|
|:---:|:---:|:---:|:---:|
|F1D30565-C50E-C833-E608-F2D491A8928C|-|Read, Write|Major gatt service|
|-|F1FE195A-9067-2481-848D-3D7CF2A45191|Write without response|Control pointer|
|-|F230DF48-9067-2481-848D-BB04B2B0F6C0|Notify|Connection challenge pointer|
|-|F230DF4D-D97D-66B3-9173-B185EEFE84FE|Notify|-|
|-|F230DF4F-B00B-ED7D-92C7-A4C6D31444E3|Notify|Sensor data(Pressure and acceleration)|
|-|F230DF58-D1B0-8EDD-3327-28B3C5A7BAC3|Notify|-
  
  
## After connect to NikePlus device and discoveried gatt profile...
  
- Enable characteristic sensor data(0xDF48) and control pointer(0xDF4F) notification switch.
  
- Start connection challenge. Write(without response) a 19 bytes(Fixed length) command to control pointer(0x195A), first byte is 0x0C, others are 0x00.  
	{0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
	 
  
- Then you'll receive a challenge code from connection challenge pointer(0xDF48), decode auth command as  
	{0x0C, 0x00, 0x01, notifyData[10], notifyData[6] ^ 0x78, notifyData[4] ^ 0x9F, notifyData[8] ^ 0xB9, notifyData[11] ^ 0x20, ~notifyData[5], notifyData[7] ^ 0xAA, notifyData[9] ^ 0x55}
  
- Write following init commands:  
	{0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}  
	{0x10, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}  
	{0x0B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
  
- Finally, set sampling rate and start sensor data notification with follwing command:  
	RATE_8_HZ = 0x43, RATE_16_HZ = 0x54  
    RATE_32_HZ = 0x63, RATE_64_HZ = 0x72  
	{0x02, 0x00, 0x01, RATE_?_HZ, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
  
- Sensor data(0xDF4F) start notify raw data to your central device.
  
  
## Sensor data parsing
  
Refer to [parsePressure function](./app/src/main/java/com/alfaloop/insoleble/ble/support/device/NikePlus.java)  

Refer to [parseAccel function](./app/src/main/java/com/alfaloop/insoleble/ble/support/device/NikePlus.java)  
  
  