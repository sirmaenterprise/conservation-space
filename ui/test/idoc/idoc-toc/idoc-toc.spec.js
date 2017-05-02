import {IdocToC} from 'idoc/idoc-toc/idoc-toc';

describe('Tests for idoc toc', function () {

  before(function () {
    fixture.setBase('test/idoc/idoc-toc')
  });

  beforeEach(function () {
    this.template = fixture.load('idoc-toc-dom-manipulator.html');
  });

  after(function () {
    fixture.cleanup()
  });

  it('should remove collapse container dom element and unsubscribe from all events', function () {
    var config = {
      source: 'deleteCollapseContainer',
      eventbus: {
        instance: new MockEventbus(),
        channel: 'mock'
      }
    };
    var toc = new IdocToC(config);
    toc.destroy();
    toc.events.forEach(function (event) {
      expect(event.unsubscribe.called).to.be.true;
    });
    expect($('#deleteCollapseContainer').length).to.be.zero;
  });

});

class MockEventbus {
  subscribe() {
    return {
      unsubscribe: sinon.spy()
    }
  }
}