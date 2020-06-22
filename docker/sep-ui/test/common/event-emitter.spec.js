import {EventEmitter, EmittableObject} from 'common/event-emitter';

class TestEmitter extends EventEmitter {
  constructor() {
    super();
  }
}

describe('EventEmitter', function () {
  it('should allow pub/sub', function () {
    var value;

    var emitter = new TestEmitter();
    emitter.subscribe('test', function (payload) {
      value = payload;
    });

    emitter.publish('test', 5);

    expect(value).to.equal(5);
  });

  it('should throw exception when subscribing to a undefined topic',()=>{
    let emitter = new TestEmitter();
    expect(emitter.subscribe).to.throw(Error,"Can't subscribe to undefined topic");
  });

  it('should not allow handlers which are not functions', () => {
    var emitter = new TestEmitter();
    expect(() => { emitter.subscribe('failing subscription', null)},'null').to.throws(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', undefined)},'undefined').to.throw(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', {should: 'error'})},'Object').to.throw(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', 'should throw error')},'string').to.throw(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', ['should', 'throw', 'error'])},'array').to.throw(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', false)},'boolean').to.throw(Error,'The subscription handler must be a function');
    expect(() => { emitter.subscribe('failing subscription', 123)},'numeric').to.throw(Error,'The subscription handler must be a function');
  });

  it('should unsubscribe all subscriptions from event emitter', () => {
    var value;
    var emitter = new TestEmitter();
    var subscriptionFunction = function (payload) {
      value = payload;
    };

    emitter.subscribe('subscription1', subscriptionFunction);
    emitter.subscribe('subscription2', subscriptionFunction);

    emitter.publish('subscription2', 5);
    expect(value).to.equal(5);
    emitter.publish('subscription2', 50);
    expect(value).to.equal(50);

    emitter.unsubscribeAll();
    emitter.publish('subscription1', 100);
    expect(value).to.not.equal(100);
    emitter.publish('subscription2', 200);
    expect(value).to.not.equal(200);
  });

  it('should unsubscribe individual subscriptions using its subscriptionDefinition', () => {
    var value1, value2;
    var emitter = new TestEmitter();
    var firstSubscriptionDefinition = emitter.subscribe('test', (payload) => {
      value1 = payload;
    });
    emitter.subscribe('test', (payload) => {
      value2 = payload;
    });
    emitter.publish('test', 5);
    expect(value1).to.equal(5);
    expect(value2).to.equal(5);
    firstSubscriptionDefinition.unsubscribe();
    emitter.publish('test', 50);
    expect(value1).to.equal(5);
    expect(value2).to.equal(50);
  })
});

describe('EmittableObject', () => {
  let testObject;
  let testEmittableObject;
  beforeEach(() => {
    testObject = {testField: 'test', testField2: 'test2', testField3: 'test'};
    testEmittableObject = new EmittableObject(testObject);
  });

  it('should create emittable object', () => {
    expect(testEmittableObject instanceof EventEmitter).to.be.true;
    Object.keys(testObject).forEach((field) => {
      expect(testEmittableObject[field]).to.eql(testObject[field]);
    });
  });
  it('should emmit on property changed', () => {
    let newValue = 'newValue';
    testEmittableObject.subscribe('propertyChanged', (propertyChanged) => {
      expect(propertyChanged).to.eql({testField: newValue});
    });
    testEmittableObject.testField = newValue;
  });

  it('should not emmit when the property changed is the same as the previous one', () => {
    let val;
    testEmittableObject.subscribe('propertyChanged', (propertyChanged) => {
      val = propertyChanged;
    });
    testEmittableObject.testField = 'test';
    expect(val).to.be.undefined;
  });
});