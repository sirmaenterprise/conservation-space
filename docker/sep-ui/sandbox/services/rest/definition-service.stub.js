import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/services/rest/definition-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class DefinitionService {
  constructor(promise) {
    this.promise = promise;
    this.config = config['definitions'];
  }

  getDefinitions(definitionsIds) {
    let response = { };
    if (!(definitionsIds instanceof Array)) {
      definitionsIds = [definitionsIds];
    }
    definitionsIds.forEach((definitionId) => {
      response[definitionId] = data.definitions[definitionId];
    });

    response = _.cloneDeep(response);

    return this.promise.resolve({
      data: response
    });
  }

  getFields(identifiers) {
    let definitions = data.fields.filter((definition) => {
      return identifiers.indexOf(definition.identifier) !== -1;
    });
    return this.promise.resolve({
      data: _.cloneDeep(definitions)
    });
  }
}