# Introduction

BeaconTracker Android is a ResearchStack application that helps users track bathroom-use frequency and duration. It is compatible with beacons that support the iBeacon, EddyStone, or AltBeacon specifications. Activated bluetooth beacons are intended to be placed near the toilets of bathrooms that the user commonly visits. The app automatically scans for beacons in the background after it is installed and opened.  

The BeaconTracker Android was created as part of a summer internship at the Small Data Lab at Cornell Tech. 

# Configuration

The frequency of bluetooth scans and interval between scans can be changed by modifying following variables in MonitoringService. Values represent time in milliseconds.

```
private static final long LOW_SCAN_TIME = 10L * 1000L;
private static final long LOW_SCAN_INTERVAL = 2L * 60L * 1000L;
private static final long MEDIUM_SCAN_TIME = 2L * 1000L;
private static final long MEDIUM_SCAN_INTERVAL = 30L * 1000L;
private static final long HIGH_SCAN_TIME = 2L * 1000L;
private static final long HIGH_SCAN_INTERVAL = 0L;
```

The minimum time that the phone needs to spend within the "bathroom-use" range to be logged can be set modifying following variable in MonitoringService.

```
private static final long MINIMUM_EPISODE_DURATION = 60L * 1000L;
```

### Third-party Library Disclosures

<b>com.android.support:appcompat-v7</b>

- Used to theme and style views within the app.

<b>org.altbeacon:android-beacon-library</b>

- Used to register and detect bluetooth beacons.

<b>com.android.support:multidex</b>

- The Android MultiDex support library enables us to go past the default 65K method limit for an android project.

<b>co.touchlab.squeaky:squeaky-processor</b>

-  Annotation processor for the Squeaky ORMLite database library. The library creates auto-generated code at compile time for our database pojos (see Mole or Measurement classes)

<b>
junit:junit:4.12<br>
com.madgag.spongycastle
</b>

-  Used for unit testing.

# App Content Attribution

This app is based on and uses resources from the ResearchStack [SampleApp](https://github.com/ResearchStack/SampleApp). 

# License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```