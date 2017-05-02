FROM docker-reg.sirmaplatform.com/openjdk-jre-headless:8

ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_NONINTERACTIVE_SEEN true

RUN echo 'deb http://ftp.debian.org/debian jessie main' >> /etc/apt/sources.list && \
  apt-get update && \
  # Docker \
  apt-get install -y apt-transport-https && \
  apt-get purge docker.io* && \
  apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D && \
  echo "deb https://apt.dockerproject.org/repo debian-jessie main" >> /etc/apt/sources.list.d/docker.list && \
  apt-get update && \
  # harcode docker version because every time the image is build, the hosts has to be updated too \
  apt-get install -y docker-engine=1.8.2-0~jessie && \
  # git is required for sonar analysis \
  apt-get install -y --no-install-recommends curl libfontconfig1 build-essential git xvfb wget && \
  #nodejs - source: https://github.com/nodesource/docker-node/blob/master/debian/jessie/node/5.1.1/Dockerfile \
  apt-get install -y rlwrap && \
  curl https://deb.nodesource.com/node_5.x/pool/main/n/nodejs/nodejs_5.1.1-1nodesource1~jessie1_amd64.deb > node.deb && \
  dpkg -i node.deb && \
  rm node.deb && \
  npm install -g pangyp && \
  ln -s $(which pangyp) $(dirname $(which pangyp))/node-gyp && \
  npm cache clear && \
  node-gyp configure || echo "" && \
  npm install -g gulp && \
  npm install -g jspm && \
  jspm config registries.github.auth c2lybWFpdHQtanNwbToxVFRQQHN3MHJk && \
  # Chrome \
  wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
  echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
  apt-get update -qqy && \
  apt-get -qqy install google-chrome-stable && \
  rm /etc/apt/sources.list.d/google-chrome.list && \
  rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
  apt-get clean

ADD entry-point.sh /
RUN chmod +x *.sh

ENV UNIT_TEST_BROWSER Chrome_without_sandbox

# xvfb configs
ENV SCREEN_WIDTH 1360
ENV SCREEN_HEIGHT 1020
ENV SCREEN_DEPTH 24
ENV DISPLAY :99.0

# Following line fixes
# https://github.com/SeleniumHQ/docker-selenium/issues/87
ENV DBUS_SESSION_BUS_ADDRESS=/dev/null

ENTRYPOINT ["/entry-point.sh"]