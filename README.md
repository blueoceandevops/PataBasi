# Name

BusBabe (.com and .org available)

PataBasi (find the bus in Swahili - .com and .org available)

TrackerJack (.com and .org both for sale with a $200 reserve)

FinderJack (.com is available and cheap)

FleetFollow (or FollowFleet)

BusBouncer (plus TruckBouncer and FleetBouncer all available .com and .org)

BusBitch - available as.com and .org, funny easter egg; make secret alias for BusBabe?

# Overview
An application to keep track of fleets of vehicles or project participants, primarily in low-connectivity environments where Android phones are readily available but more specialized hardware is not easily obtained.

**Consists of**:
- A mobile "Tracker" app that installs on Android devices in or on vehicles, sending positions at specified intervals
- A dashboard (web page or mobile app) to view the positions of the relevant vehicles.
- A back-end server to receive positions at a fixed IP address from the Tracker app.

**Primary public use-case**: allowing people in Africa to know where the bus is.

**Primary private use-case**: allowing NGOs and businesses to track their fleets for safety and coordination.

# Tracker app

This is an Android app that takes and sends the device's position to a central server at particular intervals, using data where/when available and SMS when not. The Android device is intended to be mounted on a vehicle, either permanently (stuck on and wired to the battery) or temporarly (tossed on the dash, and perhaps plugged into a charger in the 12V socket).

### Core Functions
- Takes GPS positions at specificed intervals
- Sends the positions to a server via mobile data or phone number via SMS

### Features
**GPS management**
- Between GPS points, the app can turn the GPS off (save battery) or leave it on (when wired to the battery of the vehicle).
  - Default behaviour: turns off GPS between points when running on battery, leaves it on when connected to a charger
- The interval varies by transmission method; on data more frequent, on SMS less frequent. Both the data and SMS intervals can be set by the user.
  - The fleet manager may in some cases be able to change the intervals; for example an NGO fleet manager may, during a high-security period, wish to decrease the interval of vehicle position sending from 60 to 30 minutes.
  
**Transmission of positions**

The app can either send positions using mobile data to an endpoint on a server, or can send positions by SMS to a phone number. This depends on the availability of mobile data and the type of deployment.

When a server back end is present, as in a deployment providing bus location information to the public, the Tracker application:
- Transmits positions via mobile data whenever possible (the Android device has a functioning data connection).
- Transmits positions via SMS when mobile data is unavailable (out of range or out of paid data).

When there is no server back end, as in a deployment tracking NGO vehicles in an area without network coverage, the Tracker application is configured to send data by SMS only. 

**Position Format**
Data is transmitted in a string format with character counts as follows:

| Field | sign(+/-)| pre-decimal | decimal | post-decimal| delimiter | chars |
|---| ---: | ---: | ---: | ---: | ---: | ---: |
|Lat       |1|2|1|5|1|10|
|Long      |-|3|1|5|1|10|
|Elevation |1|3|-|-|1|5|
|Accuracy  |-|4|-|-|1|5|
|Velocity  |-|3|-|-|1|4|
|Bearing   |-|3|-|-|1|4|
|Timestamp |-|11|-|10|1|22|

The delimiter is a semicolon; commas can cause problems with French-language systems which use commas as decimals.

A full position is up to 65 characters (less if one of the fields doesn't require all of the available space, for example, the latitude maybe just north of the equator, therefore eliminating the need for the +/- sign and one of the two pre-decimal digits). This means a single SMS message - containing a maximum of 140 characters - can always transmit 2 positions, though this is only relevant when transmitting backlogged positions after periods without a working connection.

Example position: ```-12.43245;123.82732;112;11;23;2017-11-20 15:34:22.1```.

# Dashboard

A zoomable viewer with an OpenStreetMap (or Google Satellite) background and icons for each vehicle being tracked. An Android app, which for fleet manager users also allows data syncing to a local web page on a nearby PC.

### Core Functions

- Receive web and SMS positions from various roving trackers and display them overlaid on a base map.
- Zoom in and out to view individual rovers.
- Display one's own location.
- Filter rovers (for example, only look at the buses labelled as running a particular route)

# User Stories

## Middle-Class Urban African Bus Rider

I have:
- A smartphone
- A data connection on that smartphone

I need to:
- Know where the bus that I want to catch is
- Know where I am (I'm not very map literate)

I want to:
- Know where the stop I will use to catch the bus is
  - I may wish to spend more time drinking my coffee before going to the bus stop
- Estimate how long it will be before the bus reaches
 - By seeing the progress visually, or
  - by getting a numerical estimate from my phone
- Know where my stop is (again, I'm not very map literate)
- Estimate how long until my stop
- 

## NGO Fleet Manager in Rural Africa

I have:
- A laptop computer
- A smartphone
- No reliable data (I probably have an internet connection, but it's often down)
- Reasonably reliable SMS coverage at my base
- Patchy SMS coverage where my vehicles travel (but in general the vehicles pass in and out of SMS range at least every hour or two)
- An HF radio that, while clunky, frustrating, and time-consuming to use, almost always can get a connection to my vehicles if necessary

I need to:
- Know where all of my vehicles are at an interval of around one hour (every half-hour in high-security zones/times, every hour in normal circumstances

I want to:
- Set the interval for the vehicle check-ins to whatever suits me from my base (remotely configure the rover apps)
- Know when the vehicle is moving (accelerometers trigger position updates)
- Be alerted if the vehicle goes outside of areas I deem safe (LoJack-style alerts)


## Bus Driver

I have:
- A bus, obviously

I need to:
- Focus on the road, not taking care of the phone. The phone should be:
  - Wired to the battery of the bus,
  - Mounted in such a way as to be protected from theft and the elements,
  - Supplied with credit and/or data with minimal fuss (ideally not by me)

I want to:
- ?


## Low-Income Urban African Bus Rider

I have:
- A dumb phone
- No data (and no device to use data with anyway)
- A little bit of SMS/Voice credit, but not enough to squander

I need:
- To know if and when my bus is coming on my particular route

I want:
- Money, probably...
- Maybe busses that will go closer to my route, on a better schedule for my needs?
