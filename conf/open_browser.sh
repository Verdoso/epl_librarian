#!/bin/bash

if [ -z $1 ]; then
  echo "Debes indicar la URL a abrir con el navegador"
  exit 1
else
  URL=$1
fi

[[ -x $BROWSER ]] && exec "$BROWSER" "$URL"
path=$(which xdg-open || which gnome-open || which x-www-browser) && exec "$path" "$URL"
if open -Ra "safari" ; then
  echo "VERIFIED: 'Safari' is installed, opening browser..."
  open -a safari "$URL"
else
  echo "Can't find any browser"
fi