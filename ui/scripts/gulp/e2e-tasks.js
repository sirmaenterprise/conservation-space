var gutil = require('gulp-util');
var runSequence = require('run-sequence');
var del = require('del');
var shell = require('gulp-shell');
var replace = require('gulp-replace');
var fs = require('fs');
var path = require('path');
var remap = require('remap-istanbul/lib/gulpRemapIstanbul');
var browserSync = require('browser-sync');
var Q = require('q');
var spawn = require('child_process').spawn;

module.exports = function (gulp, paths) {

  gulp.task('e2e', function (done) {
    var compress = require('compression');
    var server = browserSync.create();

    server.init({
      open: false,
      notify: false,
      ghostMode: false,
      codeSync: false,
      port: 7000,
      files: ['./' + paths.build + '/**', './' + paths.sandbox + '/**'],
      server: {
        baseDir: ["."],
        middleware: [compress()],
        routes: {
          '/sandbox/images': 'build/images'
        }
      },
      ui: false,
      logFileChanges: false
    });

    var callback = function () {
      server.exit();
      done();
    };

    if (gutil.env['coverage']) {
      runSequence('remove-e2e-reports', 'prepare-build-copy', 'istanbul-instrument', 'run-protractor', 'istanbul-create-e2e-report', 'remap-e2e-reports', callback);
    } else {
      runSequence('run-protractor', callback);
    }
  });

  gulp.task('run-protractor', function () {
    var times = gutil.env.times || 1;
    var cmd =  paths.workDir + '/node_modules/.bin/protractor';
    var protArgs = [];
    if (/^win/.test(process.platform)) {
      protArgs.push('/s');
      protArgs.push('/c');
      protArgs.push(cmd);
      cmd = 'cmd'
    }

    Object.keys(gutil.env).forEach(function (key, index, array) {
      if (key.lastIndexOf('prot-', 0) === 0) {
        var protKey = key.substr(5);
        if (protKey.lastIndexOf('-', 0) === 0) {
          protArgs.push('-' + protKey + '=' + gutil.env[key]);
        } else {
          protArgs.push(protKey);
        }
      }
    });

    if (gutil.env['coverage']) {
      protArgs.push('--params.coverage=' + gutil.env['coverage']);
    }

    var browser = gutil.env['browser'];
    if (!browser) {
      browser = 'chrome';
    }
    protArgs.push('--capabilities.browserName=' + browser);

    if (gutil.env['threads']) {
      protArgs.push('--capabilities.shardTestFiles=true');
      protArgs.push('--capabilities.maxInstances=' + gutil.env['threads']);
    }

    if (gutil.env['baseUrl']) {
      protArgs.push('--baseUrl ' + gutil.env['baseUrl']);
    }

    if (gutil.env['seleniumAddress']) {
      protArgs.push('--seleniumAddress ' + gutil.env['seleniumAddress']);
    }

    var done = Q.defer();
    var executedCount = 0;
    var err = false;

    // recursive function for running the tests - runs gutil.env.times times, see beginning of the task
    function runProtractor() {
      var child = spawn(cmd, protArgs);

      child.stdout.on('data', (data) => process.stdout.write(data.toString()));
      child.stderr.on('data', (data) => process.stdout.write(data.toString()));

      child.on('exit', (code) => {
        err = err || !!code;

        if (++executedCount !== times) {
          runProtractor();
          return;
        }

        if (err) {
          done.reject('There were errors durring e2e tests execution');
        } else {
          done.resolve();
        }
      });
    };

    runProtractor();
    return done.promise;
  });

  gulp.task('remap-e2e-reports', function () {
    return gulp.src(paths.reports.e2e + '/coverage-report/*.json')
      .pipe(remap({
        basePath: paths.workDir + '/' + paths.src
      })).pipe(replace(/(..\/)+source/gmi, paths.workDir + '/src'))
      .pipe(gulp.dest(paths.reports.e2e + '/coverage-remapped'));
  });

  gulp.task('remove-e2e-reports', function () {
    del.sync(paths.reports.e2e);
  });

  gulp.task('prepare-build-copy', function () {
    del.sync(paths.buildInstrumented);
    return gulp.src([paths.build + '/**/*.*']).pipe(gulp.dest(paths.buildInstrumented));
  });

  gulp.task('istanbul-instrument', function () {
    return gulp.src('').pipe(shell([paths.workDir + '/node_modules/.bin/istanbul instrument '
    + paths.build + ' -o ' + paths.buildInstrumented + ' -x common/lib/**']));
  });

  gulp.task('istanbul-create-e2e-report', function () {
    return gulp.src('').pipe(shell([paths.workDir + '/node_modules/.bin/istanbul report --include='
    + paths.reports.e2e + 'coverage-build/*.json ' + '-d ' + paths.reports.e2e + 'coverage-report json']))
  });
};