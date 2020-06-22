let gutil = require('gulp-util');
let runSequence = require('run-sequence');
let del = require('del');
let shell = require('gulp-shell');
let replace = require('gulp-replace');
let fs = require('fs');
let remap = require('remap-istanbul/lib/gulpRemapIstanbul');
let browserSync = require('browser-sync');
let spawn = require('child_process').spawn;
let path = require('path');

module.exports = function (gulp, paths) {

  gulp.task('e2e', function (done) {
    let compress = require('compression');
    let server = browserSync.create();

    server.init({
      open: false,
      notify: false,
      ghostMode: false,
      codeSync: false,
      port: process.env.TESTS_PORT || 7000,
      files: ['./' + paths.build + '/**', './' + paths.sandbox + '/**'],
      server: {
        baseDir: ['.'],
        middleware: [compress()],
        routes: {
          '/': 'build/',
          '/favicon.ico': 'build/images/favicon/favicon.png'
        }
      },
      ui: false,
      logFileChanges: false
    });

    let callback = function (err) {
      if (err) {
        return process.exit(1);
      }

      server.exit();
      return done;
    };

    if (gutil.env['coverage']) {
      runSequence('remove-e2e-reports', 'remove-allure-reports', 'prepare-build-copy', 'istanbul-instrument', 'bundle-libs', 'run-protractor',
        'istanbul-create-e2e-report', 'remap-e2e-reports', callback);
    } else {
      runSequence('remove-allure-reports', 'run-protractor', callback);
    }
  });

  gulp.task('run-protractor', function () {
    let times = gutil.env.times || 1;
    let cmd = paths.workDir + '/node_modules/.bin/protractor';
    let protArgs = [];
    if (/^win/.test(process.platform)) {
      protArgs.push('/s');
      protArgs.push('/c');
      protArgs.push(cmd);
      cmd = 'cmd';
    }

    Object.keys(gutil.env).forEach(function (key) {
      if (key.lastIndexOf('prot-', 0) === 0) {
        let protKey = key.substr(5);
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

    let browser = gutil.env['browser'];
    if (!browser) {
      browser = 'chrome';
    }
    protArgs.push('--capabilities.browserName=' + browser);

    if (gutil.env['threads']) {
      protArgs.push('--capabilities.shardTestFiles=true');
      protArgs.push('--capabilities.maxInstances=' + gutil.env['threads']);
    }

    if (gutil.env['baseUrl']) {
      protArgs.push('--baseUrl=' + gutil.env['baseUrl']);
    }

    if (gutil.env['seleniumAddress']) {
      protArgs.push('--seleniumAddress=' + gutil.env['seleniumAddress']);
    } else {
      protArgs.push('--directConnect');
    }

    let executedCount = 0;
    let err = false;

    return new Promise((resolve, reject) => {
      function endProtractor(err) {
        if (err) {
          reject('There were errors during e2e tests execution');
        } else {
          resolve();
        }
      }

      // recursive function for running the tests - runs gutil.env.times times, see beginning of the task
      function runProtractor() {
        let child = spawn(cmd, protArgs);

        child.stdout.on('data', (data) => process.stdout.write(data.toString()));
        child.stderr.on('data', (data) => process.stdout.write(data.toString()));

        child.on('exit', (code) => {
          err = err || !!code;

          if (++executedCount !== times) {
            runProtractor();
            return;
          }

          if (gutil.env['generate-html-report']) {
            return gulp.src('').pipe(shell([`${paths.workDir}/node_modules/allure-commandline/bin/allure generate `
            + `${paths.reports.allure.results} --clean`])).on('end', function () {
              endProtractor(err);
            });
          } else {
            endProtractor(err);
          }
        });
      }

      runProtractor();
    });

  });

  gulp.task('remap-e2e-reports', function () {
    var remappedDir = path.join(path.normalize(paths.workDir), 'src/');
    return gulp.src(paths.reports.e2e + '/coverage-report/*.json')
      .pipe(remap({}))
      .pipe(replace(/(..\/|..\\\\)+source[\/\\\\]/gmi, remappedDir))
      .pipe(gulp.dest(paths.reports.e2e + '/coverage-remapped'));
  });

  gulp.task('remove-e2e-reports', function () {
    del.sync(paths.reports.e2e);
  });

  gulp.task('remove-allure-reports', function () {
    del.sync(paths.reports.allure.results);
    del.sync(paths.reports.allure.report);
  });

  gulp.task('bundle-libs', function () {
    let libs = 'babel/polyfill + jquery + angular + angular-ui-router + twbs/bootstrap-sass';

    return gulp.src(paths.workDir + '/config.js')
      .pipe(replace('*.js', 'build/*.js'))
      .pipe(gulp.dest('./'))
      .pipe(shell('jspm bundle ' + libs + ' build-instrumented/libs-bundle.js --minify --inject --no-mangle'))
      .on('end', function () {
        return gulp.src(paths.workDir + '/config.js')
          .pipe(replace('build-instrumented/libs-bundle', 'libs-bundle'))
          .pipe(replace('build/*.js"', '"*.js'))
          // restore config.js to initial state
          .pipe(shell('git checkout HEAD -- config.js'));
      });
  });

  gulp.task('prepare-build-copy', function () {
    del.sync(paths.buildInstrumented);
    return gulp.src([paths.build + '/**/*.*']).pipe(gulp.dest(paths.buildInstrumented));
  });

  gulp.task('istanbul-instrument', function () {
    return gulp.src('').pipe(shell([paths.workDir + '/node_modules/.bin/istanbul instrument '
    + paths.build + ' -o ' + paths.buildInstrumented + ' -x common/lib/**']));
  });

  gulp.task('istanbul-create-e2e-report', function (callback) {
    runSequence('process-e2e-reports', 'merge-e2e-reports', callback);
  });

  let coveragePath = paths.reports.e2e + 'coverage-build/';
  let coverageMergedPath = paths.reports.e2e + 'coverage-report/';

  // A coverage json is created for each test method. As a result hundreds of files
  // get created. Istanbul merges the reports sequentially (hence slowly). The following code
  // splits the report files into multiple (n) directories and performs parallel Instanbul report merges.
  gulp.task('process-e2e-reports', function () {
    const n = 4;
    let files = fs.readdirSync(coveragePath);

    for (let i = 0; i < n; i++) {
      fs.mkdirSync(coveragePath + i);
    }

    let chunkSize = files.length / n;

    files.forEach(function (file, index) {
      fs.renameSync(coveragePath + file, coveragePath + Math.floor(index / chunkSize) + '/' + file);
    });

    let mergedStream = require('merge-stream')();

    for (let i = 0; i < n; i++) {
      let currentChunkIstanbulMergeCommand = paths.workDir + '/node_modules/.bin/istanbul report --include='
        + coveragePath + i + '/*.json ' + '-d ' + paths.reports.e2e + 'coverage-report/' + i + ' json';

      mergedStream.add(gulp.src('').pipe(shell([currentChunkIstanbulMergeCommand])));
    }

    return mergedStream;
  });

  // Merges the reports created by the parallel processing of the previous task
  gulp.task('merge-e2e-reports', function () {
    return gulp.src('').pipe(shell([paths.workDir + '/node_modules/.bin/istanbul report --include='
    + coverageMergedPath + '*/*.json ' + '-d ' + paths.reports.e2e + 'coverage-report json']));
  });
};
