export default class Paths {

  /**
   * Gets the base path where the script files are loaded from.
   * This is useful because the files are loaded from different paths in different environments - development, sandbox
   * and production environment.
   *
   * The code relies on the base path for the scripts configured in systemjs.
   *
   * @returns {void|string|*|XML}
   */
  static getBaseScriptPath() {
    var path = System.paths['*'].replace('*.js', '');
    if (path.length === 0) {
      path = '/';
    }

    if (path[0] !== '/') {
      path = '/' + path;
    }

    return path;
  }
}