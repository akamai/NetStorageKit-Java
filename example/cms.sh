#!/bin/sh

java -cp $(dirname $0):$(dirname $0)/../build/classes CMS $*
