# droid control
an android remote control to execute script on a remote ssh host

# tldr
1. install the apk  
2. configure your scripts in the droid control settings menu  
3. add your ssh servers in the servers menu and select it  
4. use your ssh remote control to control your host  


# Security
connection to the ssh host is made using a private key and the associated passphrase.  
private key must be located in the android device using the load button in the servers settings menu.  

**Remark:** The private key is not loaded, copied or saved. If you remove or move the private from the device the application will not be able to connect to your host anymore.  

## Library
The [jsch](http://www.jcraft.com/jsch/) java library is used for the ssh connection to the remote host.
current jsch version is 0.1.53


# scripts
scripts can be basically whatever you want to run on your ssh server. you can configure the buttons associated scripts in the settings menu

## Defaults
Some defaults scripts are preconfigured. You need to change them in the application preferences in order to setup your application.

### Volume control
by default the *volume up* and * volume down* binding in the application controls a unbuntu like amixer volume
commands are:
```bash
amixer -q -D pulse sset Master 5%+
amixer -q -D pulse sset Master 5%-
```

### Audio control
The audio remote control is configured to control [quodlibet](https://github.com/quodlibet/quodlibet) audio player


### Video control
The default video remote control is configured to control a [vlc](https://www.videolan.org/vlc/) instance run with the options
```
-I rc --rc-host localhost:1234
```

# contributing
This is my frst android application. Feel free to raise issues, suggest improvements, cleaning, the code is probably far from being perfect.  
if you like the application and want to contribute, motivate me to pursue it's development or simply have too much money feel free to make a donation
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.me/lerignoux)

