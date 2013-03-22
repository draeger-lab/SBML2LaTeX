#!/bin/bash

matchXML=`expr match "$1" '\(.*\.[[xX][mM][lL]\)'`
matchSBML=`expr match "$1" '\(.*\.[[sS][bB][mM][lL]\)'`

if [ "$matchXML" == "$1" ] || [ "$matchSBML" == "$1" ]; then
  echo valid
else
  echo invalid
fi

exit 0
