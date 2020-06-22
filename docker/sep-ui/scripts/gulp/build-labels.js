const fs = require('fs');
const Utils = require('./utils');

module.exports = (gulp, paths) => {
  gulp.task('build-labels', done => {
    let dir = `${paths.workDir}/${paths.generated}services/rest`;
    Utils.mkdirp(dir);

    fs.readFile(`${paths.workDir}/${paths.src}services/rest/labels.json`, 'utf8', (err, labels) => {
      if (err) {
        done(err);
        return;
      }

      let content = `// Generated at ${new Date().toISOString()}\nexport default ${labels};`
      fs.writeFile(`${dir}/labels.js`, content, 'utf8', err => {
          done(err);
      });
    });
  });
}
