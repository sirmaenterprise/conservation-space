import postal from 'postaljs/postal.js';
import {Eventbus} from 'services/eventbus/eventbus';

describe('Tests for the Eventbus service', function() {

  var eventbus = new Eventbus();
  var channel = postal.channel();
  var spyPostalSubscribe = sinon.spy(postal, 'subscribe');
  var spyEventbusPublish = sinon.spy(eventbus, 'executePublish');
  
  beforeEach(function() {
    spyEventbusPublish.reset();
    spyPostalSubscribe.reset();
  });
  
  describe('Tests for the Eventbus.subscribe method', function() {
    it('Test if subscribe() throws Error when arguments.length = 0', function() {
      expect(eventbus.subscribe.bind(eventbus.subscribe)).to.throw(Error);
    });

    it('Test if subscribe(1, 2, 3) throws Error when arguments.length > 2', function() {
      expect(function() { eventbus.subscribe(1, 2, 3) }).to.throw(Error);
    });
    
    it('Test if subscribe({ channel: channel, topic: topic, callback: callback }) calls postal with correct argument', function() {
      var bus = new Eventbus();
      var opts = {
          topic: 'user',
          callback: function(data, envelope) {}
      };
      bus.subscribe(opts);
      
      var actualArg = spyPostalSubscribe.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'user');
      
      opts.channel = 'idoc';
      bus.subscribe(opts);
      actualArg = spyPostalSubscribe.getCall(0).args[0];
      expect(actualArg).to.have.property('channel', 'idoc');
    });
    
    it('Test if subscribe("topic", callback) calls postal with correct argument', function() {
      var bus = new Eventbus();
      var callback = function() {}
      bus.subscribe('topicname', callback);
      
      var actualArg = spyPostalSubscribe.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'topicname');
    });
    
    it('Test if subscribe(MyEvent, callback) calls postal with correct argument', function() {
      var bus = new Eventbus();
      var callback = function() {};
      function MyEvent() {}
      MyEvent.EVENT_NAME = 'topic.name';
      bus.subscribe(MyEvent, callback);
      
      var actualArg = spyPostalSubscribe.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'topic.name');
    });
    
    it('Test if eventbus.subscribeForContext does call the subscribe with topic argument sufixed .#', function() {
      var bus = new Eventbus();
      var opts = {
          topic: 'user',
          callback: function(data, envelope) {}
      };
      var spy = sinon.spy(bus, 'subscribe');
      bus.subscribeForContext(opts);
      
      var actualArg = spy.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'user.#');
    });
  });
  
  describe('Tests for the Eventbus.publish method', function() {
    it('Test if eventbus.publish throws TypeError when argument is not an object', function() {
      expect(eventbus.publish.bind(eventbus.publish, 1)).to.throw(TypeError);
      expect(eventbus.publish.bind(eventbus.publish, '1')).to.throw(TypeError);
      expect(eventbus.publish.bind(eventbus.publish, function() {})).to.throw(TypeError);
    });
    
    it('Test if eventbus.publish calls the library publish method', function() {
      var opts = {
          topic: 'topic.name',
          data: {}
      };
      var spy = sinon.spy(eventbus, 'publish');
      eventbus.publish(opts);
      
      expect(spy.callCount).to.equal(1);
    });
    
    it('Test if eventbus.publish({ topic: topic, data: data}) calls postal with correct argument on default channel "/"', function() {
      var opts = {
          topic: 'topic.name',
          data: {}
      };
      eventbus.publish(opts);
      var actualArg = spyEventbusPublish.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'topic.name');
      expect(actualArg).to.have.property('data');
      expect(actualArg).to.have.property('channel', '/');
    });
    
    it('Test if eventbus.publish({ channel: channel, data: data}) calls postal with correct argument', function() {
      var opts = {
          channel: 'channel.name',
          data: {}
      };
      eventbus.publish(opts);
      var actualArg = spyEventbusPublish.getCall(0).args[0];
      expect(actualArg.topic).to.be.undefined;
      expect(actualArg).to.have.property('data');
      expect(actualArg).to.have.property('channel', 'channel.name');
    });
    
    it('Test if eventbus.publish({ channel: channel, topic: topic }) calls postal with correct argument', function() {
      var opts = {
          channel: 'channel.name',
          topic: 'topic.name'
      };
      eventbus.publish(opts);
      var actualArg = spyEventbusPublish.getCall(0).args[0];
      expect(actualArg).to.have.property('topic', 'topic.name');
      expect(actualArg).to.have.property('channel', 'channel.name');
      expect(actualArg.data).to.be.undefined;
    });
  });
  
  it('Test if eventbus.unsubscribe calls the library unsubscribe method', function() {
    var opts = {
        topic: 'user.log.in',
        callback: function(data, envelope) {}
    };
    var spy = sinon.spy(eventbus, 'unsubscribe');
    var subscription = eventbus.subscribe(opts);
    eventbus.unsubscribe(subscription);
    
    expect(spy.callCount).to.equal(1);
  });
  
  it('Test if eventbus.channel returns the default channel', function() {
    var channel = eventbus.channel();
    expect(channel).to.be.defined;
    expect(channel).to.have.property('channel', '/');
  });
  
  it('Test if eventbus.channel("user") returns the "user" channel', function() {
    var channel = eventbus.channel('user');
    expect(channel).to.be.defined;
    expect(channel).to.have.property('channel', 'user');
  });
  
});