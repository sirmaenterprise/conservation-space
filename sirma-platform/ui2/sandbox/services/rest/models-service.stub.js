import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import _ from 'lodash';
import data from 'sandbox/services/rest/models-service.data.json!';

const DATA_TYPE = 'DATA_TYPE';
const OBJECT_TYPE = 'OBJECT_TYPE';
const TYPES = [DATA_TYPE, OBJECT_TYPE];

@Injectable()
@Inject(PromiseAdapter)
export class ModelsService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getOntologies() {
    return this.promiseAdapter.resolve(data.ontologies);
  }

  getTypes(type) {
    return this.promiseAdapter.resolve(TYPES.indexOf(type) > -1 ?
      data.types[type] : _.flatten(_.map(Object.values(data.types))));
  }

  getModels(purpose, contextId, mimetype, fileExtension) {
    if (purpose && purpose[0] === 'create' && contextId === '6') {
      let clonedData = _.cloneDeep(data);
      clonedData.errorMessage = 'Context is invalid';
      return this.promiseAdapter.resolve(clonedData);
    }
    return this.promiseAdapter.resolve(data);
  }

  getClassInfo(rdfType) {
    if (Array.isArray(rdfType)) {
      rdfType = this.convertToFullUri(rdfType[0]);
    }
    var classInfo = data.models.find(function (element) {
      return element.id === rdfType;
    });
    return this.promiseAdapter.resolve({
      data: classInfo
    });
  }

  getExistingInContextInfo(definitionId) {
    let definitionInfo = data.models.find(function (element) {
      return element.id === definitionId;
    });

    let existingInContext = 'BOTH';
    if (definitionInfo) {
      existingInContext = definitionInfo.existingInContext;
    }

    return this.promiseAdapter.resolve(existingInContext);
  }

  convertToFullUri(rdfType) {
    return 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#' + rdfType.split(':')[1];
  }

  getDefinitionImportUrl() {
    return '/models/import';
  }

  getOntologyImportUrl() {
    return '/ontology/import';
  }

  getImportedModels() {
    return this.promiseAdapter.resolve(_.cloneDeep(data.imported));
  }

  download(downloadRequest) {
    // keep the params for stub verification
    window.modelsServiceStub = {
      download: downloadRequest
    };

    return this.promiseAdapter.resolve({
      data: '',
      fileName: 'models.zip'
    });
  }

  downloadOntology() {
    return this.promiseAdapter.resolve({
      data: '',
      fileName: 'ontologies.zip'
    });
  }
}

ModelsService.PURPOSE_CREATE = 'create';
ModelsService.PURPOSE_UPLOAD = 'upload';
ModelsService.PURPOSE_SEARCH = 'search';

ModelsService.TYPE_CLASS = 'class';
ModelsService.TYPE_DEFINITION = 'definition';
