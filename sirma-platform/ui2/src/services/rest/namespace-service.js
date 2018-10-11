import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';

const serviceUrl = '/semantic';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class NamespaceService {
  constructor(restClient, promiseAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.config = {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    };
    this.cache = {};
  }

  /**
   * Converts the provided array of URIs to full URI format. The array could contain short and full URIs. Ignores all
   * entries that are not URIs to avoid unexpected behaviour. This conversion creates new array and preserves
   * the order of the elements.
   * @param uris - the provided URIs
   * @returns {Promise} with the new array of resolved URIs
   */
  convertToFullURI(uris) {
    let urisToConvert = _.filter(uris, this.isUri);
    return this.toFullURI(urisToConvert).then((result) => {
      return uris.map((uri) => {
        return result.data[uri] || uri;
      });
    });
  }

  /**
   * Converts short URIs to full URIs
   * @param uris array with short URIs
   * @returns {*} map with short URIs as keys and full URIs as value
   */
  toFullURI(uris) {
    if (this.areAllUrisFull(uris)) {
      let result = {};
      uris.forEach(function (uri) {
        result[uri] = uri;
      });

      return this.promiseAdapter.resolve({
        data: result
      });
    }

    if (this.areAllShortUrisInCache(uris)) {
      return this.promiseAdapter.resolve({
        data: this.cache
      });
    }

    let url = `${serviceUrl}/uri/conversion/to-full`;
    return this.restClient.post(url, uris, this.config).then((result) => {
      let newCache = _.clone(this.cache);
      newCache = _.merge(newCache, result.data);
      this.cache = Object.freeze(newCache);
      return {
        data: this.cache
      };
    });
  }

  areAllShortUrisInCache(uris) {
    let shortUriNotInCache = _.find(uris, (uri) => {
      return this.isShortUri(uri) && !this.cache[uri];
    });

    return shortUriNotInCache === undefined;
  }

  areAllUrisFull(uris) {
    let shortUri = _.find(uris, (uri) => {
      return this.isShortUri(uri);
    });

    return shortUri === undefined;
  }

  isShortUri(uri) {
    return this.isUri(uri) && !this.isFullUri(uri);
  }

  isFullUri(uri) {
    return  uri.indexOf('http://') === 0 || uri.indexOf('https://') === 0;
  }

  isUri(uri) {
    return uri.indexOf(':') > 0;
  }
}
