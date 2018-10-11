//TODO check https://github.com/tinkertrain/jspm-react/blob/master/gulpfile.js

var gulp = require('gulp');
var babel = require('gulp-babel');
var sass = require('gulp-sass');
var sourcemaps = require('gulp-sourcemaps');
var autoprefixer = require('gulp-autoprefixer');
var plumber = require('gulp-plumber');
var notify = require('gulp-notify');
var runSequence = require('run-sequence');
var gutil = require('gulp-util');
var watch = require('gulp-watch');
var shell = require('gulp-shell');
var concat = require('gulp-concat');
var del = require('del');
var browserSync = require('browser-sync');
var e2eTestGenerator = require('./scripts/e2e-test-generator');
var httpProxy = require('http-proxy');
var addSonarTask = require('./scripts/gulp/sonar-task');
var addUnitTestTasks = require('./scripts/gulp/unit-test-tasks');
var addE2ETasks = require('./scripts/gulp/e2e-tasks');
var addUpdateMiradorTask = require('./scripts/gulp/update-mirador-task');
var addUpdateCKEditorTask = require('./scripts/gulp/update-ckeditor-task');
var replace = require('gulp-replace');
var lint = require('./scripts/gulp/lint');

// register start-dev task
require('./scripts/gulp/start-dev')(gulp);

//Uncomment for debug file order and you need to install gulp-debug
//var debug  = require('gulp-debug');
//use by calling: .pipe(debug({title: 'Processing:'}))

// https://www.npmjs.com/package/yargs

var paths = {
  workDir: __dirname,
  src: 'src/',
  dist: 'dist/',
  build: 'build/',
  buildInstrumented: 'build-instrumented/',
  test: 'test/',
  lib: 'src/common/lib/',
  reports: {
    allure: {
      results: 'allure-results/',
      report: 'allure-report/'
    },
    root: 'reports/',
    unit: 'reports/unit-coverage/',
    e2e: 'reports/e2e-coverage/',
    overall: 'reports/overall-coverage/'
  },
  sandbox: 'sandbox/',
  jspm_packages: 'jspm_packages'
};

paths.plugins = paths.src + '**/plugin.js';
paths.pluginFiles = [paths.plugins, '!**/common/lib/ckeditor/**', '!**/common/lib/mirador/**'];

// error notification settings for plumber
var plumberErrorHandler = {
  errorHandler: notify.onError({
    title: "Gulp",
    message: "Error: <%= error.message %>"
  })
};

var development = eval(gutil.env.development);
var sandboxBrowserSync;

// register lint task
lint(gulp, paths.src + '**/*.js');

gulp.task('dev', function (callback) {
  development = true;

  runSequence('compile', 'sass-watch', 'html-watch', 'plugin-definitions-watch', callback);

  // for some reason browser-sync breaks the copy of the js files
  // wait enough time so all other files are being processed
  setTimeout(function () {
    runSequence('browser-sync-app', 'browser-sync-sandbox');
  }, 20000);
});

gulp.task('compile', function (callback) {
  del.sync(paths.build + '*');
  del.sync(paths.reports.root + '*');

  gulp.src([paths.src + 'index.html', 'config.js']).pipe(gulp.dest(paths.build));

  runSequence('plugin-definitions', 'js', 'static-files', 'sass', 'html', callback);
});

gulp.task('dist', function (done) {
  development = false;

  runSequence('mark-production', 'cache-bust', 'bundle', done);
});

