# DLMS Mandjet Server

## Env Config Example
``` shell
# Endpoint for the Notificaion pushes
export CLIENT_ENDPOINT=localhost:4060
# Sets the password for the supporting logical device
export DLMS_SUPPORTING_LD_PASSWORD=password
# Mandjet API details
export MANDJET_ENDPOINT=https://emoncms.fr
export MANDJET_READ_API_KEY=XXXX
```
## Run

```shell
mvn exec:java -Ptcpip
mvn exec:java -Pe5lora
```