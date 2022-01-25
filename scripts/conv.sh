#!/bin/sh

if [ -z $2 ];then echo usage: $0 "<in> <out>";exit;fi

mkdir -p res/drawable res/drawable-hdpi res/drawable-xhdpi res/drawable-xxhdpi res/drawable-xxxhdpi

convert $1 -resize 48x48 res/drawable/$2
convert $1 -resize 72x72 res/drawable-hdpi/$2
convert $1 -resize 96x96 res/drawable-xhdpi/$2
convert $1 -resize 144x144 res/drawable-xxhdpi/$2
convert $1 -resize 192x192 res/drawable-xxxhdpi/$2
