var gutil = require('gulp-util');
var gulp = require('gulp');
var git = require('gulp-git');
var shell = require('gulp-shell');

module.exports = function (paths) {
  const tempFolder = 'mirador-temp/';
  const miradorBuildPath = paths.workDir + '/' + paths.src + 'common/lib/mirador/mirador-build';
  const miradorFilePath = miradorBuildPath + '/mirador';

  function clone(repo) {
    console.log('Cloning', repo);
    return new Promise(function (resolve, reject) {
      git.clone(repo, {args: tempFolder}, function (err) {
        if (err) {
          console.error('Could not clone the repository', err);
          reject(err);
        } else {
          resolve();
        }
      });
    });
  }

  function checkout(branch) {
    return new Promise(function (resolve, reject) {
      git.checkout(branch, {cwd: tempFolder}, function (err) {
        if (err) {
          console.error('Could not checkout', branch, err);
          reject(err);
        } else {
          resolve();
        }
      });
    });
  }

  function build() {
    console.log('Building');
    return new Promise(function (resolve, reject) {
      gulp.src('').pipe(shell([
        'cd ' + tempFolder + '&& npm install && bower install && node_modules/.bin/grunt -f',
        'cd ../',
        'rm -r ' + miradorBuildPath,
        'rm ' + tempFolder + 'build/mirador/css/mirador-combined.css',
        'mv ' + tempFolder + 'build ' + miradorBuildPath,
        'rm -r ' + tempFolder
      ])).on('end', resolve);
    });
  }

  function saveVersion(repo, branch) {
    console.log('Generating version file');
    return new Promise(function (resolve, reject) {
      gulp.src('').pipe(shell([
        `rm ${miradorBuildPath}/build-version.txt || true`,
        `echo repo=${repo} and branch=${branch} >> ${miradorBuildPath}/build-version.txt`
      ])).on('end', resolve);
    });

  }

  gulp.task('mirador:update', function () {
    var branch = gutil.env['branch'] || 'master';
    var repo = gutil.env['repo'];
    if (!repo) {
      throw new gutil.PluginError({
        plugin: 'update mirador script',
        message: 'Wrong arguments'
      });
    }

    return clone(repo).then(function () {
      return checkout(branch);
    }).then(build).then(function () {
      return saveVersion(repo, branch);
    });

  });

};