import {WindowResizedHandler} from 'layout/window-resized/window-resized-handler';
import _ from 'lodash';

describe('WindowResizedHandler', () => {

  it('should fire WindowResizedEvent when window size is changed', () => {
    var debounceStub = sinon.stub(_, 'debounce', function (inputFunc) {
      return inputFunc;
    });
    let eventbus = {
      publish: sinon.spy()
    };
    let handler = new WindowResizedHandler(eventbus);
    $(window).trigger('resize');
    expect(debounceStub.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

});