import {ChangeTemplateAction} from 'idoc/actions/change-template-action';
import {InstanceObject} from 'models/instance-object';
import {
  STATE_PARAM_MODE,
  STATE_PARAM_ID,
  MODE_EDIT,
  IDOC_STATE,
  SHOW_TEMPLATE_SELECTOR
} from 'idoc/idoc-constants';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('Edit template action', () => {

  const IDOC_ID = 'emf:123456';

  it('should open idoc in edit mode with template selector if no idoc page controller', () => {
    let currentObject = new InstanceObject(IDOC_ID, IdocMocks.generateModels(), IdocMocks.generateIntialContent());
    let router = getRouterMock();
    let stateParamsAdapter = StateParamsAdapterMock.mockAdapter();
    var sessionStorageService = getSessionStorageServiceMock();


    let handler = new ChangeTemplateAction(router, stateParamsAdapter, {}, {}, {}, {}, PromiseAdapterMock.mockImmediateAdapter(), {}, sessionStorageService);
    handler.execute({
      operation: 'editTemplate',
      action: 'editTemplate'
    }, {
      currentObject: currentObject
    });

    let stateParams = stateParamsAdapter.getStateParams();
    let sessionStorageParam = sessionStorageService.get(SHOW_TEMPLATE_SELECTOR);
    expect(router.navigate.called).to.be.true;
    let routerArgs = router.navigate.getCall(0).args;
    expect(stateParams[STATE_PARAM_MODE]).to.equal(MODE_EDIT);
    expect(stateParams[STATE_PARAM_ID]).to.equal(IDOC_ID);
    expect(sessionStorageParam).to.equal(true);
    expect(routerArgs[0]).to.equal(IDOC_STATE);
    expect(routerArgs[2]).to.deep.equal({notify: true});
  });
});

function getRouterMock() {
  return {
    navigate: sinon.spy()
  }
}

function getSessionStorageServiceMock() {
  let params = {};
  return {
    set: (key, value) => {
      params[key] = value;
    },
    get: (key) => {
      return params[key];
    }
  };
}