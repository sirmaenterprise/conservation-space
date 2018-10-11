import {decorate, filterBy, getNestedObjectValue} from 'common/object-utils';

describe('Object utils', function () {

  describe('Decorated object', function () {
    class TestClass {
      constructor(param) {
        this.param = param;
      }

      say(message) {
        this.message = message;
        return 'Hello ' + message;
      }

      sayLast() {
        return 'Last message: ' + this.message;
      }
    }

    it('should directly call undecorated methods', function () {
      var originalObject = new TestClass('Test');

      var decorator = decorate(originalObject, {});

      expect(decorator.say('World!')).to.equal('Hello World!');
    });

    it('should decorated methods', function () {
      var originalObject = new TestClass('Test');

      var decorator = decorate(originalObject, {
        'sayLast': function () {
          return 'Decorated ' + originalObject.sayLast();
        }
      });

      decorator.say('World!');
      expect(decorator.sayLast()).to.equal('Decorated Last message: World!');
    });

    it('should proxy the properties of the decorated object', function () {
      var originalObject = new TestClass('Test');

      originalObject.property1 = 'test1';
      originalObject.property2 = 'test2';

      var decorator = decorate(originalObject, {});

      decorator.property1 = 'new_test1';

      expect(originalObject.property1).to.equal('new_test1');
      expect(decorator.property2).to.equal('test2');
    });

    it('should be able to provide the original object', function () {
      var originalObject = new TestClass('Test');

      var decorator = decorate(originalObject, {});

      expect(decorator.getWrappedObject()).to.equal(originalObject);
    });

    it('should be able to undecorate the decorated methods', function () {
      var originalObject = new TestClass('Test');

      var decorator = decorate(originalObject, {
        'sayLast': function () {
          return 'Decorated ' + originalObject.sayLast();
        }
      });

      decorator.say('World!');

      decorator.undecorate();

      expect(decorator.sayLast()).to.equal('Last message: World!');
    });
  });

  describe('FilterBy', function() {
    it('should return the same object if filterFunc is undefined', function () {
      let obj = {
        a: 1,
        b: 2
      };
      expect(filterBy(obj)).to.equals(obj);
    });

    it('should filter the object by filterFunc', function () {
      let obj = {
        a: 1,
        b: 2,
        c: 3
      };

      let filterFunc = (value, key) => {
        return value > 1 && key === 'c';
      };
      expect(filterBy(obj, filterFunc)).to.deep.equals({
        c: 3
      });
    });
  });

  describe('getNestedObjectValue', () => {
    it('should return undefined if passed object is null', () => {
      let nestedObject = null;
      expect(getNestedObjectValue(nestedObject, ['67890', 'header']) === undefined).to.be.true;
    });

    it('should return undefined if passed object is undefined', () => {
      let nestedObject = undefined;
      expect(getNestedObjectValue(nestedObject, ['67890', 'header']) === undefined).to.be.true;
    });

    it('should return undefined if passed path not exist', () => {
      let nestedObject = {
        '12345': {id: '12345', headers: 'header html 12345'},
        '67890': {id: '67890', headers: 'header html 67890'},
        '11111': {id: '11111', headers: 'header html 111111'}
      };
      expect(getNestedObjectValue(nestedObject, ['AAAAA', 'header']) === undefined).to.be.true;
    });

    it('should return value', () => {
      let nestedObject = {
        '12345': {id: '12345', headers: 'header html 12345'},
        '67890': {id: '67890', headers: 'header html 67890'},
        '11111': {id: '11111', headers: 'header html 111111'}
      };
      expect(getNestedObjectValue(nestedObject, ['67890', 'headers'])).to.equal('header html 67890');
    });
  });

});