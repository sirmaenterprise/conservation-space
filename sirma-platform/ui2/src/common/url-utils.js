import {IDOC_STATE} from 'idoc/idoc-constants';

// Gets any prefix:uuid until ?, # or / is matched
const INSTANCE_ID_REGEX = new RegExp('([a-z]+:[^?#/]+)', 'g');

export class UrlUtils {

  /**
   * Searches for an a parameter in a URL.
   *
   * @param url url to process
   * @param name name of the parameter
   * @returns {string} value of the parameter or null if missing
   */
  static getParameter(url, name) {
    let result = UrlUtils.getParameterArray(url, name);
    return result instanceof Array && result.length > 0 ? result[0] : null;
  }

  /**
   * @param url
   * @param name
   * @returns {*} an array with URL parameters values (multiple parameters with the same name) or undefined
   */
  static getParameterArray(url, name) {
    let decodedUrl = decodeURIComponent(url);
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    let regex = new RegExp('[\\?&]' + name + '=([^&#]*)', 'g');
    let results, match;
    while ((match = regex.exec(decodedUrl)) !== null) {
      if (results === undefined) {
        results = [];
      }
      results.push(decodeURIComponent(match[1].replace(/\+/g, ' ')));
    }
    return results;
  }

  static getParamSeparator(url) {
    return url.indexOf('?') !== -1 ? '&' : '?';
  }

  /**
   * Extracts url fragment from given url if any. The url is decoded first.
   *
   * @param url
   * @returns The url fragment string or empty string if no fragment is found.
   */
  static getUrlFragment(url) {
    let hash = url.replace('/#', '');
    hash = decodeURIComponent(hash);
    hash = hash.indexOf('#') !== -1 ? hash.slice(hash.indexOf('#') + 1) : '';
    return hash;
  }

  /**
   * Extract instance identifier from url by specific pattern.
   *
   * @returns instance identifier or null
   */
  static getIdFromUrl(url) {
    if (!url) {
      return null;
    }
    var decodedUrl = decodeURIComponent(url);
    var results = decodedUrl.match(INSTANCE_ID_REGEX);
    // Returns the last match (if host:port is matched too)
    return results === null ? null : results[results.length - 1];
  }

  static buildIdocUrl(instanceId, tabId = '', params = {}) {
    if (!instanceId) {
      throw Error('Instance id is required!');
    }
    let url = `/#/${IDOC_STATE}/${instanceId}`;
    let paramsLen = Object.keys(params).length;
    if (paramsLen > 0) {
      url += '?';
      Object.keys(params).forEach((key) => {
        url += `${key}=${params[key]}&`;
      });
      url = url.substring(0, url.length - 1);
    }
    if (tabId) {
      url += `#${tabId}`;
    }
    return url;
  }

  static appendQueryParam(url, param, value) {
    let querySeparator = UrlUtils.getParamSeparator(url);
    return `${url}${querySeparator}${param}=${value}`;
  }
}