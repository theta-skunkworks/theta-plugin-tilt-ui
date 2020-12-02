# Change via Tilt

Japanese page [here](README_jp.md)

## Overview

With this THETA plug-in, you can change the shooting settings by combining THETA's position (tilt) and button operations.
Most shooting settings can be done without a smartphone.

The settings that can be operated with this plug-in are as follows.

- Change shooting mode (still image/video)
- Change the exposure mode (Auto/Av/Tv/Iso/Manual)
- Change shooting settings for each exposure mode<br>(Aperture, shutter speed, ISO sensitivity, white balance, Option setting in still image auto mode [NR/DR Comp/HDR/ Hh HDR]
- Change the white balance specification method (preset/color temperature)
- Change file format (JPEG / RAW +) when shooting still images
- Change self-timer on/off
- Change continuous shooting mode (Off/Time Shift/Interval/Interval Composite)

When you close the plug-in, the setting change made with the plug-in is saved and will be adopted when the plug-in is restarted.

This plug-in is compatible with remote controllers (HID devices that send out the "Vol +" key code).

Also see the video below.

[![](https://img.youtube.com/vi/5kFv-7Cc3h8/0.jpg)](https://www.youtube.com/watch?v=5kFv-7Cc3h8)


## Tilt operation & Changeable setting 

![Tilt](img/01.PNG)


## Button operation

![Button](img/02.PNG)


## Change the “white balance specification method(Preset/Color temperature)”

![WhiteBalance](img/03.PNG)


## Change the Still Image format

![Format](img/04.PNG)


## OLED

![OLED](img/05.PNG)

- While shooting "Time Shift" and "Interval", "in progress." Is displayed in the dialog.
- While shooting "Interval Composite", the elapsed time is displayed in the dialog.
- During movie recording, the recording time is displayed in the dialog.
- When the exposure program is manual, the brightness of the OLED is adjusted according to the set value.

The dialog display is as follows.

![Dialog](img/00_daialog.gif)


## Constraint

- The display order of Option setting (_filter) is determined by the response of the RICOH THETA API.
- It takes time to finish "Interval" and "Interval Composite". After the end operation, continue shooting about once, but please wait for a while.
- The "Interval" setting is fixed to "Shooting interval = shortest" and "Shooting period = ∞ (= 0)".
- The setting of "Interval Composite" is fixed to "Save in progress = None" and "Shooting time = 24 hours".
- Set the self-timer time from the basic app.


## Development Environment

### Camera
* RICOH THETA Z1 Firmware ver.1.60.1 and above

### SDK/Library
* RICOH THETA Plug-in SDK ver.2.0.10

### Development Software
* Android Studio ver.3.5.3
* gradle ver.5.1.1


## License

```
Copyright 2018 Ricoh Company, Ltd.

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

## Contact
![Contact](img/contact.png)

