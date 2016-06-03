#!/bin/bash
FILES=~/.dbus/session-bus/*
for f in $FILES
do
  echo "processing file $f"
  BUS_ADDR=$(grep -Po '(?<=DBUS_SESSION_BUS_ADDRESS=).+' $f)
  # take action on each file. $f store current file name
  echo "dbus bus Address found: $BUS_ADDR"
  RES="DISPLAY=\":0\" DBUS_SESSION_BUS_ADDRESS=$BUS_ADDR qdbus org.kde.amarok /Player org.freedesktop.MediaPlayer.Stop"
  eval $RES
done
