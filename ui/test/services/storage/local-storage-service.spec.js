import {LocalStorageService} from 'services/storage/local-storage-service';

describe('LocalStorageService', function() {

  var storage = new LocalStorageService(window);

  beforeEach(function() {
    storage.clear();
  });

  describe('#key', function () {
    it('should return the key name by its index from the storage', function () {
      storage.set('one', 1);
      var keyName = storage.key(0);
      expect(keyName).to.equal('one');
    });

    it('should return null if no key on that index in the storage', function () {
      var keyName = storage.key(5);
      assert(keyName === null);
    });
  });

  describe('#setItem', function () {
    it('should store a value under provided key', function () {
      storage.set('one', 1);
      expect(storage.get('one')).to.equal('1');
    });
    it('should update a value under provided key', function () {
      storage.set('one', 1);
      storage.set('one', 'one');
      expect(storage.get('one')).to.equal('one');
    });
  });

  describe('#getItem', function () {
    it('should return an item by its key from the storage', function () {
      storage.set('one', 1);
      expect(storage.get('one')).to.equal('1');
    });
    it('should return null if no item is found in the storage', function () {
      storage.set('one', 1);
      assert(storage.get('two') === null);
    });
  });

  describe('#getJson', function () {
    it('should return an item as json object', function () {
      storage.set('one', {
        'one': 1
      });
      expect(storage.getJson('one')).to.deep.equal({
        'one': 1
      });
    });
  });

  describe('#getNumber', function () {
    it('should return an item as number if can conversion using parseInt(value, 10) does not return NaN', function () {
      storage.set('one', 12);
      expect(storage.getNumber('one')).to.equal(12);
    });
    it('should return the original not converted value if it can not be converted to a number', function () {
      storage.set('one', 'a12');
      expect(storage.getNumber('one')).to.equal('a12');
    });
  });

  describe('#removeItem', function () {
    it('should remove an item from the storage by its key', function () {
      storage.set('one', 1);
      storage.remove('one');
      assert(storage.get('one') === null);
    });
  });

  describe('#clear', function () {
    it('should remove all items from the storage', function () {
      storage.set('one', 1);
      storage.set('two', 2);
      storage.clear();
      expect(storage.length()).to.equal(0);
      assert(storage.get('one') === null);
      assert(storage.get('two') === null);
    });
  });

  describe('#length', function () {
    it('should return the storage size', function () {
      expect(storage.length()).to.equal(0);
      storage.set('one', 1);
      expect(storage.length()).to.equal(1);
    });
  });

  describe('Inserting values in store', () => {
    it(' should merge values in store', () => {
      let expected = {'idoc' : {'prop1': {} , 'prop2': {}}};
      let item1 = {'idoc' : {'prop1': {}}};
      let item2 = {'idoc' : {'prop2': {}}};
      storage.mergeValues('testStore', item1);
      storage.mergeValues('testStore', item2);
      expect(storage.getJson('testStore', {})).to.deep.equal(expected);
      storage.clear();
    });

    it(' should overide in store', () => {
      let expected = {'idoc' : {'prop1': {'boolean' : true} }};
      let expectedFalse = {'idoc' : {'prop1': {'boolean' : false} }};
      let item1 = {'idoc' : {'prop1': {'boolean' : true}}};
      let item2 = {'idoc' : {'prop1': {'boolean' : false}}};
      storage.mergeValues('testStore', item1);
      expect(storage.getJson('testStore', {})).to.deep.equal(expected);
      storage.mergeValues('testStore', item2);
      expect(storage.getJson('testStore', {})).to.deep.equal(expectedFalse);
      storage.clear();
    });

  });

});