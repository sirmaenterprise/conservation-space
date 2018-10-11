import _ from 'lodash';

/**
 * Collection meant to store and retrieve models. This class provides
 * a fast access and iteration over the stored models. In contrast
 * it provides relatively slow insertion and remove operations.
 *
 * Stored models order is preserved during insertion, they can be
 * also sorted based on a custom provided comparator.
 *
 * If a model with existing identifier is inserted it replaces
 * the old model, but preserves the position at which it was
 * initially before the insertion.
 *
 * @author Svetlozar Iliev
 */
export class ModelList {

  constructor() {
    this.map = {};
    this.array = [];
  }

  insert(model) {
    let index = -1;
    let id = model.getId();

    if (this.map[id]) {
      // remove existing model from the array
      index = arrayRemoveById(this.array, id);
    }
    this.map[id] = model;
    // if we have matching identifiers insert the model at the old position
    index > -1 ? this.array.splice(index, 0, model) : this.array.push(model);
  }

  remove(id) {
    if (!this.map[id]) {
      return;
    }

    // remove the model
    delete this.map[id];
    arrayRemoveById(this.array, id);
  }

  getModel(id) {
    return this.map[id];
  }

  getModels() {
    return this.array;
  }
}

function arrayRemoveById(array, id) {
  let index = _.findIndex(array, (item) => item.getId() === id);
  array.splice(index, 1);
  return index;
}