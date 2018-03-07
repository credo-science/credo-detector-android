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
    /*[...]*/
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

### Request

**Operation name:** `login`

**Body:**
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
  
### Response

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

## Ping

## UserInfo

## Detection