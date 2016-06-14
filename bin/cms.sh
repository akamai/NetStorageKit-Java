#!/bin/sh

java -cp $(dirname $0):$(dirname $0)/../target/classes com.akamai.netstorage.cli.CMS $*
