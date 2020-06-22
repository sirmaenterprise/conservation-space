const fs = require('fs');
const Utils = require('./utils');

module.exports = (gulp, paths) => {
  gulp.task('report-missing-label-translations', () => {
    const labels = require(`${paths.workDir}/${paths.src}services/rest/labels.json`);
    const base = labels.en;
    const missingLabelsDir = `${paths.workDir}/temp/labels`;

    Object.keys(labels)
      .filter(lang => lang != 'en')
      .forEach(key => {
        const translation = labels[key];

        const missing = Object.keys(base)
          .filter(key => !translation[key])
          .reduce((result, value) => {
            result[value] = base[value];
            return result;
          }, {});

        // if there are missing translations - write them to a file
        if (Object.keys(missing).length) {
          if (!fs.existsSync(missingLabelsDir)) {
            Utils.mkdirp(missingLabelsDir);
          }

          fs.writeFileSync(`${missingLabelsDir}/missing_${key}.json`, JSON.stringify(missing, null, 2), 'utf8');
        }
      });
  });
}
