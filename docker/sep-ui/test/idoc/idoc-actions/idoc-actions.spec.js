import {IdocActions} from 'idoc/idoc-actions/idoc-actions';
import {PermissionsChangedEvent} from 'idoc/system-tabs/permissions/permissions-changed-event';
import {ActionExecutedEvent, ActionInterruptedEvent} from 'services/actions/events';
import {PromiseStub} from 'promise-stub';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import {MockEventbus} from 'test/test-utils';

describe('IdocActions', function () {

  let idocActions;
  let eventbus = new MockEventbus();
  let timeout = function (callback) {
    callback();
  };

  let actionsService = {
    getActions: sinon.stub().returns(PromiseStub.resolve({data: [{name: 'editDetails'}]})),
    getFlatActions: sinon.stub().returns(PromiseStub.resolve({data: [{name: 'editDetails'}]}))
  };

  function instantiateIdocActions() {
    //Necessary in the constructor
    IdocActions.prototype.actionContext = {
      placeholder: 'idoc-actions',
      idocContext: {
        setShowTemplateSelector: sinon.spy(),
        isPreviewMode: sinon.spy(()=>{return true;})
      }
    };


    let actions = new IdocActions(eventbus, {}, {}, {}, {}, actionsService, PromiseStub, {}, timeout);
    actions.onActionsLoaded = sinon.spy();
    actions.actionConfig = {
      disbleSaveButton: false
    };

    return actions;
  }

  function createIdocContextStub(editAllowed, isPersisted) {
    return {
      getCurrentObject: function () {
        return PromiseStub.resolve({
          id: 'emf:id',
          isPersisted: function () {
            return isPersisted;
          },
          getWriteAllowed: function () {
            return editAllowed;
          }
        });
      }
    };
  }

  let actions = [{
    name: 'editDetails',
    action: 'editDetails',
    disabled: false
  }];

  let extractActionsStub;
  let getActionsConfigStub;
  let getFilterCriteriaSpy;

  beforeEach(() => {
    getActionsConfigStub = sinon.stub(ActionsHelper, 'getActionsLoaderConfig').returns({});
    getFilterCriteriaSpy = sinon.spy(ActionsHelper, 'getFilterCriteria');
  });

  it('should subscribe and ActionExecutedEvent', () => {
    idocActions = instantiateIdocActions();
    let subscribeSpy = sinon.spy(idocActions.eventbus, 'subscribe');
    idocActions.context = createIdocContextStub(true, true);
    idocActions.ngOnInit();
    expect(subscribeSpy.called).to.be.true;
    expect(eventbus.subscribe.args[0][0]).to.equal(ActionExecutedEvent);
    expect(eventbus.subscribe.args[1][0]).to.equal(ActionInterruptedEvent);
  });


  it('should allow edit of idoc if has edit action', () => {
    idocActions = instantiateIdocActions();
    idocActions.context = createIdocContextStub(true, true);
    idocActions.ngOnInit();
    extractActionsStub = sinon.stub(ActionsHelper, 'extractActions').returns(actions);
    expect(idocActions.editAllowed).to.be.true;
    extractActionsStub.restore();
  });

  it('should not allow edit of idoc if it has not edit action', () => {
    idocActions = instantiateIdocActions();
    idocActions.context = createIdocContextStub(false, true);
    idocActions.ngOnInit();
    let noEditActions = [{action: 'approve'}];
    extractActionsStub = sinon.stub(ActionsHelper, 'extractActions').returns(actions);
    expect(idocActions.editAllowed).to.be.false;
    extractActionsStub.restore();
  });

  it('should disable action button when action is executed', () => {
    let idocActions = instantiateIdocActions();
    let action = {};

    idocActions.actionExecutor.execute = sinon.spy();
    idocActions.executeHeaderAction(action, false);

    expect(action.disableButton).to.be.true;
    expect(idocActions.actionExecutor.execute.called).to.be.true;
  });

  it('should call disableSaveAction when save action is executed', () => {
    let idocActions = instantiateIdocActions();
    let action = {};

    idocActions.disableSaveButton = sinon.spy();
    idocActions.actionExecutor.execute = sinon.spy();
    idocActions.executeHeaderAction(action, true);

    expect(action.disableButton).to.be.undefined;
    expect(idocActions.disableSaveButton.called).to.be.true;
    expect(idocActions.actionExecutor.execute.called).to.be.true;
  });

  it('should always call enable action method when action is executed', () => {
    let idocActions = instantiateIdocActions();
    idocActions.context = createIdocContextStub(true, false);
    idocActions.ngOnInit();
    let spy = sinon.spy(idocActions, 'enableHeaderAction');
    idocActions.eventbus.publish(new ActionExecutedEvent({forceRefresh: false}));
    expect(spy.calledOnce).to.be.true;
  });

  it('should enable action button after action is completed or failed', () => {
    let idocActions = instantiateIdocActions();
    idocActions.headerActions = [{name: 'testAction', disableButton: true}];

    idocActions.enableHeaderAction({name: 'testAction'});
    expect(idocActions.headerActions[0].disableButton).to.be.false;
  });

  afterEach(() => {
    getFilterCriteriaSpy.restore();
    getActionsConfigStub.restore();
  });
});