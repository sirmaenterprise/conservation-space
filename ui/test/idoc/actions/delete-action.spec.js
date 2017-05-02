import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseStub} from 'test/promise-stub'
import {DeleteAction} from 'idoc/actions/delete-action';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {STATE_PARAM_ID} from "idoc/idoc-page";

describe('DeleteAction', () => {

  const ID = 'emf:123456';
  const PREVIOUS_ID = 'emf:qwerty';

  let handler;
  let breadcrumbEntryManager;
  let eventbus = new Eventbus();
  let actionDefinition = {
    action: 'deleteAction'
  };
  let context = {
    currentObject: {
      getId: () => {
        return ID
      }
    },
    placeholder: IDOC_PAGE_ACTIONS_PLACEHOLDER
  };

  beforeEach(() => {
    let instanceRestService = {
      deleteInstance: () => {
        return PromiseStub.resolve({})
      }
    };
    let logger = {};
    let notificationService = {
      success: () => {
      }
    };
    let translateService = {
      translateInstant: () => {
      }
    };
    breadcrumbEntryManager = {
      getPreviousEntry: () => {
        return {
          getId: () => PREVIOUS_ID
        }
      },
      back: () => {

      }
    };
    let router = {
      navigate: sinon.spy()
    };
    handler = new DeleteAction(instanceRestService, logger, notificationService, translateService, breadcrumbEntryManager, eventbus, router);
  });

  it('should call service for delete with the object id', () => {
    let spyDeleteInstance = sinon.spy(handler.instanceRestService, 'deleteInstance');
    handler.execute(actionDefinition, context);
    expect(spyDeleteInstance.calledOnce).to.be.true;
    expect(spyDeleteInstance.getCall(0).args).to.eql([ID])
  });

  it('should fire AfterIdocDeletedEvent if successful', () => {
    var spyEventHandler = sinon.spy();
    eventbus.subscribe(AfterIdocDeleteEvent, spyEventHandler);
    handler.execute(actionDefinition, context);
    expect(spyEventHandler.calledOnce).to.be.true;
  });

  it('should rise notification for success', () => {
    let spySuccess = sinon.spy(handler.notificationService, 'success');
    handler.execute(actionDefinition, context);
    expect(spySuccess.calledOnce).to.be.true;
  });

  it('should perform navigation to previous state if executed from the object\'s landing page and there is previous state', () => {
    let expectedStateParams = {};
    expectedStateParams[STATE_PARAM_ID] = PREVIOUS_ID;
    let expectedOptions = {
      reload: true,
      inherit: false
    };

    handler.execute(actionDefinition, context);

    expect(handler.router.navigate.called).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal('idoc');
    expect(handler.router.navigate.getCall(0).args[1]).to.deep.equal(expectedStateParams);
    expect(handler.router.navigate.getCall(0).args[2]).to.deep.equal(expectedOptions);
  });

  it('should navigate to user dashboard if executed from object\'s landing page but there is no previous state', () => {
    let stubGetPreviousState = sinon.stub(breadcrumbEntryManager, 'getPreviousEntry', () => {
      return undefined;
    });
    handler.execute(actionDefinition, context);

    expect(handler.router.navigate.called).to.be.true;
    expect(handler.router.navigate.getCall(0).args[0]).to.equal('userDashboard');

    stubGetPreviousState.reset();
  });

  it('should not navigate to previous state if executed not from object\'s landing page', () => {
    context.placeholder = 'search.actions.menu';
    handler.execute(actionDefinition, context);
    expect(handler.router.navigate.called).to.be.false;
  });
});