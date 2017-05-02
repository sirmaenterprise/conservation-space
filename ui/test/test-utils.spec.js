import {stub} from 'test/test-utils';

describe('TestUtils', () => {

  describe('stub(type)', () => {
    var stubbed;
    beforeEach(() => {
      stubbed = stub(TestClass);
    });
    it('should allow changing existing methods', () => {
      stubbed.test = () => {
        return 456;
      };
      expect(stubbed.test()).to.equal(456);
    });

    it('should not allow adding new methods', () => {
      expect(() => {
        stubbed.missing = () => {};
      }).to.throw(Error);
    });

    it('should not allow removing existing methods', () => {
      expect(() => {
        delete stubbed.test
      }).to.throw(Error);
    });

    it('should stub both own methods and those from the super class', () => {
      stubbed = stub(SecondTestClass);
      expect(stubbed.test).to.exist;
      expect(stubbed.anotherTest).to.exist;
    });
  });

});

class TestClass {
  test() {
    return 123;
  }
}

class SecondTestClass extends TestClass {
  anotherTest() {
    return 'abv';
  }
}