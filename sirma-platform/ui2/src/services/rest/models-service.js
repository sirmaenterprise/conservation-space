import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {RequestsCacheService} from 'services/rest/requests-cache-service';

const SERVICE_URL = '/models';
const INFO_REST_SERVICE_URL = SERVICE_URL + '/info';

@Injectable()
@Inject(RestClient, RequestsCacheService)
export class ModelsService {

  constructor(restClient, requestsCacheService) {
    this.restClient = restClient;
    this.requestsMap = new Map();
    this.requestsCacheService = requestsCacheService;
  }

  /**
   * Provides list of ontologies represented as objects with id and title properties.
   * @returns a promise resolving to an array containing the existing ontologies.
   */
  getOntologies() {
    return this.restClient.get(`${SERVICE_URL}/ontologies`).then((response) => {
      return response.data;
    });
  }

  getModels(purpose, contextId, mimetype, fileExtension, classFilter, definitionFilter) {
    let config = {
      params: {
        purpose,
        contextId,
        mimetype,
        extension: fileExtension,
        classFilter,
        definitionFilter
      }
    };


    return this.requestsCacheService.cache(SERVICE_URL, [config], this.requestsMap, () => {
      return this.restClient.get(SERVICE_URL, config).then(response => {
        return response.data;
      });
    });
  }

  getClassInfo(rdfType) {
    let config = {
      params: {
        id: rdfType
      }
    };
    return this.restClient.get(INFO_REST_SERVICE_URL, config);
  }

  getDefinitionImportUrl() {
    return this.restClient.basePath + SERVICE_URL + '/import';
  }

  getOntologyImportUrl(tenantId) {
    return this.restClient.basePath + '/tenant/upload/ontology/' + tenantId;
  }

  /**
   * Fetches data about all imported models.
   *
   * @return object containing array of objects with 'id' and 'title' properties that is mapped to
   * 'definitions' and 'templates' property of the object.
   */
  getImportedModels() {
    return this.restClient.get(`${SERVICE_URL}/imported`).then(response => {
      return response.data;
    });
  }

  /**
   * Fetches imported models as files by given identifiers packaged as zip file.
   *
   * @param downloadRequest object containing array of definition identifiers mapped to 'definitions' property
   * and  array of template identifier mapped to 'templates' property. There are two special properties:
   * 'allTemplates' and 'allDefinitions' that can mark all templates/definitions for selection.
   *
   * @return models as binary sequence.
   */
  download(downloadRequest) {
    let config = {
      responseType: 'arraybuffer',
      headers: {
        'Accept': 'application/octet-stream',
        'Content-Type': HEADER_V2_JSON
      }
    };

    return this.restClient.post(`${SERVICE_URL}/download`, downloadRequest, config).then(response => {
      return {
        data: response.data,
        fileName: response.headers('x-file-name')
      };
    });
  }

  /**
   * Fetch value of field with uri "emf:existingInContext" from definition with definitionId.
   * <pre>
   *    <span style="color:RED">This have to be removed after implementation of definition fields.</span>
   * </pre>
   */
  getExistingInContextInfo(definitionId) {
    let url = `${SERVICE_URL}/existing-in-context`;
    return this.requestsCacheService.cache(url, definitionId, this.requestsMap, () => {
      return this.restClient.get(url, {params: {definitionId}}).then((response) => {
        return response.data;
      });
    });
  }
}

ModelsService.PURPOSE_CREATE = 'create';
ModelsService.PURPOSE_UPLOAD = 'upload';
ModelsService.PURPOSE_SEARCH = 'search';

ModelsService.TYPE_CLASS = 'class';
ModelsService.TYPE_DEFINITION = 'definition';