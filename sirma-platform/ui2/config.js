System.config({
  "baseURL": "/",
  "transpiler": "babel",
  "babelOptions": {
    "modules": "system",
    "moduleIds": false,
    "comments": false,
    "compact": false,
    "externalHelpers": true,
    "optional": [
      "es7.decorators",
      "runtime"
    ]
  },
  "paths": {
    "github:*": "jspm_packages/github/*.js",
    "npm:*": "jspm_packages/npm/*.js",
    "*": "*.js"
  },
  "rootURL": "build/"
});

System.config({
  "map": {
    "CodeSeven/toastr": "github:CodeSeven/toastr@2.1.1",
    "Eonasdan/bootstrap-datetimepicker": "github:Eonasdan/bootstrap-datetimepicker@4.17.37",
    "angular": "github:angular/bower-angular@1.5.4",
    "angular-animate": "github:angular/bower-angular-animate@1.5.5",
    "angular-loading-bar": "github:chieffancypants/angular-loading-bar@0.8.0",
    "angular-sanitize": "github:angular/bower-angular-sanitize@1.5.10",
    "angular-translate": "github:angular-translate/bower-angular-translate@2.7.2",
    "angular-ui-router": "github:angular-ui/ui-router@0.2.18",
    "babel": "npm:babel-core@5.8.38",
    "babel-runtime": "npm:babel-runtime@5.8.38",
    "bowser": "npm:bowser@1.4.3",
    "bpmn-js": "npm:bpmn-js@0.20.5",
    "clean-css": "npm:clean-css@3.4.28",
    "clivezhg/select2-to-tree": "github:clivezhg/select2-to-tree@1.1.1",
    "codemirror": "npm:codemirror@5.40.2",
    "core-js": "npm:core-js@0.9.18",
    "css": "github:systemjs/plugin-css@0.1.18",
    "d3": "npm:d3@4.7.1",
    "dotdotdot": "npm:dotdotdot@1.7.0",
    "file-saver": "npm:file-saver@1.3.3",
    "filesize": "npm:filesize@3.2.1",
    "font-awesome": "npm:font-awesome@4.6.3",
    "johnculviner/jquery.fileDownload": "github:johnculviner/jquery.fileDownload@1.4.6",
    "johnny/jquery-sortable": "github:johnny/jquery-sortable@0.9.13",
    "jquery": "github:components/jquery@2.1.4",
    "jquery-file-download": "npm:jquery-file-download@1.4.6",
    "jquery-file-upload": "npm:blueimp-file-upload@9.12.1",
    "jquery-lazyload": "npm:jquery-lazyload@1.9.7",
    "json": "github:systemjs/plugin-json@0.1.2",
    "jstz": "github:pellepim/jstimezonedetect@master",
    "keycloak-js": "npm:keycloak-js@4.3.0",
    "lodash": "npm:lodash@3.10.0",
    "moment": "github:moment/moment@2.14.1",
    "nathancahill/Split.js": "github:nathancahill/Split.js@1.2.0",
    "node-emoji": "npm:node-emoji@1.4.1",
    "polished": "npm:polished@1.9.2",
    "postaljs/monologue.js": "github:postaljs/monologue.js@0.3.5",
    "postaljs/postal.js": "github:postaljs/postal.js@1.0.6",
    "riveter": "npm:riveter@0.2.0",
    "sdecima/javascript-detect-element-resize": "github:sdecima/javascript-detect-element-resize@0.5.3",
    "select2": "github:sirmaenterprise/select2@4.0.3-custom.1",
    "shortid": "npm:shortid@2.2.8",
    "spectrum-colorpicker": "npm:spectrum-colorpicker@1.8.0",
    "stacktrace-js": "npm:stacktrace-js@0.6.4",
    "string-matcher": "github:radpet/node-kmp@master",
    "text": "github:systemjs/plugin-text@0.0.2",
    "twbs/bootstrap-sass": "github:twbs/bootstrap-sass@3.3.5",
    "underscore": "npm:lodash@3.10.0",
    "vakata/jstree": "github:vakata/jstree@3.3.5",
    "github:CodeSeven/toastr@2.1.1": {
      "css": "github:systemjs/plugin-css@0.1.18",
      "jquery": "github:components/jquery@2.1.4"
    },
    "github:angular-translate/bower-angular-translate@2.7.2": {
      "angular": "github:angular/bower-angular@1.5.4"
    },
    "github:angular-ui/ui-router@0.2.18": {
      "angular": "github:angular/bower-angular@1.5.4"
    },
    "github:angular/bower-angular-animate@1.5.5": {
      "angular": "github:angular/bower-angular@1.5.4"
    },
    "github:angular/bower-angular-sanitize@1.5.10": {
      "angular": "github:angular/bower-angular@1.5.4"
    },
    "github:chieffancypants/angular-loading-bar@0.8.0": {
      "angular": "github:angular/bower-angular@1.5.4",
      "css": "github:systemjs/plugin-css@0.1.18"
    },
    "github:jspm/nodelibs-assert@0.1.0": {
      "assert": "npm:assert@1.4.1"
    },
    "github:jspm/nodelibs-buffer@0.1.1": {
      "buffer": "npm:buffer@5.0.7"
    },
    "github:jspm/nodelibs-constants@0.1.0": {
      "constants-browserify": "npm:constants-browserify@0.0.1"
    },
    "github:jspm/nodelibs-crypto@0.1.0": {
      "crypto-browserify": "npm:crypto-browserify@3.11.0"
    },
    "github:jspm/nodelibs-events@0.1.1": {
      "events": "npm:events@1.0.2"
    },
    "github:jspm/nodelibs-http@1.7.1": {
      "Base64": "npm:Base64@0.2.1",
      "events": "github:jspm/nodelibs-events@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "url": "github:jspm/nodelibs-url@0.1.0",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "github:jspm/nodelibs-https@0.1.0": {
      "https-browserify": "npm:https-browserify@0.0.0"
    },
    "github:jspm/nodelibs-os@0.1.0": {
      "os-browserify": "npm:os-browserify@0.1.2"
    },
    "github:jspm/nodelibs-path@0.1.0": {
      "path-browserify": "npm:path-browserify@0.0.0"
    },
    "github:jspm/nodelibs-process@0.1.2": {
      "process": "npm:process@0.11.10"
    },
    "github:jspm/nodelibs-stream@0.1.0": {
      "stream-browserify": "npm:stream-browserify@1.0.0"
    },
    "github:jspm/nodelibs-string_decoder@0.1.0": {
      "string_decoder": "npm:string_decoder@0.10.31"
    },
    "github:jspm/nodelibs-url@0.1.0": {
      "url": "npm:url@0.10.3"
    },
    "github:jspm/nodelibs-util@0.1.0": {
      "util": "npm:util@0.10.3"
    },
    "github:jspm/nodelibs-vm@0.1.0": {
      "vm-browserify": "npm:vm-browserify@0.0.4"
    },
    "npm:asn1.js@4.9.1": {
      "bn.js": "npm:bn.js@4.11.6",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
      "vm": "github:jspm/nodelibs-vm@0.1.0"
    },
    "npm:assert@1.4.1": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "util": "npm:util@0.10.3"
    },
    "npm:babel-runtime@5.8.38": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:blueimp-file-upload@9.12.1": {
      "process": "github:jspm/nodelibs-process@0.1.2",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:bn.js@4.11.6": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1"
    },
    "npm:bpmn-js@0.20.5": {
      "bpmn-moddle": "npm:bpmn-moddle@0.14.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "diagram-js": "npm:diagram-js@0.19.3",
      "diagram-js-direct-editing": "npm:diagram-js-direct-editing@0.17.1",
      "diagram-js-origin": "npm:diagram-js-origin@0.15.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "ids": "npm:ids@0.2.0",
      "inherits": "npm:inherits@2.0.1",
      "lodash": "npm:lodash@3.10.0",
      "min-dom": "npm:min-dom@0.2.0",
      "object-refs": "npm:object-refs@0.1.1",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2",
      "tiny-svg": "npm:tiny-svg@0.1.1"
    },
    "npm:bpmn-moddle@0.14.0": {
      "lodash": "npm:lodash@3.10.0",
      "moddle": "npm:moddle@1.0.0",
      "moddle-xml": "npm:moddle-xml@1.0.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:brorand@1.1.0": {
      "crypto": "github:jspm/nodelibs-crypto@0.1.0"
    },
    "npm:browserify-aes@1.0.6": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "buffer-xor": "npm:buffer-xor@1.0.3",
      "cipher-base": "npm:cipher-base@1.0.3",
      "create-hash": "npm:create-hash@1.1.2",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "inherits": "npm:inherits@2.0.1",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:browserify-cipher@1.0.0": {
      "browserify-aes": "npm:browserify-aes@1.0.6",
      "browserify-des": "npm:browserify-des@1.0.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "evp_bytestokey": "npm:evp_bytestokey@1.0.0"
    },
    "npm:browserify-des@1.0.0": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "cipher-base": "npm:cipher-base@1.0.3",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "des.js": "npm:des.js@1.0.0",
      "inherits": "npm:inherits@2.0.1"
    },
    "npm:browserify-rsa@4.0.1": {
      "bn.js": "npm:bn.js@4.11.6",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "constants": "github:jspm/nodelibs-constants@0.1.0",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "randombytes": "npm:randombytes@2.0.3"
    },
    "npm:browserify-sign@4.0.4": {
      "bn.js": "npm:bn.js@4.11.6",
      "browserify-rsa": "npm:browserify-rsa@4.0.1",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hash": "npm:create-hash@1.1.2",
      "create-hmac": "npm:create-hmac@1.1.4",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "elliptic": "npm:elliptic@6.4.0",
      "inherits": "npm:inherits@2.0.1",
      "parse-asn1": "npm:parse-asn1@5.1.0",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:buffer-xor@1.0.3": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:buffer@5.0.7": {
      "base64-js": "npm:base64-js@1.2.1",
      "ieee754": "npm:ieee754@1.1.8"
    },
    "npm:cipher-base@1.0.3": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "string_decoder": "github:jspm/nodelibs-string_decoder@0.1.0"
    },
    "npm:clean-css@4.1.7": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "http": "github:jspm/nodelibs-http@1.7.1",
      "https": "github:jspm/nodelibs-https@0.1.0",
      "os": "github:jspm/nodelibs-os@0.1.0",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "source-map": "npm:source-map@0.5.6",
      "url": "github:jspm/nodelibs-url@0.1.0"
    },
    "npm:codemirror@5.40.2": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:commander@2.9.0": {
      "child_process": "github:jspm/nodelibs-child_process@0.1.0",
      "events": "github:jspm/nodelibs-events@0.1.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "graceful-readlink": "npm:graceful-readlink@1.0.1",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:component-classes@1.2.6": {
      "component-indexof": "npm:component-indexof@0.0.3"
    },
    "npm:component-closest@0.1.4": {
      "component-matches-selector": "npm:component-matches-selector@0.1.6"
    },
    "npm:component-delegate@0.2.4": {
      "component-closest": "npm:component-closest@0.1.4",
      "component-event": "npm:component-event@0.1.4"
    },
    "npm:component-matches-selector@0.1.6": {
      "component-query": "npm:component-query@0.0.3"
    },
    "npm:constants-browserify@0.0.1": {
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:core-js@0.9.18": {
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:core-util-is@1.0.2": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1"
    },
    "npm:create-ecdh@4.0.0": {
      "bn.js": "npm:bn.js@4.11.6",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "elliptic": "npm:elliptic@6.4.0"
    },
    "npm:create-hash@1.1.2": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "cipher-base": "npm:cipher-base@1.0.3",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "inherits": "npm:inherits@2.0.1",
      "ripemd160": "npm:ripemd160@1.0.1",
      "sha.js": "npm:sha.js@2.4.8"
    },
    "npm:create-hmac@1.1.4": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hash": "npm:create-hash@1.1.2",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "inherits": "npm:inherits@2.0.1",
      "stream": "github:jspm/nodelibs-stream@0.1.0"
    },
    "npm:crypto-browserify@3.11.0": {
      "browserify-cipher": "npm:browserify-cipher@1.0.0",
      "browserify-sign": "npm:browserify-sign@4.0.4",
      "create-ecdh": "npm:create-ecdh@4.0.0",
      "create-hash": "npm:create-hash@1.1.2",
      "create-hmac": "npm:create-hmac@1.1.4",
      "diffie-hellman": "npm:diffie-hellman@5.0.2",
      "inherits": "npm:inherits@2.0.1",
      "pbkdf2": "npm:pbkdf2@3.0.9",
      "public-encrypt": "npm:public-encrypt@4.0.0",
      "randombytes": "npm:randombytes@2.0.3"
    },
    "npm:d3-brush@1.0.3": {
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-drag": "npm:d3-drag@1.0.3",
      "d3-interpolate": "npm:d3-interpolate@1.1.3",
      "d3-selection": "npm:d3-selection@1.0.4",
      "d3-transition": "npm:d3-transition@1.0.3"
    },
    "npm:d3-chord@1.0.3": {
      "d3-array": "npm:d3-array@1.1.0",
      "d3-path": "npm:d3-path@1.0.4"
    },
    "npm:d3-drag@1.0.3": {
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-selection": "npm:d3-selection@1.0.4"
    },
    "npm:d3-dsv@1.0.4": {
      "commander": "npm:commander@2.9.0",
      "iconv-lite": "npm:iconv-lite@0.4.15",
      "rw": "npm:rw@1.3.3"
    },
    "npm:d3-force@1.0.5": {
      "d3-collection": "npm:d3-collection@1.0.2",
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-quadtree": "npm:d3-quadtree@1.0.2",
      "d3-timer": "npm:d3-timer@1.0.4",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:d3-geo@1.6.1": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "d3-array": "npm:d3-array@1.1.0"
    },
    "npm:d3-interpolate@1.1.3": {
      "d3-color": "npm:d3-color@1.0.2"
    },
    "npm:d3-request@1.0.4": {
      "d3-collection": "npm:d3-collection@1.0.2",
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-dsv": "npm:d3-dsv@1.0.4",
      "xmlhttprequest": "npm:xmlhttprequest@1.8.0"
    },
    "npm:d3-scale@1.0.4": {
      "d3-array": "npm:d3-array@1.1.0",
      "d3-collection": "npm:d3-collection@1.0.2",
      "d3-color": "npm:d3-color@1.0.2",
      "d3-format": "npm:d3-format@1.1.0",
      "d3-interpolate": "npm:d3-interpolate@1.1.3",
      "d3-time": "npm:d3-time@1.0.5",
      "d3-time-format": "npm:d3-time-format@2.0.4"
    },
    "npm:d3-shape@1.0.5": {
      "d3-path": "npm:d3-path@1.0.4"
    },
    "npm:d3-time-format@2.0.4": {
      "d3-time": "npm:d3-time@1.0.5"
    },
    "npm:d3-transition@1.0.3": {
      "d3-color": "npm:d3-color@1.0.2",
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-ease": "npm:d3-ease@1.0.2",
      "d3-interpolate": "npm:d3-interpolate@1.1.3",
      "d3-selection": "npm:d3-selection@1.0.4",
      "d3-timer": "npm:d3-timer@1.0.4"
    },
    "npm:d3-zoom@1.1.2": {
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-drag": "npm:d3-drag@1.0.3",
      "d3-interpolate": "npm:d3-interpolate@1.1.3",
      "d3-selection": "npm:d3-selection@1.0.4",
      "d3-transition": "npm:d3-transition@1.0.3"
    },
    "npm:d3@4.7.1": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "d3-array": "npm:d3-array@1.1.0",
      "d3-axis": "npm:d3-axis@1.0.5",
      "d3-brush": "npm:d3-brush@1.0.3",
      "d3-chord": "npm:d3-chord@1.0.3",
      "d3-collection": "npm:d3-collection@1.0.2",
      "d3-color": "npm:d3-color@1.0.2",
      "d3-dispatch": "npm:d3-dispatch@1.0.2",
      "d3-drag": "npm:d3-drag@1.0.3",
      "d3-dsv": "npm:d3-dsv@1.0.4",
      "d3-ease": "npm:d3-ease@1.0.2",
      "d3-force": "npm:d3-force@1.0.5",
      "d3-format": "npm:d3-format@1.1.0",
      "d3-geo": "npm:d3-geo@1.6.1",
      "d3-hierarchy": "npm:d3-hierarchy@1.1.2",
      "d3-interpolate": "npm:d3-interpolate@1.1.3",
      "d3-path": "npm:d3-path@1.0.4",
      "d3-polygon": "npm:d3-polygon@1.0.2",
      "d3-quadtree": "npm:d3-quadtree@1.0.2",
      "d3-queue": "npm:d3-queue@3.0.4",
      "d3-random": "npm:d3-random@1.0.2",
      "d3-request": "npm:d3-request@1.0.4",
      "d3-scale": "npm:d3-scale@1.0.4",
      "d3-selection": "npm:d3-selection@1.0.4",
      "d3-shape": "npm:d3-shape@1.0.5",
      "d3-time": "npm:d3-time@1.0.5",
      "d3-time-format": "npm:d3-time-format@2.0.4",
      "d3-timer": "npm:d3-timer@1.0.4",
      "d3-transition": "npm:d3-transition@1.0.3",
      "d3-voronoi": "npm:d3-voronoi@1.1.1",
      "d3-zoom": "npm:d3-zoom@1.1.2",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:des.js@1.0.0": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "minimalistic-assert": "npm:minimalistic-assert@1.0.0"
    },
    "npm:diagram-js-direct-editing@0.17.1": {
      "diagram-js": "npm:diagram-js@0.19.3",
      "lodash": "npm:lodash@3.10.0",
      "min-dom": "npm:min-dom@0.2.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:diagram-js-origin@0.15.1": {
      "diagram-js": "npm:diagram-js@0.19.3",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "tiny-svg": "npm:tiny-svg@0.1.1"
    },
    "npm:diagram-js@0.19.3": {
      "didi": "npm:didi@0.1.1",
      "hammerjs": "npm:hammerjs@2.0.8",
      "inherits": "npm:inherits@2.0.1",
      "lodash": "npm:lodash@3.10.0",
      "min-dom": "npm:min-dom@0.2.0",
      "object-refs": "npm:object-refs@0.1.1",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "tiny-svg": "npm:tiny-svg@0.1.1"
    },
    "npm:diffie-hellman@5.0.2": {
      "bn.js": "npm:bn.js@4.11.6",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "miller-rabin": "npm:miller-rabin@4.0.0",
      "randombytes": "npm:randombytes@2.0.3",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:dotdotdot@1.7.0": {
      "jquery": "npm:jquery@3.2.1"
    },
    "npm:elliptic@6.4.0": {
      "bn.js": "npm:bn.js@4.11.6",
      "brorand": "npm:brorand@1.1.0",
      "hash.js": "npm:hash.js@1.0.3",
      "hmac-drbg": "npm:hmac-drbg@1.0.1",
      "inherits": "npm:inherits@2.0.1",
      "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
      "minimalistic-crypto-utils": "npm:minimalistic-crypto-utils@1.0.1",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:evp_bytestokey@1.0.0": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hash": "npm:create-hash@1.1.2",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0"
    },
    "npm:font-awesome@4.6.3": {
      "css": "github:systemjs/plugin-css@0.1.18"
    },
    "npm:graceful-readlink@1.0.1": {
      "fs": "github:jspm/nodelibs-fs@0.1.2"
    },
    "npm:hammerjs@2.0.8": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:hash.js@1.0.3": {
      "inherits": "npm:inherits@2.0.1"
    },
    "npm:hmac-drbg@1.0.1": {
      "hash.js": "npm:hash.js@1.0.3",
      "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
      "minimalistic-crypto-utils": "npm:minimalistic-crypto-utils@1.0.1",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:https-browserify@0.0.0": {
      "http": "github:jspm/nodelibs-http@1.7.1"
    },
    "npm:iconv-lite@0.4.15": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "string_decoder": "github:jspm/nodelibs-string_decoder@0.1.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:ids@0.2.0": {
      "hat": "npm:hat@0.0.3"
    },
    "npm:inherits@2.0.1": {
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:jquery-file-download@1.4.6": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:keycloak-js@4.3.0": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:lodash@2.4.2": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:lodash@3.10.0": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:miller-rabin@4.0.0": {
      "bn.js": "npm:bn.js@4.11.6",
      "brorand": "npm:brorand@1.1.0"
    },
    "npm:min-dom@0.2.0": {
      "component-classes": "npm:component-classes@1.2.6",
      "component-closest": "npm:component-closest@0.1.4",
      "component-delegate": "npm:component-delegate@0.2.4",
      "component-event": "npm:component-event@0.1.4",
      "component-matches-selector": "npm:component-matches-selector@0.1.6",
      "component-query": "npm:component-query@0.0.3",
      "domify": "npm:domify@1.4.0"
    },
    "npm:moddle-xml@1.0.0": {
      "lodash": "npm:lodash@3.10.0",
      "moddle": "npm:moddle@1.0.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "sax": "npm:sax@0.6.1",
      "tiny-stack": "npm:tiny-stack@0.1.0"
    },
    "npm:moddle@1.0.0": {
      "lodash": "npm:lodash@3.10.0"
    },
    "npm:node-emoji@1.4.1": {
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "string.prototype.codepointat": "npm:string.prototype.codepointat@0.2.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:os-browserify@0.1.2": {
      "os": "github:jspm/nodelibs-os@0.1.0"
    },
    "npm:parse-asn1@5.1.0": {
      "asn1.js": "npm:asn1.js@4.9.1",
      "browserify-aes": "npm:browserify-aes@1.0.6",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hash": "npm:create-hash@1.1.2",
      "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
      "pbkdf2": "npm:pbkdf2@3.0.9",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:path-browserify@0.0.0": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:pbkdf2@3.0.9": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hmac": "npm:create-hmac@1.1.4",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:process@0.11.10": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "vm": "github:jspm/nodelibs-vm@0.1.0"
    },
    "npm:public-encrypt@4.0.0": {
      "bn.js": "npm:bn.js@4.11.6",
      "browserify-rsa": "npm:browserify-rsa@4.0.1",
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "create-hash": "npm:create-hash@1.1.2",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "parse-asn1": "npm:parse-asn1@5.1.0",
      "randombytes": "npm:randombytes@2.0.3"
    },
    "npm:punycode@1.3.2": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:randombytes@2.0.3": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:readable-stream@1.1.14": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "core-util-is": "npm:core-util-is@1.0.2",
      "events": "github:jspm/nodelibs-events@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "isarray": "npm:isarray@0.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "stream-browserify": "npm:stream-browserify@1.0.0",
      "string_decoder": "npm:string_decoder@0.10.31",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:ripemd160@1.0.1": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:riveter@0.2.0": {
      "lodash": "npm:lodash@2.4.2",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "systemjs-json": "github:systemjs/plugin-json@0.1.2"
    },
    "npm:rw@1.3.3": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:sax@0.6.1": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "http": "github:jspm/nodelibs-http@1.7.1",
      "path": "github:jspm/nodelibs-path@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "stream": "github:jspm/nodelibs-stream@0.1.0",
      "string_decoder": "github:jspm/nodelibs-string_decoder@0.1.0",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:sha.js@2.4.8": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "inherits": "npm:inherits@2.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:shortid@2.2.8": {
      "crypto": "github:jspm/nodelibs-crypto@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:source-map@0.5.6": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:stream-browserify@1.0.0": {
      "events": "github:jspm/nodelibs-events@0.1.1",
      "inherits": "npm:inherits@2.0.1",
      "readable-stream": "npm:readable-stream@1.1.14"
    },
    "npm:string_decoder@0.10.31": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1"
    },
    "npm:tiny-svg@0.1.1": {
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:url@0.10.3": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "punycode": "npm:punycode@1.3.2",
      "querystring": "npm:querystring@0.2.0",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:util@0.10.3": {
      "inherits": "npm:inherits@2.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:vm-browserify@0.0.4": {
      "indexof": "npm:indexof@0.0.1"
    },
    "npm:xmlhttprequest@1.8.0": {
      "buffer": "github:jspm/nodelibs-buffer@0.1.1",
      "child_process": "github:jspm/nodelibs-child_process@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "http": "github:jspm/nodelibs-http@1.7.1",
      "https": "github:jspm/nodelibs-https@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "url": "github:jspm/nodelibs-url@0.1.0"
    }
  }
});
