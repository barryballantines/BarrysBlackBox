---
title: "Getting Started"
layout: default
---

## Getting Started with Barry's BlackBox

### Prerequisites

This is what you need to run Barry's BlackBox:

- Java 8 installation
- FlightGear
- the ***blackbox-1.0.jar*** file.
- the protocol ***blackbox.xml***

Check [Releases](https://github.com/barryballantines/BarrysBlackBox/releases) for latest versions.

### Preparing FlightGear:

1. Download the protocol definition file [blackbox.xml](https://raw.githubusercontent.com/barryballantines/BarrysBlackBox/master/src/main/resources/Protocol/blackbox.xml) and put it into your flightgear protocol directory at `${FGHOME}/data/Protocol/`
2. Start Flightgear with the following commandline options:
  
 `--httpd=5500 --generic=socket,out,2,localhost,5555,udp,blackbox`. 

If you are using FGRun for starting Flightgear, you can define these parameters in the "Advanced Options" dialog. 
The port for the httpd server (`--httpd=5500`) is defined in the Network section, the generic protocol can be set 
in the Input/Output section.

> **Description of the FlightGear Commandline Options**
> 
> The option `--httpd=5500` starts the internal FG webserver on port 5500. This server is used by Barry's BlackBox > in order to receive data on "Start" and "Shutdown" and it is also used by the "Parking" panel for relocating the
> aircraft to the latest stored parking position of the current airport.
>
> The option `--generic=socket,out,2,localhost,5555,udp,blackbox`option tells FG to send UDP requests twice a second
> to a UDP server running on localhost and listening on port 5555. The port and host can be changed, if you want 
> to run your blackbox on separate maschine. The format of the UDP packets is described in
> the protocol definition file `blackbox.xml`, which you need to copy to your FG installation.

 
### Configuring Barry's Blackbox

Double-click the blackbox-<version>.jar or start it from the commandline using the following command 
`java -jar blackbox-<version>.jar`. (Replace <version> with the version you are using).

Open the 'Configuration' tab and press the "Test Connection" button in the Flightgear HTTPD section. 
If your Flightgear is running, you should see a green "Connection successful" message. If not, check your
Flightgear configuration.

Check the checkbox in the UDP-Server section. This will start the internal UDP Server which receives the flight 
data from flightgear. To check, if the UDP server is working, change to the "Parking" tab. If FlightGear is already 
running you should see the ICAO code of the closest airport. If you do not see anything, there is propably a problem 
with the port for the UDP server (5555), maybe this port is already in use... you can change the port in the 
Configuration tab, and of course also in the `--generic` configuration of Flightgear.

If you are flying for a Virtual Airline which supports the ***kACARS*** protocol, you can configure the kACARS URL and
your pilot credentions in the *kACARS* section of the configuration tab. This section also has a checkbox to disable 
the ACARS functionality completely.

You will also find a checkbox for activating the ***kACARS Live Update*** which sends flight data to the VA website in 
with a predefined frequency. (the default is 30 seconds).

Test your ACARS connection with the test button.

### Starting your flight

Taxi to a gate, load some passengers and fuel and enter your route in the Flightgear route manager. When ready 
for engine startup, open the "Overview" tab and press the "Start-Up" button. The ICAO code of your airport, the 
current time (UTC) and the current fuel level will appear in the Destination column of your PIREP form.

The recording will start as soon as your aircraft starts moving.

### Cruising

When using the route manager/autopilot in flightgear you can see the progress of your flight on the "Route" tab of 
Barry's BlackBox. This tab will also give you the estimated time of arrival (ETA) and the estimated time enroute 
(ETE), based on your route and your current ground speed.

### Finishing your flight.

After landing at your destination airport taxi to your gate, shutdown your engines and press the "Shut-Down" button 
on the "Overview" tab. The form will show you the consumed fuel and the flight time as well as the landing rate in 
feets per minutes.

### Submitting your PIREP

Open the "PIREP" tab. Here you will see all data which have been tracked during your flight as well as some data that
has been downloaded from your Virtual Airlines flight bids. You can change some data here, for example the number of 
PAX or some comments. Finally press the "Submit PIREP" button. It will take a few seconds, and Barry's BlackBox will 
(hopefully) report a successful PIREP submission (Or an error, some Virtual Airlines servers might not respond in time,
so that Barry's BlackBox will receive a Timeout, which is shown as an error. If this is the case, please check the 
VA webpage to check, if the PIREP has been submitted.)
