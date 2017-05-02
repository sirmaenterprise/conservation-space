import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/models-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class ModelsService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getModels(purpose, contextId, mimetype, fileExtension) {
    return this.promiseAdapter.resolve(data);
  }

  getClassInfo(rdfType) {
    var classInfo = data.models.find(function(element) {
      return element.id === rdfType;
    });

    return this.promiseAdapter.resolve({
      data: classInfo
    });
  }
}

ModelsService.PURPOSE_CREATE = 'create';
ModelsService.PURPOSE_UPLOAD = 'upload';
ModelsService.PURPOSE_SEARCH = 'search';

ModelsService.TYPE_CLASS = 'class';
ModelsService.TYPE_DEFINITION = 'definition';