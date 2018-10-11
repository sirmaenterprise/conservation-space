import {EditDetailsAction} from 'idoc/actions/edit-details-action';
import {InstanceObject} from 'models/instance-object';
import {STATE_PARAM_MODE, STATE_PARAM_ID, MODE_EDIT, MODE_PREVIEW, IDOC_STATE} from 'idoc/idoc-constants';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('Edit details action', () => {

  const IDOC_ID = 'emf:123456';

  it('should open idoc in edit mode if no idoc page controller', () => {
    let currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let router = getRouterMock();
    let stateParamsAdapter = StateParamsAdapterMock.mockAdapter();

    let handler = new EditDetailsAction(router, stateParamsAdapter, {}, {}, {}, {}, PromiseAdapterMock.mockImmediateAdapter());
    handler.execute({
      operation: 'editDetails',
      action: 'editDetails'
    }, {
      currentObject: currentObject
    });

    let stateParams = stateParamsAdapter.getStateParams();
    expect(router.navigate.called).to.be.true;
    let routerArgs = router.navigate.getCall(0).args;
    expect(stateParams[STATE_PARAM_MODE]).to.equal(MODE_EDIT);
    expect(stateParams[STATE_PARAM_ID]).to.equal(IDOC_ID);
    expect(routerArgs[0]).to.equal(IDOC_STATE);
    expect(routerArgs[2]).to.deep.equal({notify: true});
  });

  it('should invoke EditIdocAction if there is idoc page controller', () => {
    let currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let actionsService = IdocMocks.mockActionsService();
    let actionsServiceLockSpy = sinon.spy(actionsService, "lock");

    let handler = new EditDetailsAction(getRouterMock(), StateParamsAdapterMock.mockAdapter(), IdocMocks.mockInstanceRestService(IDOC_ID), IdocMocks.mockEventBus(),
      IdocMocks.mockLogger(), actionsService, PromiseAdapterMock.mockAdapter(), IdocMocks.mockIdocDraftService());
    handler.execute({
      operation: 'editDetails',
      action: 'editDetails'
    }, {
      currentObject: currentObject,
      idocPageController: {},
      idocContext: {
        reloadObjectDetails: () => PromiseStub.resolve()
      }
    });

    expect(actionsServiceLockSpy.calledOnce).to.be.true;
  });

});

function getRouterMock() {
  return {
    navigate: sinon.spy()
  }
}