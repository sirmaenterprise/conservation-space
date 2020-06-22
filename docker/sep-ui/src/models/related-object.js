/**
 * Wrapper for a related object model. These are so called object properties.
 */
export class RelatedObject {

  constructor(model) {
    this.model = model;
  }

  getValue() {
    if(this.model.results) {
      return this.model.results;
    }
    return [];
  }

  getFirstValue() {
    let value = this.getValue();
    return value.length ? value[0] : null;
  }

  getTotal() {
    return this.model.total || 0;
  }

  setTotal(count) {
    this.model.total = count;
  }
}