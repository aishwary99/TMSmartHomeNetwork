# TM Smart Home Network (Make things go easy)

## Description

TMSmartHomeNetwork is a mini prototype IOT project written in languages incorporating Java , C++ & JQuery without using any third party library & its functions. Our smart home network provides services that automates 
the things we do in our day to day life such as turning on/off switches to toggle electronic units and appliances in our home.

### Features
* Board registration module.
* Electronic unit registstration module.
* Electronic unit toggle module.
* List updation with time intervals to keep user updated with real time changes.
* Our very own TCP/IP socket server to handle HTTP request's.


### Installing

* Install arduino IDE , an enviroment for IOT modules.
* IOT modules and units such as arduino nano , esp 8266-01s module, bread board (for testing) , jumper wires , adapters (to convert AC to DC) and some led's which will represent status.
* Java version 8+ is required to compile java classes.
* C++ version 11+ is required to compile cpp files.
* Latest browser such as Mozilla Firefox is preferred.
* Create a setup which incorporates nano micro controller with esp and having a circuitry driven on AC with the help of adapter.
* Mobile hotstop / wifi router to create an AP through which IP will be broadcasted to System and ESP side.


### Executing program

* First & for most compile all the java files to create server enviroment. 
* Open nano.cpp in arduino ide and upload it in nano.
* Run the server by using the following command on CLI -
```
java Server.java
```
* Open Serial Monitor to capture the working status of ESP module.
* Open the preffered browser and type the following in the address/url bar -
```
<host_name>:<port_number>
```
* Register a board with respective electronic units and toggle them to see real time magic.

## Help

* Debug the circuitry using LED's to check whether the setup is working properly or not.
* Try not to send unnecessary request's on server side.
* Track the status when things not working at your end.


## Version History
* 0.2
    * Updated GUI and resolved minor bugs
* 0.1
    * Initial Release

## Acknowledgments

* https://github.com/google/gson
