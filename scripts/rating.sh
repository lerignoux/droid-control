
# Get metadata
data=DISPLAY=":0" DBUS_SESSION_BUS_ADDRESS="unix:abstract=/tmp/dbus-VtiCpVkNOX,guid=2190ab4179e97e6607b9e48b56f51b54" qdbus org.kde.amarok /Player org.freedesktop.MediaPlayer.GetMetadata

# Get file name:
filename=""

#convert rating
rating=$1


# Set rating:
eyeD3 --set-user-text-frame=FMPS_Rating:$rating $filename


DISPLAY=":0" DBUS_SESSION_BUS_ADDRESS="unix:abstract=/tmp/dbus-VtiCpVkNOX,guid=2190ab4179e97e6607b9e48b56f51b54" notify-send "Rating received $1" -i ~/Downloads/droid_control.png
echo "`date '+%Y-%m-%d.%H:%M:%S'` rating ran" >> control.log
