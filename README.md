<!--
SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
SPDX-License-Identifier: CC-BY-4.0
-->

# OPTIGA™ Authenticate NBT Wi-Fi Direct Demo App for Android

[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)
[![REUSE](https://github.com/Infineon/optiga-nbt-example-wifip2p-android/actions/workflows/reuse-lint.yml/badge.svg?branch=main)](https://github.com/Infineon/optiga-nbt-example-wifip2p-android/actions/workflows/reuse-lint.yml)
[![Android Build](https://github.com/Infineon/optiga-nbt-example-wifip2p-android/actions/workflows/android-build.yml/badge.svg?branch=main)](https://github.com/Infineon/optiga-nbt-example-wifip2p-android/actions/workflows/android-build.yml)

This mobile phone app is a Java-based *Android Studio* project which demonstrates the establishment of a Wi-Fi Direct connection using the [OPTIGA™ Authenticate NBT](https://www.infineon.com/part/optiga-authenticate-nbt).

## Overview

The proof-of-concept demo application enables an Android mobile phone to establish a Wi-Fi Direct connection with a Raspberry Pi, with an attached OPTIGA™ Authenticate NBT shield.

This mobile phone app is intended to be used in combination with
the [Wi-Fi Direct Demo Application for Raspberry Pi](https://github.com/Infineon/optiga-nbt-example-wifip2p-rpi)
to demonstrate the OPTIGA™ Authenticate NBT's Wi-Fi Direct functionality.

Refer to the [OPTIGA™ Authenticate NBT: Host software overview](https://github.com/Infineon/optiga-nbt) repository for an overview of the available host software for the OPTIGA™ Authenticate NBT.

### Features

- Android application with user interface and NFC communication capability
- Example implementation to establish a Wi-Fi Direct connection
- Sample usage of the [OPTIGA™ Authenticate NBT Host Library for Java](https://github.com/Infineon/optiga-nbt-lib-java) (ref v1.1.3)

## Getting started

Information on how to setup this Android mobile phone app.
For information to run the full Wi-Fi Direct demo, refer to the [Wi-Fi Direct Demo Application for Raspberry Pi](https://github.com/Infineon/optiga-nbt-example-wifip2p-rpi).

### Requirements

- NFC-enabled Android device running on API level 33 or higher (Android 13+)
  - Tested with Google Pixel 9a/9 Pro (Android 16) and Samsung Galaxy A52 (Android 14)
- Android Studio to build the project and generate the APK
  - Tested with Android Studio Rabbit 2 | 2026.2.2

### Setup

This project is intended to be loaded and compiled using *Android Studio*.

The project uses a Git submodule (the OPTIGA™ Authenticate NBT Host Library for Java), so downloading a ZIP archive of the repository is not sufficient. Clone the repository together with its submodule:

```sh
git clone --recurse-submodules <repository-url>
```

If the repository is already cloned, initialize the submodule with:

```sh
git submodule update --init --recursive
```

- Then import the project into *Android Studio* (by selecting File --> Open).
- After building, the mobile phone app needs to be installed on the mobile phone.
  - For installing Android applications, it is recommended to use *Android Studio*, with the mobile phone set to developer mode.
  - Alternatively, generate the `.apk` file in *Android Studio* and transfer/install it manually to the mobile phone.

### Usage

After installation, the Android app can be started and used to run the demo.
To make sure the mobile phone app works as intended, NFC and location permissions need to be enabled on the Android device.

See [Wi-Fi Direct Demo Application for Raspberry Pi](https://github.com/Infineon/optiga-nbt-example-wifip2p-rpi) for instruction on how to run the full proof-of-concept demo.

## Additional information

This section lists additional resources and information related to this repository.

### Contact

In case of questions regarding this repository and its contents, refer to [MAINTAINERS.md](MAINTAINERS.md) for the contact details of this project's maintainers.

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution instructions and refer to our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

### Licensing

Please see our [LICENSE](LICENSE) for copyright and license information.

This project follows the [REUSE](https://reuse.software/) approach, so copyright and licensing information is
available for every file (including third party components) either in the file header, an individual *.license file or
a REUSE.toml file. All licenses can be found in the [LICENSES](LICENSES) folder.

### Related resources

- [OPTIGA™ Authenticate NBT: Host software overview](https://github.com/Infineon/optiga-nbt)
- [OPTIGA™ Authenticate NBT: Host software documentation](https://infineon.github.io/optiga-nbt)
- [OPTIGA™ Authenticate NBT: Product page](https://www.infineon.com/part/optiga-authenticate-nbt)
- [OPTIGA™ Authenticate NBT: Wi-Fi Direct Demo Application for Raspberry Pi](https://github.com/Infineon/optiga-nbt-example-wifip2p-rpi)
