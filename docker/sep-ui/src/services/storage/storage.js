export class Storage {

  constructor(storage) {
    this.storage = storage;
  }

  key(index) {
    return this.storage.key(index);
  }

  get(key) {
    return this.storage.getItem(key);
  }

  set(key, value) {
    let _value = value;
    if(_value && typeof _value === 'object') {
      _value = JSON.stringify(_value);
    }
    this.storage.setItem(key, _value);
  }

  remove(key) {
    this.storage.removeItem(key);
  }

  clear() {
    this.storage.clear();
  }

  length() {
    return this.storage.length;
  }

  getJson(key, defaultValue) {
    var value = this.get(key);
    if (!value) {
      return defaultValue || null;
    }
    return JSON.parse(value);
  }

  getNumber(key) {
    let value = this.get(key);
    return parseInt(value, 10) || value;
  }
}