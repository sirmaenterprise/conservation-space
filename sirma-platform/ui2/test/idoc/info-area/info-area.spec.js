import {InfoArea} from 'idoc/info-area/info-area';

describe('InfoArea', ()=> {

  let scope = {
    $apply: ()=> {
    }
  };

  class Eventbus {
    constructor() {
      this.subscriptions = [];
    }

    subscribe(event) {
      this.subscriptions = (event);
    }
  }

  it('should not allow empty messages to be added', ()=> {
    let eventbus = new Eventbus();
    let infoArea = new InfoArea(scope, eventbus);
    let mockMessage = '';
    infoArea.addMessage(mockMessage);
    expect(Object.keys(infoArea.messages)).has.length(0);
  });

  it('should add message after event', ()=> {
    let eventbus = new Eventbus();
    let infoArea = new InfoArea(scope, eventbus);
    let callback = eventbus.subscriptions.callback;
    let mockData = {
      id: 'mockId',
      message: 'mock'
    };
    callback(mockData);
    expect(infoArea.messages['mockId']).to.equals('mock');
  });

  it('should unsubscribe from event when component is destroyed', () => {
    let unsubscribeSpy = sinon.spy();
    let eventbus = {
      subscribe: () => {
        return {
          unsubscribe: unsubscribeSpy
        };
      }
    };
    let infoArea = new InfoArea(scope, eventbus);
    infoArea.ngOnDestroy();
    expect(unsubscribeSpy.calledOnce).to.be.true;
  });

});
