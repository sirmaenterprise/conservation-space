const fs = require('fs');
const path = require('path');

class Utils {

  static mkdirp(dir) {
    path.normalize(dir)
      .split(path.sep)
      .reduce((currentPath, folder) => {
        currentPath += folder + path.sep;
        if (!fs.existsSync(currentPath)){
          fs.mkdirSync(currentPath);
        }
        return currentPath;
      }, '');
  }
}

module.exports = Utils;
