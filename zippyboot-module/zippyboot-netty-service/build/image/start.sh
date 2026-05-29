#!/bin/bash
set -e

exec java ${JAVA_OPTS} -jar /zippyboot-netty.jar --spring.config.additional-location=file:/bootstrap.yml