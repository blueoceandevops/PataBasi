# Name

PataBasi (find the bus in Swahili - .com and .org available)

TrackerJack (.com and .org both for sale with a $200 reserve)

FinderJack (.com is available and cheap)

FleetFollow (or FollowFleet)

BusBouncer (plus TruckBouncer and FleetBouncer all available .com and .org)

# Overview
An application to keep track of fleets of vehicles or project participants, primarily in low-connectivity environments where Android phones are readily available but more specialized hardware is not easily obtained.

**Consists of**:
- A mobile "Tracker" app that installs on Android devices in or on vehicles, sending positions at specified intervals
- A dashboard (web page or mobile app) to view the positions of the relevant vehicles.
- A back-end apparatus to receive positions at a fixed IP address from the Tracker app.

**Primary public use-case**: allowing people in Africa to know where the bus is.

**Primary private use-case**: allowing NGOs and businesses to track their fleets for safety and coordination.

# Tracker app

This is an Android app that takes and sends the device's position at particular intervals, using data where/when available and SMS when not. The Android device is intended to be mounted on a vehicle, either permanently (stuck on and wired to the battery) or temporarly (tossed on the dash, and perhaps plugged into a charger in the 12V socket).

### Core Functions
- Takes GPS positions at specificed intervals
- Sends the positions via mobile data or SMS

### Features
**GPS management**
- Between GPS points, the app can turn the GPS off (save battery) or leave it on (when wired to the battery of the vehicle).
  - Default behaviour: turns off GPS between points when running on battery, leaves it on when connected to a charger
- The interval varies by transmission method; on data more frequent, on SMS less frequent. Both the data and SMS intervals can be set by the user.
  - The fleet manager may in some cases be able to change the intervals remotely; for example an NGO fleet manager may, during a high-security period, wish to decrease the interval of vehicle position sending from 60 to 30 minutes.

**Phone credit management**
- Fleet managers may wish to know and/or top up mobile credit on selected phones. This should probably be a standalone app, as more people are likely to want this functionality than are served by the fleet tracker application.
  
**Transmission of positions**

The app can either send positions using mobile data to a particular IP address, or can send positions by SMS to a phone number. This depends on the availability of mobile data and the type of deployment.

When a server back end is present, as in a deployment providing bus location information to the public, the Tracker application:
- Transmits positions via mobile data whenever possible (the Android device has a functioning data connection).
- Transmits positions via SMS when mobile data is unavailable (out of range or out of paid data).

When there is no server back end, as in a deployment tracking NGO vehicles in an area without network coverage, the Tracker application is configured to send data by SMS only. 

**Position Format**
Data is transmitted in a string format with character counts as follows:

| Field | sign(+/-)| pre-decimal | decimal | post-decimal| delimiter | chars | example |
|---| ---: | ---: | ---: | ---: | ---: | ---: |---|
|Lat       |1|2|1|5|1|10|-12.43245|
|Long      |-|3|1|5|1|9|123.82732|
|Elevation |1|3|-|-|1|5|112|
|Accuracy  |-|4|-|-|1|5|11|
|Velocity  |-|3|-|-|1|4|23|
|Bearing   |-|3|-|-|1|4|359|
|Timestamp |-|10|-|-|1|11|010704120856|

The delimiter is a semicolon; commas can cause problems with French-language systems which use commas as decimals.

The date is a Java SimpleDateFormat string; the example above, 010704120856, stands for 2001-07-04 12:08:56.

A full position is up to 64 characters (less if one of the fields doesn't require all of the available space, for example, the latitude may be just north of the equator, therefore eliminating the need for the +/- sign and one of the two pre-decimal digits). This means a single SMS message - containing a maximum of 140 characters - can always transmit 2 positions, though this is only relevant when transmitting backlogged positions after periods without a working connection.

Example position: ```-12.43245;123.82732;112;11;23;359;010704120856```.

# Dashboard

A zoomable viewer with an OpenStreetMap (or Google Satellite) background and icons for each vehicle being tracked. An Android app, which for fleet manager users also allows data syncing to a local web page on a nearby PC.

### Core Functions

- Receive web and SMS positions from various roving trackers and display them overlaid on a base map.
- Zoom in and out to view individual rovers.
- Display one's own location.
- Filter rovers (for example, only look at the buses labelled as running a particular route)

## Registration

The process of registering a new phone and vehicle may be as follows:

- The Fleet Manager procures a phone, adds a SIM card, SMS credit, and (optionally) mobile data credit to it.
- She then texts a particular token ("register", for example) to the phone number of the particular instance.
  - Optionally: she uses a QR code supplied by the desktop application, which contains the appropriate phone number and URL. causing the phone to SMS a registration token to the instance.
- The instance generates a UUID for that phone, and displays a pop-up requesting a label for it (i.e. the vehicle number, the name of the person who will be carrying the phone, or whatever - labels can be changed in the event that the phone needs to be re-allocated to someone or something else). The UUID is sent to the phone along with the label and any other configuration information.
- From that moment on, all messages from that SMS number will be associated with that UUID, and all mobile data position transmissions will include the UUID.

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
