var sonar = require('gulp-sonar');
var gutil = require('gulp-util');
var shell = require('gulp-shell');
var fs = require('fs');
var runSequence = require('run-sequence');

module.exports = function (gulp, paths) {

  gulp.task('sonar', function (done) {
    if (!fileExists(paths.reports.unit + 'coverage-final.json') || !fileExists(paths.reports.e2e + 'coverage-remapped/coverage-final.json')) {
      throw new gutil.PluginError({
        plugin: 'sonar',
        message: 'Unit and e2e coverage reports should be provided'
      });
    }

    // retry 3 times to generate overall coverage (sometimes it doesn't generate it for unknown reason)
    runSequence('overall-coverage', 'overall-coverage', 'overall-coverage', 'sonar-run', done);
  });

  gulp.task('overall-coverage', function () {
    if (!fileExists(paths.reports.overall + 'lcov.info')) {
      return gulp.src('').pipe(shell(['node ./node_modules/.bin/istanbul-combine -d ' + paths.reports.overall
      + ' -p summary -r lcov ' + paths.reports.unit + 'coverage-final-remapped/coverage-final.json ' + paths.reports.e2e
      + 'coverage-remapped/coverage-final.json '], {
        cwd: paths.workDir
      }));
    }
  });

  gulp.task('sonar-run', function () {
    var host = gutil.env['sonar_host'];
    var branchName = gutil.env['sonar_branch_name'];
    var version = gutil.env['sonar_project_version'];

    if (!host && !version) {
      throw new gutil.PluginError({
        plugin: 'sonar-run',
        message: 'Wrong arguments'
      });
    }

    if (!fileExists(paths.reports.overall + 'lcov.info')) {
      throw new gutil.PluginError({
        plugin: 'sonar-run',
        message: 'Overall coverage report not generated'
      });
    }

    // More info - http://docs.sonarqube.org/display/SONAR/Analysis+Parameters
    var options = {
      sonar: {
        host: {
          url: host
        },
        projectKey: 'seip-ui',
        projectName: 'seip-ui',
        projectVersion: version,
        branch: branchName,
        // comma-delimited string of source directories
        sources: 'src/',
        tests: 'test/,test-e2e/',
        exclusions: '**/lib/**/*,**/plugin.js,**/plugins.js,**/*adapters*/**',
        language: 'js',
        sourceEncoding: 'UTF-8',
        javascript: {
          lcov: {
            reportPath: paths.reports.overall + 'lcov.info',
            itReportPath: paths.reports.e2e + '/coverage-remapped/lcov.info'
          }
        },
        scm: {
          disabled: 'true'
        },
        issueassign: {
          enabled: 'false'
        },
        log: {
          level: 'WARN'
        },
        verbose: 'false',
        exec: {
          maxBuffer: 1024 * 1024
        }
      }
    };

    gulp.src([paths.src + '/**/*.js', paths.src + '/common/lib/ckeditor/'])
      .pipe(gulp.dest(paths.src));

    var consoleMethods = {
      error: console.error,
      info: console.info,
      debug: console.debug,
      log: console.log
    };

    var log = function (date, message) {
      if (message && !message.includes('DEBUG')) {
        consoleMethods.log(message.trim());
      }
    };

    var restoreConsole = function () {
      console.info = consoleMethods.info;
      console.error = consoleMethods.error;
      console.debug = consoleMethods.debug;
      console.log = consoleMethods.log;
    };

    console.info = log;
    console.error = log;
    console.debug = log;
    console.log = log;

    //manually merge unit & integration tests until https://jira.sonarsource.com/browse/SONAR-6202 gets done
    return gulp.src('').pipe(sonar(options)).on('end', function () {
      restoreConsole();
    }).on('error', function () {
      restoreConsole();
    });
  });

};

function fileExists(filePath) {
  try {
    return fs.statSync(filePath).isFile();
  } catch (error) {
    return false;
  }
}