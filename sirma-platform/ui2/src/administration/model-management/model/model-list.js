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
    this.clear();
  }

  /**
   * Inserts an item which should be of type {@link ModelBase}. The model is internally
   * mapped by its identifier for fast access. Optionally a callback function can be
   * introduced as the second argument to provide a custom mapping function instead of
   * the default - using model identifier
   *
   * When an item with the same mapping is inserted again the new item would override &
   * replace the old item. During insertion the order of insertion is preserved even
   * when a duplicate item is inserted it is inserted at the original position.
   *
   * @param model - the model to be inserted to the model list
   * @param mapper - optional callback mapper or a string key
   * @returns {ModelList} - reference to this model list object
   */
  insert(model, mapper) {
    let index = -1;
    let id = getModelKey(model, mapper);
    let current = this.getModel(id);

    // trying to insert same
    if (current === model) {
      return this;
    }

    if (current) {
      // remove the existing model from the array
      index = arrayRemoveById(this.array, id, mapper);
    }
    this.map[id] = model;
    // if we have matching identifiers insert the model at the old position
    index > -1 ? this.array.splice(index, 0, model) : this.array.push(model);

    return this;
  }

  /**
   * Removes an item with the specified identifier from the list. The default behaviour
   * implicitly uses the model identifier to compare with the provided identifier for
   * removal.  Optionally a callback function can be introduced as the second argument
   * to provide a custom mapping function instead of the default - using model identifier
   *
   * @param id - identifier of the item to be removed
   * @param mapper - optional callback mapper  - identifier of the item to be removed
   */
  remove(id, mapper) {
    if (!this.map[id]) {
      return;
    }

    // remove the model
    delete this.map[id];
    arrayRemoveById(this.array, id, mapper);
  }

  /**
   * Checks if the list contains a model with the specified identifier.
   *
   * @param id  - identifier of the item to be checked
   * @returns {boolean} - true if contained, false otherwise
   */
  hasModel(id) {
    return !!this.getModel(id);
  }

  /**
   * Extracts the model with the specified identifier.
   *
   * @param id - identifier of the item to be extracted
   * @returns {*} - the found item or undefined when item is not present
   */
  getModel(id) {
    return this.map[id];
  }

  /**
   * Returns a plain array of the items present inside the list. Items inside this list
   * are with preserved order of insertion.
   *
   * @returns {Array} - plain js array
   */
  getModels() {
    return this.array;
  }

  /**
   * Internally tries to seal all items contained inside the list and finally seals the list
   */
  seal() {
    // first try to seal individual elements if operation is supported
    Object.values(this.map).forEach(item => item.seal && item.seal());
    // seal the object
    Object.freeze(this);
  }

  /**
   * Clears the list from all items contained inside of it.
   */
  clear() {
    this.map = {};
    this.array = [];
  }

  /**
   * Performs a shallow copy from given list to this. Only the internal
   * storage containers are copied. Items contained inside are copied by
   * reference inside the current list.
   *
   * @param list - {@link ModelList} from which to copy items
   */
  copyFrom(list) {
    this.map = _.clone(list.map);
    this.array = _.clone(list.array);
  }

  /**
   * Returns the size of the list. The size is equal to the
   * number of inserted elements or items inside the list at
   * the time of calling this method.
   *
   * @returns {Number} - size / length of the list
   */
  size() {
    return this.array.length;
  }

  /**
   * Sorts the given model list based on a provided callback function
   *
   * @param callback - used to compute the sorting technique
   */
  sort(callback) {
    this.array.sort(callback);
  }
}

function getModelKey(model, mapper) {
  if (mapper) {
    return _.isFunction(mapper) ? mapper(model) : mapper;
  }
  return model.getId();
}

function arrayRemoveById(array, id, mapper) {
  let index = _.findIndex(array, item => getModelKey(item, mapper) === id);
  array.splice(index, 1);
  return index;
}