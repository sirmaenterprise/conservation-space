var gulp 		= require('gulp'),
	concat 		= require('gulp-concat'),
	ngTemplates = require('gulp-angular-templatecache'),
	es 			= require('event-stream'),
	wrap 		= require('gulp-wrap'),
	glib		= require('./sources.json');

function makeNgTemplates() {
	return gulp.src(glib.sources.ngTemplates)
			.pipe(ngTemplates('templates.js', { module: 'permissions' }));
}

function jsSrc() {
	return gulp.src(glib.sources.js);
}

gulp.task('default', function() {
	es.merge(
		jsSrc(),
		makeNgTemplates()
	)
	.pipe(concat('permissions.js'))
	.pipe(wrap('(function() { <%= contents %> }());'))
	.pipe(gulp.dest('dist/'));

	gulp.src('src/**/*.css')
		.pipe(gulp.dest('dist/'));
});

gulp.task('watch', function() {
	gulp.watch('src/**/*', ['default']);
});