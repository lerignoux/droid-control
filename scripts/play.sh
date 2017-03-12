DISPLAY=":0" DBUS_SESSION_BUS_ADDRESS="unix:abstract=/tmp/dbus-z0Bcs99wTF,guid=7f60193f58429e5f80c2757356f6c2b6" qdbus org.kde.amarok /Player org.freedesktop.MediaPlayer.PlayPause
echo "`date '+%Y-%m-%d.%H:%M:%S'` playpause ran" >> ~/Projects/droid_control/scripts/logs.log
