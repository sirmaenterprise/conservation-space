var gulp = require('gulp');
var template = require('gulp-template');
var rename = require('gulp-rename');

module.exports = function (basePath, source, destination, fileName, templateOptions) {
  return gulp.src(basePath + source)
    .pipe(template(templateOptions))
    .pipe(rename(fileName))
    .pipe(gulp.dest(destination));
};
