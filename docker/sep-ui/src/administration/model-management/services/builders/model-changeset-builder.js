import {Injectable, Inject} from 'app/app';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelChangeSet} from 'administration/model-management/model/model-changeset';

import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';

import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

/**
 * Service which builds a change set with a specified format from a collection of models. Change set is build in
 * a single array of changes collected from all models passed as arguments to the builder function. The changes
 * are built by taking only the dirty or changed models.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelPathBuilder)
export class ModelChangeSetBuilder {

  constructor(modelPathBuilder) {
    this.modelPathBuilder = modelPathBuilder;
  }

  /**
   * Builds a change set from a collection of models which should be
   * instance of {@link ModelAttribute} or any type which inherits it.
   * The method also requires an operation to be provided, which would
   * be associated with the constructed change sets.
   *
   * @param models - list of dirty models for which a change set would be build for a specific operation
   * @param operation - operation to be assigned to the change set. Must be of type {@link ModelOperation}
   * @returns {Array} - collection of all constructed items of type {@link ModelChangeSet}
   */
  buildChangeSets(models, operation) {
    // collection to hold changes
    let changes = new ModelList();

    models.forEach(model => {
      let change = this.buildChangeSet(model, operation);
      // map each change by the selector built from path
      changes.insert(change, (c) => c.getSelector());
    });

    // changes as plain array
    return changes.getModels();
  }

  /**
   * Builds a change set single model provided as parameter which should be
   * instance of {@link ModelAttribute} or any type which inherits it. The
   * method also requires an operation to be provided, which would be associated
   * with the constructed change set.
   *
   * @param model - single model for which a change set for a specific operation would be build and provided
   * @param operation - operation to be assigned to the change set. Must be of type {@link ModelOperation}
   * @returns {ModelChangeSet} - the constructed instance of type {@link ModelChangeSet}
   */
  buildChangeSet(model, operation) {
    // path to the current model
    let path = this.getPath(model);

    // construct current change from model
    return new ModelChangeSet()
      .setSelector(this.toString(path))
      .setNewValue(this.getNewValue(model))
      .setOldValue(this.getOldValue(model))
      .setModel(model).setOperation(operation);
  }

  getPath(model) {
    return this.modelPathBuilder.buildPathFromModel(model);
  }

  getNewValue(model) {
    // when an attribute model is provided get current values from the dirty model values
    return this.isModelAttribute(model) ? this.getValue(model, (v) => v.getValue()) : null;
  }

  getOldValue(model) {
    // when an attribute model is provided get old values from all of the dirty model values
    return this.isModelAttribute(model) ? this.getValue(model, (v) => v.getOldValue()) : null;
  }

  getValue(model, callback) {
    if (this.isMultiValued(model)) {
      let values = _.transform(model.getValues(), (result, value) => {
        // for each value map language to each value
        result[value.getLanguage()] = callback(value);
      }, {});
      return values;
    }
    // get from single valued attribute
    return callback(model.getValue());
  }

  toString(path) {
    return this.modelPathBuilder.buildStringFromPath(path);
  }

  isModelAttribute(model) {
    return ModelManagementUtility.isModelAttribute(model);
  }

  isMultiValued(model) {
    return ModelAttributeTypes.isMultiValued(model.getType());
  }
}