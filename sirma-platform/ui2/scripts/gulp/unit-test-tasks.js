var gutil = require('gulp-util');
var karma = require('karma').Server;
var remap = require('remap-istanbul/lib/gulpRemapIstanbul');
var runSequence = require('run-sequence');
var replace = require('gulp-replace');
var shell = require('gulp-shell');

module.exports = function (gulp, paths, development) {
  var singleRun = development === false;

  gulp.task('test', function (done) {
    if (singleRun) {
      runSequence('prepare-build-copy', 'istanbul-instrument', 'run-karma', 'remap-ut-reports', 'generate-ut-report', done);
    }
    else {
      runSequence('run-karma', done);
    }

  });

  gulp.task('run-karma', function (done) {

    var browsers = ['ChromeHeadless'];

    if (gutil.env['headless'] === 'false') {
      var browsers = ['Chrome'];
    }

    if (gutil.env['browsers']) {
      browsers = gutil.env['browsers'].split(',');
    }

    var config = paths.workDir + '/karma.conf.js';
    if (singleRun) {
      config = paths.workDir + '/karma.conf.coverage.js'
    }

    new karma({
      configFile: config,
      singleRun: singleRun,
      browsers: browsers
    }, done).start();
  });

  gulp.task('remap-ut-reports', function () {
    var remappedDir = paths.workDir.replace(/\\/g, '/');
    return gulp.src(paths.reports.unit + '/coverage-final.json')
      .pipe(remap({}))
      .pipe(replace(/(..\/|..\\\\)+source/gmi, remappedDir + '/src'))
      .pipe(gulp.dest(paths.reports.unit + 'coverage-final-remapped'));
  });

  gulp.task('generate-ut-report', function () {
    return gulp.src('')
    .pipe(shell([paths.workDir + '/node_modules/.bin/istanbul report --include='
    + paths.reports.unit + 'coverage-final-remapped/coverage-final.json' + ' -d ' + paths.reports.unit + 'lcov-report html']))
  });

};