#!/bin/sh

export GEOMETRY="10""x""10""x""24"
export DISPLAY=:99.0

Xvfb $DISPLAY -screen 0 $GEOMETRY -extension RANDR & node index.js
