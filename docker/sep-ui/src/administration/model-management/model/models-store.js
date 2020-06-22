import {ModelList} from 'administration/model-management/model/model-list';
import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import _ from 'lodash';

/**
 * Class meant to store different types of models. The base models which can be stored are of
 * type {@link ModelBase}. The {@link ModelStore}'s main purpose is to contain instances of
 * different model types. It allows the user to get a model of a given type or lookup a model
 * among all stored types. Internally the {@link ModelStore} uses {@link ModelList} to store
 * all models and provides an interface to extract both a plain {@link Array} and a {@link ModelList}
 * of models for a given type or all types.
 *
 * @author Svetlozar Iliev
 */
export class ModelStore {

  constructor() {
    this.models = {};
  }

  getModel(id, type) {
    if (!type) {
      let result = null;
      this.getAllModels().some(models => {
        result = models.getModel(id);
        return !!result;
      });
      return result;
    }
    return this.getModelsList(type).getModel(id);
  }

  getModels(type) {
    if (!type) {
      let result = [];
      this.getAllModels().map(models => {
        result = result.concat(models.getModels());
        return result;
      });
      return result;
    }
    return this.getModelsList(type).getModels();
  }

  getModelsList(type) {
    return this.models[this.getModelType(type)];
  }

  addModel(model) {
    if (model instanceof ModelBase) {
      let type = this.getModelType(model);
      if (!this.models[type]) {
        this.models[type] = new ModelList();
      }
      this.models[type].insert(model);
    }
    return this;
  }

  getAllModels() {
    return Object.values(this.models);
  }

  getModelType(target) {
    return ModelManagementUtility.getModelType(_.isFunction(target) ? target.prototype : target);
  }
}