ARG BASE_IMAGE=eclipse-temurin:21-jre
FROM ${BASE_IMAGE}

LABEL maintainer="zippyboot-netty"

RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY zippyboot-netty-*.jar /zippyboot-netty.jar
COPY bootstrap.yml /bootstrap.yml
COPY start.sh /start.sh
RUN chmod 755 /start.sh
ENTRYPOINT ["/start.sh"]
