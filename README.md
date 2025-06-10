BinEd - Hex Editor
==================

Editor for binary data (hex viewer/editor) written in Java.

Homepage: https://bined.exbin.org/android  

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/org.exbin.bined.editor.android/)[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=org.exbin.bined.editor.android)[<img src="https://github.com/user-attachments/assets/713d71c5-3dec-4ec4-a3f2-8d28d025a9c6" alt="Get it on Obtainium" height="80">](http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22org.exbin.bined.editor.android%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fexbin%2Fbined-android%22%2C%22author%22%3A%22ExBin%20Project%22%2C%22name%22%3A%22BinEd%22%7D)[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyDroid" height="80">](https://android.izzysoft.de/repo/apk/org.exbin.bined.editor.android)

Or download the latest APK from the [Releases Section](https://github.com/exbin/bined-android/releases/latest).

Screenshot
----------

![BinEd-Editor Screenshot](images/editor_screenshot.png?raw=true)

Features
--------

  * Visualize data as numerical (hexadecimal) codes and text representation
  * Codes can be also binary, octal or decimal
  * Support for Unicode, UTF-8 and other charsets
  * Insert and overwrite edit modes
  * TODO: Searching for text / hexadecimal code with matching highlighting
  * Support for undo/redo
  * Support for files with size up to exabytes (in partial file mode)

Compiling
---------

To compile project and build apk you need installed Java JDK 17 or later and run:

    ./gradlew assembleRelease

or on Windows:

    gradlew.bat assembleRelease

To modify this project, Android Studio is recommended.  
https://developer.android.com/studio/  

When using Android Studio modify following line in build.gradle file to avoid legacy errors:

    def playStore = true

License
-------

Apache License, Version 2.0 - see LICENSE.txt  

