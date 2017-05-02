
# Pdf.js
The platform manages own version of the pdf.js library/viewer which is located in src/common/lib/pdfjs.
## Library update
If the library have to be updated, then read the notes below:
* Download the version which is needed - most likely from the https://mozilla.github.io/pdf.js/getting_started/#download
* Delete the old version: build and web folders in src/common/lib/pdfjs
* Extract the new version in src/common/lib/pdfjs
* In src/common/lib/pdfjs/web/viewer.js is forbidden loading of external files on hosted viewer (see https://github.com/mozilla/pdf.js/pull/6916). Locate the HOSTED_VIEWER_ORIGINS list in viewer.js and comment in the next lines this code ```if (fileOrigin !== viewerOrigin) { throw new Error('file origin does not match viewer\'s'); }```
* Check if the new version works as expected: content viewer widget, print, export and so on.

# Widget development
See [yeoman generator page](http://git.sirmaplatform.com/stash/projects/SEIP/repos/generator-ses-idoc)

# Build
For the build scripts process look at ./docker/build
