import {ManagePermissionsAction} from 'idoc/actions/manage-permissions-action';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {STATE_PARAM_ID, IDOC_STATE, PERMISSIONS_TAB_ID} from 'idoc/idoc-constants';
import {EDIT_PERMISSIONS_PARAM} from 'idoc/system-tabs/permissions/permissions';

describe('ManagePermissionsAction', ()=> {

  let router = {
    navigate: sinon.spy()
  };

  let stateParamsAdapter = {
    setStateParam: sinon.spy(),
    getStateParams: sinon.spy()
  };

  let managePermissionsAction;

  beforeEach(()=> {
    managePermissionsAction = new ManagePermissionsAction({}, router, stateParamsAdapter, PromiseAdapterMock.mockImmediateAdapter());
  });

  it('should execute the action', ()=> {
    let actionContext = {
      currentObject: {
        getId: ()=> {
          return 0;
        }
      }
    };

    managePermissionsAction.execute({}, actionContext);

    expect(stateParamsAdapter.setStateParam.callCount).to.equal(3);
    expect(stateParamsAdapter.setStateParam.args[0][0]).to.equal(STATE_PARAM_ID);
    expect(stateParamsAdapter.setStateParam.args[1][0]).to.equal('#');
    expect(stateParamsAdapter.setStateParam.args[2][0]).to.equal(EDIT_PERMISSIONS_PARAM);
    expect(router.navigate.called).to.be.true;
    expect(router.navigate.args[0][0]).to.equal(IDOC_STATE);
  });
});