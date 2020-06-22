import _ from 'lodash';
import {Inject, Injectable, NgHttp} from 'app/app';

export const HEADER_V2_JSON = 'application/vnd.seip.v2+json';

export const BASE_PATH = '/remote/api';

@Injectable()
@Inject(NgHttp)
export class RestClient {

  constructor($http) {
    this.$http = $http;
    this.basePath = BASE_PATH;
    this.config = {
      headers: {}
    };
  }

  get(url, config) {
    return this.$http.get(this.basePath + url, config);
  }

  post(url, data, config) {
    return this.$http.post(this.basePath + url, data, config);
  }

  patch(url, data, config) {
    return this.$http.patch(this.basePath + url, data, config);
  }

  deleteResource(url, config) {
    return this.$http.delete(this.basePath + url, config);
  }

  delete(url, data, config) {
    return this.$http.delete(this.basePath + url, data, config);
  }

  /**
   * Provides additional configurations like http headers
   * @param config Supports the following config groups: <br />
   *        headers: object which properties are the headers to be applied on ajax request.<br />
   *        basePath: base path to the path of the proxy forwarding to the backend. Defaults to "/remote/api".
   */
  configure(config) {
    _.merge(this.config, config);

    if (config.headers) {
      _.forEach(config.headers, (value, key) => {
        this.$http.defaults.headers.common[key] = config.headers[key];
      });
    }

    if (config.basePath) {
      this.basePath = config.basePath;
    }
  }

  getUrl(url) {
    return this.basePath + url;
  }
}
