import Paths from 'common/paths';

export const PDFJS_PATH = 'common/lib/pdfjs/web/viewer.html';

/**
 * Represents a tiny wrapper for the pdf.js configuration link.
 */
export class PdfViewer {

  /**
   * @param filePath The file path to the pdf.
   * @param viewerConfig An object that might contain pdf.js viewer configuration options. If provided they will be
   * converted to query string and appended to the url that will be used for pdf loading.
   * For details about these arguments see: https://github.com/mozilla/pdf.js/wiki/Viewer-options
   */
  constructor(filePath, viewerConfig) {
    let configQuery = '';
    if(viewerConfig) {
      configQuery = Object.keys(viewerConfig).reduce((previous, key) => {
        return previous + key + '=' + viewerConfig[key] + '&';
      }, '#');
      if(configQuery.length === 1 || configQuery.indexOf('&', configQuery.length - 1) !== -1) {
        configQuery = configQuery.slice(0, configQuery.length - 1);
      }
    }
    this.src = Paths.getBaseScriptPath() + PDFJS_PATH + '?file=' + encodeURIComponent(filePath) + configQuery;
  }

}