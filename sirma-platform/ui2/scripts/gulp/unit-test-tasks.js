var gutil = require('gulp-util');
var karma = require('karma').Server;
var remap = require('remap-istanbul/lib/gulpRemapIstanbul');
var runSequence = require('run-sequence');
var replace = require('gulp-replace');
var shell = require('gulp-shell');
var path = require('path');

module.exports = function (gulp, paths, development) {
  var singleRun = development === false;

  gulp.task('test', function (done) {
    if (gutil.env.coverage) {
      runSequence('prepare-build-copy', 'istanbul-instrument', 'run-karma', 'remap-ut-reports', 'generate-ut-report', done);
    } else {
      runSequence('run-karma', done);
    }
  });

  gulp.task('run-karma', function (done) {
    new karma({
      configFile: path.join(paths.workDir, 'karma.conf.js'),
    }, done).start();
  });

  gulp.task('remap-ut-reports', function () {
    var remappedDir = path.join(path.normalize(paths.workDir), 'src/');
    return gulp.src(paths.reports.unit + '/coverage-final.json')
      .pipe(remap({}))
      .pipe(replace(/(..\/|..\\\\)+source[\/\\\\]/gmi, remappedDir))
      .pipe(gulp.dest(paths.reports.unit + 'coverage-final-remapped'));
  });

  gulp.task('generate-ut-report', function () {
    return gulp.src('')
    .pipe(shell([paths.workDir + '/node_modules/.bin/istanbul report --include='
    + paths.reports.unit + 'coverage-final-remapped/coverage-final.json' + ' -d ' + paths.reports.unit + 'lcov-report html']))
  });

};
