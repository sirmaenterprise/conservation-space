import {decorate} from 'common/object-utils';

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
    })
  });

});