import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

const SERVICE_URL = '/models';
const INFO_REST_SERVICE_URL = SERVICE_URL + '/info';

@Injectable()
@Inject(RestClient)
export class ModelsService {

  constructor(restClient) {
    this.restClient = restClient;
  }

  getModels(purpose, contextId, mimetype, fileExtension, classFilter, definitionFilter) {
    let config = {
      params: {
        purpose: purpose,
        contextId: contextId,
        mimetype: mimetype,
        extension: fileExtension,
        classFilter: classFilter,
        definitionFilter: definitionFilter
      }
    };
    return this.restClient.get(SERVICE_URL, config).then((response) => {
      return response.data;
    });
  }

  getClassInfo(rdfType) {
    var config = {
      params: {
        id: rdfType
      }
    };
    return this.restClient.get(INFO_REST_SERVICE_URL, config);
  }
}

ModelsService.PURPOSE_CREATE = 'create';
ModelsService.PURPOSE_UPLOAD = 'upload';
ModelsService.PURPOSE_SEARCH = 'search';

ModelsService.TYPE_CLASS = 'class';
ModelsService.TYPE_DEFINITION = 'definition';