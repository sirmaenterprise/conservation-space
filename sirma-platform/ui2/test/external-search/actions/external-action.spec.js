import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {ExternalAction} from 'external-search/actions/external-action';
import {StatusCodes} from 'services/rest/status-codes';

describe('ExternalAction search spec', () => {

  let notificationService;
  let externalObjectService;
  let translateService;
  let eventbus;
  let actions;
  let context = {
    currentObject: {
      models: {
        instance: undefined
      }
    }
  };

  beforeEach(() => {
    let scope = mock$scope();
    actions = new ExternalAction(scope);
    notificationService = mockNotificationService();
    actions.notificationService = notificationService;
    translateService = moctTranslationService();
    actions.translateService = translateService;
    eventbus = mockEventBus({});
    actions.eventbus = eventbus;
  });


  it('Test success execute', () => {
    actions.externalObjectService = mockExtrnalObjectService({
      data: {
        cause: undefined
      }
    });
    actions.execute(null, context);
    expect(notificationService.success.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

  it('Test error ', () => {
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


  it('Test warning ', () => {
    actions.externalObjectService = mockExtrnalObjectService({
      status : StatusCodes.FORBIDDEN,
      data: {
        cause: {
          message: "random error message"
        }
      }
    });
    actions.execute(null, context);
    expect(notificationService.warning.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

  it('Test reject ', () => {
    actions.externalObjectService = mockExtrnalObjectServiceError({
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

  it('Test extraction ', () => {
    actions.externalObjectService = mockExtrnalObjectService({
      data: {
        cause: undefined
      }
    });
    let object = {
      currentObject: {
        models: {
          instance: {
            data: {}
          }
        }
      }
    };
    actions.execute(null, object);
    expect(notificationService.success.calledOnce).to.be.true;
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
      }),
      warning: sinon.spy((ms) => {
        return PromiseStub.resolve(ms);
      })
    }
  }

  function mockExtrnalObjectService(response) {
    return {
      importObjects: sinon.spy((data) => {
        return PromiseStub.resolve(response);
      })
    }
  }

  function mockExtrnalObjectServiceError(response) {
    return {
      importObjects: sinon.spy((data) => {
        return PromiseStub.reject(response);
      })
    }
  }

  function moctTranslationService() {
    return {
      translateInstant: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    }
  }

});