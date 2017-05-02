import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const SERVICE_PATH = '/objects/browse/tree';

@Injectable()
@Inject(RestClient)
export class ObjectBrowserRestService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  /**
   * Fetches children nodes of a given node.
   *
   * @param nodePath path of the node which children nodes are fetched.
   * @param tere config used to construct request parameters object.
   * @return an array with the result
   */
  getChildNodes(nodePath, params) {
    var requestParams = {
      currId: params.id,
      node: nodePath,
      allowSelection: params.selectable,
      clickableLinks: params.clickableLinks,
      clickOpenWindow: params.openInNewWindow,
      manualExpand: true
    };

    return this.restClient.get(SERVICE_PATH, {
      params: requestParams
    }).then(function (response) {
      return response.data;
    });
  }
}
