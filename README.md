# Change via Tilt

Japanese page [here](README_jp.md)

## Overview

With this THETA plug-in, you can change the shooting settings by combining THETA's posture (tilt) and button operations.
Most shooting settings can be done without a smartphone.

The items that can be operated with this plug-in are as follows.

- Switching shooting mode (still image/video)
- Switching the exposure mode (Auto/Av/Tv/Iso/Manual)
- Shooting settings for each exposure mode <br> (Aperture, shutter speed, ISO sensitivity, white balance, Option setting in still image auto mode [NR/DR Comp/HDR/ Hh HDR])
- Switching the white balance specification method (preset/color temperature)
- Switching file storage format (JPEG / RAW +) when shooting still images
- Switching self-timer on/off 
- Switching continuous shooting (Off/Time Shift/Interval/Interval Composite)

The settings made with this plugin will be saved when the plugin is closed and will be restored when the plugin is started again.

This plug-in is compatible with remote controllers (HID devices that send out the "Vol +" key code).

See also the video below.

[![](http://img.youtube.com/vi/wF3f3BWbe4M/0.jpg)](http://www.youtube.com/watch?v=wF3f3BWbe4M "")


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

