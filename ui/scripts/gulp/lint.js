var eslint = require('gulp-eslint');

module.exports = function(gulp, glob) {

  gulp.task('lint', function() {
    return gulp.src(glob)
      .pipe(eslint())
      .pipe(eslint.format());
  });
};