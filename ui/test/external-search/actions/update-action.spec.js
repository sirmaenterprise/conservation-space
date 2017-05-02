import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {UpdateIntAction} from 'external-search/actions/update-action';

describe('UpdateAction search spec', () => {

  let notificationService;
  let externalObjectService;
  let windowAdapter;
  let translateService;
  let eventbus;
  let actions;

  beforeEach(() => {
    let scope = mock$scope();
    actions = new UpdateIntAction(scope);
    notificationService = mockNotificationService();
    actions.notificationService = notificationService;
    translateService = moctTranslationService();
    actions.translateService = translateService;
    eventbus = mockEventBus({});
    actions.eventbus = eventbus;
    windowAdapter = mockWindowAdapter();
    actions.windowAdapter = windowAdapter;
  });


  it('Test success execute', () => {
    let context = {};
    context.currentObject = {
      models: {
        instance: undefined
      }
    };
    actions.externalObjectService = mockExtrnalObjectService({
      data: {
        cause: undefined
      }
    });
    actions.execute(null, context);
    expect(notificationService.success.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

  it('Test success execute with reload', () => {
    let context = {
      idocContext: 'Context'
    };
    context.currentObject = {
      models: {
        instance: undefined
      }
    };
    actions.externalObjectService = mockExtrnalObjectService({
      data: {
        cause: undefined
      }
    });
    actions.execute(null, context);
    expect(notificationService.success.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
    expect(windowAdapter.location.reload.calledOnce).to.be.true;
  });

  it('Test error ', () => {
    let context = {};
    context.currentObject = {
      models: {
        instance: undefined
      }
    };
    actions.externalObjectService = mockExtrnalObjectService({
      data: {
        cause: {
          message: "random error message"
        }
      }
    });
    actions.execute(null, context);
    expect(notificationService.error.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

  function mockEventBus(ev) {
    return {
      publish: sinon.spy((ev) => {
        return PromiseStub.resolve(ev);
      })
    };
  };

  function mockNotificationService() {
    return {
      error: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      }),
      success: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      })
    };
  }

  function mockExtrnalObjectService(response) {
    return {
      importObjects: sinon.spy((data) => {
        return PromiseStub.resolve(response);
      })
    }
  }

  function moctTranslationService() {
    return {
      translateInstant: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    };
  }

  function mockWindowAdapter() {
    return {
      location: {
        reload: sinon.spy(() => {
          return PromiseStub.resolve();
        })
      }
    };
  }

});