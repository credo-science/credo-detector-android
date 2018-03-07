# Client - Server protocol documentation

**Protocol type:** exchange of JSON messages with header and body.

**Communication:** request/response. Android application sends *request* message,
server sends *response* message as reply to the request. 

**Sending request:** as HTTP request to the endpoint in POST body,
encoded as *application/json; charset=utf-8*.

**Sending response:** as HTTP response, encoded as *application/json; charset=utf-8*. 

Example of HTTP request:

```bash
$ curl -v -H "Content-Type: application/json" -X POST -d "@login.json" https://api.credo.science/api/
```

```
POST /api/ HTTP/1.1
Host: api.credo.science
User-Agent: curl/7.47.0
Accept: */*
Content-Type: application/json
Content-Length: 248

{
  "header": {
    "server": "0.90",
    "frame_type": "login",
    "protocol": "1.0",
    "time_stamp": 1506606884264
  },
  "body": {
    "user_info": {
    "email": "test@email.com",
    "name": "John",
    "key": "QWERTY",
    "team": "Avengers"
    }
  }
}
```

Example of HTTP response:
```
HTTP/1.1 200 OK
Date: Wed, 07 Mar 2018 10:50:44 GMT
Server: Apache/2.4.18 (Ubuntu)
Strict-Transport-Security: max-age=63072000; includeSubdomains
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Set-Cookie: PHPSESSID=ek4ck2nh4r13jcl5e3b2jrpu62; path=/
Expires: Thu, 19 Nov 1981 08:52:00 GMT
Cache-Control: no-store, no-cache, must-revalidate
Pragma: no-cache
Vary: Accept-Encoding
Content-Length: 187
Content-Type: text/html; charset=UTF-8
 
{
  "header": {
    "server": "1.00",
    "frame_type": "login",
    "protocol": "1.0",
    "time_stamp": 1520419844
  },
  "body": {
    "user_info": {
      "email": "test@email.com",
      "name": "John",
      "key": null,
      "team": "Avengers"
    }
  }
}

```


## Message communication

Message template:
```json
{
  "header": {
    "server": "0.90",
    "frame_type": "login",
    "protocol": "1.0",
    "time_stamp": 1506606884264
    },
  "body": {
    
  }
}
```

* **header** - message header, contains:
  * **server** - TODO: ???
  * **frame_type** - operation name
  * **protocol** - version of protocol
  * **time_stamp** - UNIX time in ms
* **body** - content of input data for operation 

## Login

**Operation name:** `login`

### Request body


```json
{
  "user_info": {
    "email": "john@mail.com",
    "name": "John",
    "key": "QWERTY",
    "team": "Avengers"
  }
}
```

* **user_info** - user setting
  * **email**: users e-mail
  * **name**: uesers login
  * **key**: login key given on registration
  * **team**: team of device
  
### Response body

```json
{
  "user_info": {
    "email": "john@mail.com",
    "name": "John",
    "key": null,
    "team": "Avengers"
  }
}
```

* **user_info** - see: request
  
## Register

**Operation name:** `register`

### Request body


```json
{
  "user_info": {
    "email": "john@mail.com",
    "name": "John",
    "key": "QWERTY",
    "team": "Avengers"
  },
  "device_info": {
    "deviceId": "",
    "androidVersion": "",
    "deviceModel": ""
  }
}
```

* **user_info** - see `login`
* **device_info** - device information 
  * **deviceId** - device ID from `Settings.Secure.ANDROID_ID`
  * **androidVersion** - android Version as `${Build.VERSION.SDK_INT}-${Build.VERSION.RELEASE}`
  * **deviceModel** - device model from `Build.MODEL`

### Response body

Application checks HTTP return code. When HTTP return code is 200 then ignore the body content.
When HTTP return code is between 400 and 500 then response body is:

```json
{
  "error": "",
  "message": ""
}
```

Otherwise application shows *toast* with network problem message.

## Ping

**Operation name:** `ping`

Response message have the same content with *register* message.

No response checked.

## UserInfo

**Operation name:** `user_data`

Response message have the same content with *register* message.

Never used.

## Detection

**Operation name:** `detection`

### Request body

```json
{
  "detection": {
    "id": 0,
    "frame_content": "",
    "timestamp": 0,
    "latitude": 0.0,
    "longitude": 0.0,
    "altitude": 0.0,
    "accuracy": 0.0,
    "provider": "",
    "width": 0,
    "height": 0
  },
  "user_info": {
    "email": "john@mail.com",
    "name": "John",
    "key": "QWERTY",
    "team": "Avengers"
  },
  "device_info": {
    "deviceId": "",
    "androidVersion": "",
    "deviceModel": ""
  }
}
```

* **detection** - detection hit values
  * **id** - autoincremental value,
  * **frame_content** - cropped PNG encoded as BASE64 with detected hit,
  * **timestamp** - UNIX miliseconds timestamp of hit detection,
  * **latitude**, **longitude**, **altitude**, **accuracy** and **provider** - GPS status,
  * **width** - height of camera frame in pixels,
  * **height** - height of camera frame in pixels
  
### Response body

Application checks only HTTP return code. When is not 200 then store hit in storage
and try send again later.
