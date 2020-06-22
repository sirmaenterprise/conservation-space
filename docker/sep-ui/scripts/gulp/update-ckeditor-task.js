var gulp = require('gulp');
var gulpDecompress = require('gulp-decompress');
var fs = require('fs');
var http = require('http');
var del = require('del');
var gutil = require('gulp-util');

const tmpFolderName = 'ckeditor-temp/';

module.exports = function (paths) {

  const ckeditorPath = paths.workDir + '/' + paths.lib + 'ckeditor/';
  const tmpPath = ckeditorPath + tmpFolderName;

  function download(url) {
    var filename = tmpPath + 'ckeditor.zip';
    var fileStream = fs.createWriteStream(filename);
    return new Promise(function (resolve, reject) {
      http.get(url, function (response) {
        if (response.statusCode !== 200) {
          reject();
        }

        response.pipe(fileStream);

        fileStream.on('finish', function () {
          fileStream.close(function () {
            console.log('Build downloaded successfully from', url);
            resolve(filename);
          });
        });

      })
    });
  }

  function extract(file) {
    return new Promise(function (rosolve, reject) {
      gulp.src(file).pipe(gulpDecompress({strip: 1})).pipe(gulp.dest(tmpPath));
      console.log('Zip extracted');
      setTimeout(function () {
        rosolve();
      }, 300);
    });
  }

  function parseUrl() {
    return new Promise(function (resolve, reject) {
      var url = gutil.env['url'];
      if (url) {
        resolve(url);
      } else {
        const buildConfig = ckeditorPath + 'build-config.js';
        fs.readFile(buildConfig, function (err, data) {
          console.log('Reading config from', buildConfig);
          if (err) {
            reject(error);
          }
          var pattern = new RegExp(/http:\/\/ckeditor.com\/builder\/download\/[\w]+/);
          var dataStr = data.toString('utf8');
          var result = dataStr.match(pattern);
          if (result[0]) {
            resolve(result[0]);
          } else {
            reject('Cannot parse url');
          }
        });
      }
    });
  }

  function update() {
    return new Promise(function (resolve, reject) {
      gulp.src([tmpPath + 'ckeditor.js', tmpPath + 'build-config.js']).pipe(gulp.dest(ckeditorPath));
      gulp.src(tmpPath + 'plugins/**').pipe(gulp.dest(ckeditorPath + 'plugins/'));
      gulp.src(tmpPath + 'lang/**').pipe(gulp.dest(ckeditorPath + 'lang/'));
      setTimeout(function () {
        console.log('Files updated');
        resolve();
      }, 300);
    });

  }

  function createTmpFolder() {
    return new Promise(function (resolve, reject) {
      gulp.src('').pipe(gulp.dest(tmpPath));
      setTimeout(function () {
        console.log('Created temp folder', tmpPath);
        resolve();
      }, 300);
    });
  }

  function delTmpFolder() {
    console.log('Delete temp folder', del.sync(tmpPath));
  }

  function error(error) {
    console.error(error);
    delTmpFolder();
  }

  gulp.task('ckeditor:update', function () {
    createTmpFolder()
      .then(parseUrl)
      .then(download, error)
      .then(extract, error)
      .then(update)
      .then(delTmpFolder);
  });

};