import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/idoc/widget/object-data-widget/data/definition-service.data.json!';
import _ from 'lodash';

@Injectable()
@Inject(PromiseAdapter)
export class DefinitionService {
  constructor(promise) {
    this.promise = promise;
    this.config = config['definitions'];
  }

  getTypes(options) {
    var types = data.types;

    if (options && options.classFilter) {
      types = _.filter(data.types, function (type) {
        return _.includes(options.classFilter, type.id);
      });
    }

    return this.promise.resolve({
      data: types
    });
  }

  getDefinitions(definitionsIds) {
    let response = { };
    if (!(definitionsIds instanceof Array)) {
      definitionsIds = [definitionsIds];
    }
    definitionsIds.forEach((definitionId) => {
      response[definitionId] = data.definitions[definitionId];
    });
    return this.promise.resolve({
      data: response
    });
  }

  getFields(identifiers) {
    let definitions = data.fields.filter((definition) => {
      return identifiers.indexOf(definition.identifier) !== -1;
    });
    // in auto selection get the definition as is
    if (definitions.length === 0) {
      definitions = data.fields;
    }
    return this.promise.resolve({
      data: _.cloneDeep(definitions)
    });
  }
}