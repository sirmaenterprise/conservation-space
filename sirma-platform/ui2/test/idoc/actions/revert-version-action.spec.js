import {PromiseStub} from 'test/promise-stub'
import {RevertVersionAction} from 'idoc/actions/revert-version-action';

describe('RevertVersionAction', () => {

  let action;
  let actionDefinition = {
    action: 'revertVersion'
  };

  let context = {
    currentObject: {
      getId: () => {
        return 'instance-id-v1.6';
      }
    }
  };

  let router;
  let stateParamsAdapter;
  let notificationService;
  let translateService;

  beforeEach(() => {
    let logger = {};
    router = {
      navigate: sinon.spy()
    };
    stateParamsAdapter = {
      setStateParam: sinon.spy(),
      getStateParams: sinon.spy()
    };
    notificationService = {
      success: sinon.spy()
    };
    translateService = {
      translateInstant: sinon.spy()
    };
    let actionService = {
      revertVersion: () => {
        return PromiseStub.resolve({
          data: {
            id: 'instance-id'
          }
        })
      }
    };
    action = new RevertVersionAction(logger, actionService, router, stateParamsAdapter, notificationService, translateService);
  });

  it('should call service for revert version with the version id', () => {
    let revertVersionSpy = sinon.spy(action.actionService, 'revertVersion');
    action.execute(actionDefinition, context);
    expect(revertVersionSpy.calledOnce).to.be.true;
    expect(revertVersionSpy.getCall(0).args[0]).to.eql('instance-id-v1.6');
    expect(revertVersionSpy.getCall(0).args[1]).to.eql({userOperation:'revertVersion'});
  });

  it('should not call internal services when revert is not successful', () => {
    action.actionService.revertVersion = ()=> {
      return PromiseStub.reject({})
    };
    action.execute(actionDefinition, context);
    expect(action.stateParamsAdapter.setStateParam.notCalled).to.be.true;
    expect(action.stateParamsAdapter.getStateParams.notCalled).to.be.true;
    expect(action.router.navigate.notCalled).to.be.true;
    expect(action.translateService.translateInstant.notCalled).to.be.true;
    expect(action.notificationService.success.notCalled).to.be.true;
  });

  it('should call internal services when revert is successful', () => {
    action.execute(actionDefinition, context);
    expect(action.stateParamsAdapter.setStateParam.calledOnce).to.be.true;
    expect(action.stateParamsAdapter.getStateParams.calledOnce).to.be.true;
    expect(action.router.navigate.calledOnce).to.be.true;
    expect(action.translateService.translateInstant.calledOnce).to.be.true;
    expect(action.notificationService.success.calledOnce).to.be.true;
  });
});
