import {Inject, Injectable} from 'app/app';
import {ServletClient} from 'services/servlet-client';
import {RestClient} from 'services/rest-client';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import _ from 'lodash';

export const SERVICE_PATH = '/content/';

@Injectable()
@Inject(ServletClient, RestClient, WindowAdapter)
export class ContentRestService {

  constructor(servletClient, restClient, windowAdapter) {
    this.config = {};
    this.servletClient = servletClient;
    this.restClient = restClient;
    this.windowAdapter = windowAdapter;
  }

  getContentUrl(id) {
    return this.servletClient.basePath + SERVICE_PATH + id;
  }

  getServiceUrl() {
    return this.restClient.basePath + SERVICE_PATH;
  }

  /**
   * Retrieves content by given content id.
   *
   * @param id the id of the content that should be retrieved
   * @param config additional configurations
   * @returns {*}
   */
  getContent(id, config) {
    config = _.defaults(config || {}, this.config);
    return this.restClient.get(`${SERVICE_PATH}${id}`, config);
  }

  getImageUrl(embeddedId, tenantId) {
    return `${this.windowAdapter.location.origin}/remote/api/content/static/${embeddedId}?tenant=${tenantId}`;
  }
}
