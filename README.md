Calimero-core [![Build Status](https://travis-ci.org/calimero-project/calimero-core.svg?branch=master)](https://travis-ci.org/calimero-project/calimero-core)
=============

The calimero-core library provides KNX communication protocols, KNX datapoint & property access, and management functionality. The minimum required runtime environment is Java SE 9 (module _java.base_).
Calimero is developed with a focus on applications that run on embedded devices and require a small footprint.

Code examples for using this library are shown in [introduction](https://github.com/calimero-project/introduction).

Download
--------

~~~ sh
# Either using git
$ git clone https://github.com/calimero-project/calimero-core.git

# Or using hub
$ hub clone calimero-project/calimero-core
~~~

Supported Features
--------

### Access Protocols
* KNX IP Secure Multicast (experimental)
* KNXnet/IP
    * Discovery and Self-description
    * Tunneling
    * Routing
    * Busmonitor
    * Device Management
* KNX IP
* KNX RF USB
* KNX USB
* KNX FT1.2 Protocol (serial connections)
* TP-UART (access TP1 networks over serial connections)

### Process Communication
* DPT encoding/decoding of Java/KNX data types
* Process Communicator client
* Group Monitor

#### Supported Datapoint Types (DPTs)
* 1.x - Boolean, e.g., Switch, Alarm
* 2.x - Boolean controlled, e.g., Switch Controlled, Enable Controlled
* 3.x - 3 Bit controlled, e.g., Dimming, Blinds
* 5.x - 8 Bit unsigned value, e.g., Scaling, Tariff information
* 6.x - 8 Bit signed value, e.g., Percent (8 Bit), Status with mode
* 7.x - 2 octet unsigned value, e.g., Unsigned count, Time period
* 9.x - 2 octet float value, e.g., Temperature, Humidity
* 10.x - Time
* 11.x - Date
* 12.x - 4 octet unsigned value
* 13.x - 4 octet signed value, e.g., Counter pulses, Active Energy
* 14.x - 4 octet float value, e.g., Acceleration, Electric charge
* 16.x - String, e.g., ASCII string, ISO-8859-1 string (Latin 1)
* 17.x - Scene number
* 18.x - Scene control
* 19.x - Date with time
* 20.x - 8 Bit enumeration, e.g., Occupancy Mode, Blinds Control Mode
* 21.x - Bit array of length 8, e.g., General Status, Room Heating Controller Status
* 28.x - UTF-8 string
* 29.x - 64 Bit signed value, e.g., Active Energy, Apparent energy
* 229.001 - M-Bus metering value, with the various M-Bus VIF/VIFE codings
* 232.x - RGB color value

### Network Monitor / Busmonitor
Access via KNXnet/IP, KNX USB, KNX RF USB, TP-UART, and FT1.2

#### Raw Frame Decoding
* TP1
* KNX IP
* PL110
* RF

### Management
* KNX Management Layer
* KNX Management Procedures
* cEMI Local Device Management

### EMI Support
* cEMI standard and extend L-Data
* cEMI Busmonitor 
* cEMI Device Management
* EMI1/2 standard L-Data 
* EMI1/2 Busmonitor

### Network Buffer
* State/command-based datapoint buffer to answer .reqs, buffer incoming .ind updates


Logging
-------

Calimero uses the [Simple Logging Facade for Java (slf4j)](http://www.slf4j.org/). Bind any desired logging frameworks of your choice. The default maven dependency is the [Simple Logger](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html). It logs everything to standard output. The simple logger can be configured via the file `simplelogger.properties`, JVM system properties, or `java` command line options, e.g., `-Dorg.slf4j.simpleLogger.defaultLogLevel=warn`.

Testing
-------

For unit tests, Calimero provides a [test network](https://github.com/calimero-project/calimero-testnetwork), consisting of a [KNXnet/IP server](https://github.com/calimero-project/calimero-server) and a virtual KNX network with two [KNX devices](https://github.com/calimero-project/calimero-device). The complete test network is implemented in software, and can be executed in any J2SE runtime environment. It provides the remote KNXnet/IP endpoint for executing unit tests for KNXnet/IP tunneling, busmonitoring, routing, device management, and KNX IP protocols. The same setup is used for Calimero Travis CI.

Before running any KNXnet/IP or KNX IP tests, start the test network (`gradle run`)
in the directory _calimero-core/test/testnetwork-launcher_. When using Gradle, KNXnet/IP tests can be excluded via `junitPlatform.filters.exclude 'knxnetip'`.

Currently, the TP-UART and FT1.2 protocols can only be tested if the corresponding hardware is available. 


More Features, Tools, Examples
------------------------------

* [introduction](https://github.com/calimero-project/introduction) provides code examples for programming with Calimero.

* [calimero-tools](https://github.com/calimero-project/calimero-tools) offers command-line tools for KNX process communication, monitoring, and management.

* [calimero-gui](https://github.com/calimero-project/calimero-gui) provides a graphical user interface (based on SWT) for process communication, monitoring, and management.

* [calimero-server](https://github.com/calimero-project/calimero-server) is the Calimero KNXnet/IP Server, providing KNXnet/IP access to KNX networks.

* [calimero-device](https://github.com/calimero-project/calimero-device) is the communication stack to implement a KNX device.

* [serial-native](https://github.com/calimero-project/serial-native) provides native libraries for serial port access (using JNI) on Windows, Linux, and MacOS.

* [import-ets-xml](https://github.com/calimero-project/import-ets-xml) allows importing ETS XML KNX datapoints for use with Calimero.