gulp.task('cache-bust', function (done) {
  var cacheBustSuffix = '.cb' + Math.round(new Date() / 60000);

  // The algorithm assumes that there will not be two builds in the same minute
  var modulesCacheBust = `var systemLocate = System.locate;
  System.locate = function(load) {
  var cacheBust = '${cacheBustSuffix}';
  return Promise.resolve(systemLocate.call(this, load)).then(function(address) {
      if (address.indexOf('jspm_packages') > -1 || address.indexOf('css.js') > -1 || address.indexOf('json.js') > -1 || address.indexOf('html.js') > -1 || address.indexOf(cacheBust) > -1) return address;
      var addressWithoutExtension = address.substring(0, address.lastIndexOf("."));
      var extension = address.substring(address.lastIndexOf("."), address.length);
      return addressWithoutExtension + cacheBust + extension;
    });
  };`;

  // Also add cache busting for css and js defined directly in index.html. I.e. config.js, plugins.js, etc.
  return gulp.src('build/index.html')
    .pipe(replace("// entrypoint", "// entrypoint\n  " + modulesCacheBust))
    .pipe(replace(/src="(.+?)\.js"/g, `src="$1${cacheBustSuffix}.js"`))
    .pipe(replace(/href="(.+?)\.css"/g, `href="$1${cacheBustSuffix}.css"`))
    .pipe(gulp.dest('./build'));
});

gulp.task('bundle', function (done) {
  gulp.src(paths.workDir + '/config.js')
    .pipe(replace('"*.js"', '"build/*.js"'))
    .pipe(gulp.dest('./'))
    .pipe(shell('jspm bundle main + common/* + adapters/**/* + filters/* + components/**/* + layout/**/* + form-builder/**/* build/main-bundle.js --minify --inject --no-mangle'))
    .on('end', function () {

      gulp.src(paths.workDir + '/config.js')
        .pipe(replace('"build/main-bundle"', '"main-bundle"'))
        .pipe(replace('"build/*.js"', '"*.js"'))
        .pipe(gulp.dest('./build')).on('end', function () {
        done();
      });
    });
});

gulp.task('mark-production', function (done) {
  require('gulp-git').exec({args : 'rev-parse HEAD'}, function (err, stdout) {
    var commitId = stdout.trim();

    var currentDate = new Date();
    var prodInfo = `window.prod = true;
    window.buildInfo = '${currentDate}';
    window.buildCommit = '${commitId}';`;

    require('fs').writeFileSync('build/build-info.txt', currentDate);

    gulp.src('build/index.html')
      .pipe(replace("// entrypoint", "// entrypoint\n " + prodInfo))
      .pipe(gulp.dest('./build'))
      .on('end', done);
  });

});

gulp.task('js', function () {
  var es6scripts = paths.src + '**/*.js';
  var libs = paths.src + '**/common/lib/**/*.js';

  var babelConfig = {
    // systemjs requires 'system' type -
    // https://github.com/ModuleLoader/es6-module-loader/wiki/Production-Workflows
    modules: 'system',
    moduleIds: false,
    comments: true,
    compact: false,
    stage: 0,
    externalHelpers: true,
    optional: ["es7.decorators", "runtime"]
  };

  var scriptPaths = [es6scripts, '!' + libs, '!' + paths.plugins];

  gulp.src(scriptPaths).pipe(plumber(plumberErrorHandler))
    .pipe(development ? watch(scriptPaths) : gutil.noop())
    .pipe(sourcemaps.init())
    .pipe(babel(babelConfig))
    .pipe(sourcemaps.write('.')).pipe(gulp.dest(paths.build));

  // directly copy es5 scripts
  var es5scripts = [libs, paths.plugins];

  gulp.src(es5scripts).pipe(plumber(plumberErrorHandler)).pipe(development ? watch(es5scripts) : gutil.noop())
    .pipe(gulp.dest(paths.build));
});

gulp.task('plugin-definitions', function () {
  del.sync(paths.build + 'plugins.js');

  gulp.src(paths.pluginFiles).pipe(plumber(plumberErrorHandler))
    .pipe(concat('plugins.js')).pipe(gulp.dest(paths.build));
});

gulp.task('plugin-definitions-watch', function () {
  gulp.watch(paths.pluginFiles, ['plugin-definitions']);
});

