import{Event} from 'app/app';
import {stub, MockEventbus} from 'test/test-utils';

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
        stubbed.missing = () => {
        };
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

  describe('MockEventbus', () => {
    it('should subscribe and emit event', () => {
      let eventbus = new MockEventbus();
      let callbackSpy = sinon.spy();
      eventbus.subscribe(TestEvent, callbackSpy);
      eventbus.subscribe(TestEvent, callbackSpy);
      eventbus.publish(new TestEvent());
      expect(callbackSpy.calledTwice).to.be.true;
    });

    it('should reset eventub',()=>{
      let eventbus = new MockEventbus();
      let callbackSpy = sinon.spy();
      eventbus.subscribe(TestEvent, callbackSpy);
      eventbus.subscribe(TestEvent, callbackSpy);
      eventbus.reset();
      expect(Object.keys(eventbus.subscriptions).length).to.equal(0);
    })
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

@Event()
class TestEvent {
}