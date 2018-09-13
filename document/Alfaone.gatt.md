
# AlfaOne gatt sensor profile
  
|Service|Characteristic|Permission|Description|
|:---:|:---:|:---:|:---:|
|79430001-F6C2-09A3-E9F9-128ABCA31297|-|Read, Write|Sensor gatt service|
|-|79430002-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Foot-pressure sensor data|
|-|79430003-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Acceleration sensor data|
|-|79430004-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Gyro sensor data|
|-|79430005-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Compass sensor data|
|-|79430006-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Quaternion sensor data|
|-|79430007-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Euler sensor data|
|-|79430008-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Lineal acceleration sensor data|
|-|79430009-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Gravity Vector data|
|-|7943000A-F6C2-09A3-E9F9-128ABCA31297|Read, Write, Notify|Heading data|
|-|7943000B-F6C2-09A3-E9F9-128ABCA31297|Write, Write without response|Notification control pointer
  
  
## After connect to alfaone device and discoveried gatt profile...
  
- Enable characteristics notification switch of sensor which you want(0x0002~0x000A).  
  
- Write a 9 bytes(Fixed length) command to the control pointer(0x000B). If first byte is 0x01 means Foot-pressure notification will be allowed, on the contrary, notification won't be allowed.
  
  
#### Notic: Both characteristic's notification and control pointer must be setup, or you won't get the sensor data.

## Sensor data parsing

Refer to [parsePressure function](./app/src/main/java/com/alfaloop/insoleble/ble/support/device/Alfaone.java)  
  
Refer to [parseAccel function](./app/src/main/java/com/alfaloop/insoleble/ble/support/device/Alfaone.java)  
  
Refer to [parseGyro function](./app/src/main/java/com/alfaloop/insoleble/ble/support/device/Alfaone.java)  
  
  