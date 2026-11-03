<!--
SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
SPDX-License-Identifier: CC-BY-4.0
-->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-11-03

### Changed

- Updated the OPTIGA™ Authenticate NBT Host Library for Java to v1.1.3, it is now a Git submodule.
- Updated the Android Gradle plugin to 9.4.1 and Gradle to 9.8.0.
- Updated the README to link to the product page, the Java library and the OPTIGA™ Authenticate NBT host software overview, and made minor documentation updates.

### Fixed

- Version 1.0.1 can be installed over version 1.0.0. The `versionCode` of 1.0.0 was higher than the one of this version, so Android refused the update, the `versionCode` is the release date now.

## [1.0.0] - 2026-07-15

### Added

- Initial release for wifi p2p.
