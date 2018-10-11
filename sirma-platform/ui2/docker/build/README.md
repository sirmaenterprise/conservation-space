Image for docker container containing all the tools required to build the web application.

#Configurations:
- The root of the web application has to mounted to /seip using volume.
- The docker sock has to be passed in order to use the host's docker daemon.
- The version of the web app has to be maintained in packages.json
- There is an option to provide pre-cached node_modules and jspm_packages directory in order to speed up the build
  This can be done by mounting the cache directories to /node_modules and /jspm_packages. Both cache directories can
  be used separately.
- Sonar configurations - SONAR_HOST=<url to the sonarqube server> and SONAR_BRANCH_NAME=<name of the branch>
- Integration tests:
   To run in parallel use env var SELENIUM_THREADS=<thread count>
   To run on remote selenium grid use SELENIUM_ADDRESS=<url to the grid>. Warning: when the build is performed inside
   a docker container, the http server hosting the web app is inside the container and its port should be forwarded
   to the host. When the forwarding is performed, selenium should know on which url to find the app. The env var
   HOST_ADDRESS=<ip:port on the host> (I.e. 10.1.1.1:10070) should be provided.
- Using npm proxy for faster npm install - use NPM_PROXY to provide a proxy url to be used for npm install
- --shm-size="512m" this prevents internal chrome from hanging

#Usage:
```
docker run -it --rm -e HOST_ADDRESS=<ip of the host>:<host port fordarded to 7000 of the container> \
-e SELENIUM_ADDRESS=<selenium hub url> -e SELENIUM_THREADS=<thread count> \
-e SONAR_BRANCH_NAME=<branch name> -e SONAR_HOST=<sonarqube url> \
-e NPM_PROXY=<url to proxy> \
-v <path to local ui>:/seip \
-v /var/run/docker.sock:/var/run/docker.sock --shm-size="512m" \
-p <host port fordarded to 7000 of the container>:7000
docker-reg.sirmaplatform.com/seip-ui-build:2.2 pull-request
```

#Q&A
* The following error occurs during integration tests  - `WebDriverError: unknown error: Chrome version must be >= <some chrome version goes here>`
 On each build the newest Chrome driver gets downloaded. Newer wersion of driver usually requires newer Chrome version.
 However the Chrome is bundled inside the docker image that runs the tests. To get a newer chrome, a newer image has to be built.
 To create a new image simply build from the Dockerfile and push it with a newer minor version.
 Also make sure to update the CI jobs to use the latest version of the build image.