gulp.task('sass', function () {
  var cssPath = paths.src + '**/*.scss';
  var autoprefixerConfig = "last 2 versions";
  gulp.src(cssPath)
    // gulp.watch doesn't work this way for the sass partials.
    // There is a special sass-watch task below.
    //    .pipe(development ? watch(cssPath) : gutil.noop())
    .pipe(sass().on('error', sass.logError))
    .pipe(sourcemaps.init())
    .pipe(autoprefixer(autoprefixerConfig))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.build));
});

gulp.task('sass-watch', function () {
  var cssPath = paths.src + '**/*.scss';
  gulp.watch(cssPath, ['sass']);
});

gulp.task('html', function () {
  var htmlFiles = paths.src + '**/*.html';
  gulp.src(htmlFiles)
    .pipe(plumber(plumberErrorHandler))
    .pipe(gulp.dest(paths.build));
});

gulp.task('html-watch', function () {
  var htmlFiles = paths.src + '**/*.html';
  gulp.watch(htmlFiles, ['html']);
});

gulp.task('static-files', function () {
  var staticPaths = [paths.src + '**/*.{html,css,png,jpg,json,gif,eot,svg,ttf,woff,woff2,properties}'];

  gulp.src(staticPaths)
    .pipe(plumber(plumberErrorHandler))
    .pipe(development ? watch(staticPaths) : gutil.noop())
    .pipe(gulp.dest(paths.build));
});

gulp.task('e2e-ci', function (done) {
  runSequence('e2e', done);
});

gulp.task('browser-sync-app', function () {
  var backendUrl = gutil.env.backend;

  if (!backendUrl) {
    throw new Error('Backend url not supplied. Use --backend=<backend url> I.e. -backend=http://<address>:<port>/emf');
  }

  var proxy = httpProxy.createProxyServer({
    timeout: 120000,
    changeOrigin: true,
    secure: false,
    target: {
      https: backendUrl.startsWith('https://')
    }
  })
  .on('error', err => console.error('Proxy error:', err));

  var devBrowserSync = browserSync.create();
  devBrowserSync.init({
    server: {
      baseDir: ["./build"],
      routes: {
        "/jspm_packages": "jspm_packages"
      },
      middleware: function (req, res, next) {
        //proxy all the remote calls
        if (req.originalUrl.startsWith('/remote')) {
          var pathWithoutProxyPrefix = req.originalUrl.substring('/remote'.length);

          if (pathWithoutProxyPrefix.startsWith('/auth')) {
            res.writeHead(302,
              {Location: backendUrl + pathWithoutProxyPrefix}
            );
            res.end();
            return;
          }

          req.url = pathWithoutProxyPrefix;
          proxy.web(req, res, {target: backendUrl});
        } else {
          next();
        }
      }
    },
    ghostMode: false,
    injectChanges: false,
    files: ['./' + paths.build + '**',],
    port: 5000,
    ui: {
      port: 5001
    },
    open: false,
    logFileChanges: false
  });
});

gulp.task('browser-sync-sandbox', function (callback) {
  sandboxBrowserSync = browserSync.create();

  sandboxBrowserSync.init({
    open: false,
    notify: false,
    ghostMode: false,
    injectChanges: false,
    port: 9000,
    files: ['./' + paths.build + '**', './' + paths.sandbox + '**'],
    server: {
      baseDir: ["."],
      routes: {
        '/sandbox/images': 'build/images'
      }
    },
    ui: {
      port: 9001
    },
    logFileChanges: false
  }, callback);
});

gulp.task('browser-sync-sandbox-stop', function () {
  sandboxBrowserSync.exit();
});

addSonarTask(gulp, paths);
addE2ETasks(gulp, paths);
addUnitTestTasks(gulp, paths, development);
addUpdateMiradorTask(paths);
addUpdateCKEditorTask(paths);

gulp.task('e2e-test', function () {
  if (gutil.env.name) {
    e2eTestGenerator(gutil.env.name);
  }
});
