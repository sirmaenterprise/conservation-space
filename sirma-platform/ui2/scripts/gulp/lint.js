var eslint = require('gulp-eslint');

module.exports = (gulp, glob) => {

  gulp.task('lint', () => {
    return gulp.src(glob)
      .pipe(eslint())
      .pipe(eslint.format())
      .pipe(eslint.failAfterError());
  });
};
