var process = require('./file-process');

module.exports = function (filePath) {
  var tokens = filePath.split('/');
  var filename = tokens[tokens.length - 1];
  var path = '';
  for (var i = 0; i < tokens.length - 1; i++) {
    path = path.concat(tokens[i] + '/');
  }
  var e2ePath = 'test-e2e/' + path;
  var sandboxPath = 'sandbox/' + path;
  var basePath = 'scripts/e2e/';
  var template = {
    name: filename,
    sandboxPath: sandboxPath
  };

  process(basePath, 'sandbox.html', sandboxPath, 'index.html', template);
  process(basePath, 'e2e.spec.js', e2ePath, filename + '.spec.js', template);
};