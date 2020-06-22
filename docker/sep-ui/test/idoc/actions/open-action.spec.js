import {OpenAction} from 'idoc/actions/open-action';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {MODE_PREVIEW, IDOC_STATE} from 'idoc/idoc-constants';

const TEST_ID = 'emf:123';

describe('OpenAction', () => {
  it('should navigate to landing page', () => {
    var action = new OpenAction(StateParamsAdapterMock.mockAdapter(), getRouterMock(), PromiseAdapterMock.mockImmediateAdapter());
    action.execute({}, getContext());
    var spy = action.router.navigate;
    expect(spy.calledOnce).to.be.true;
    expect(spy.getCall(0).args[0]).to.be.equal(IDOC_STATE);

    var expected = {
      mode: MODE_PREVIEW,
      id: TEST_ID
    };
    expect(spy.getCall(0).args[1]).to.deep.equal(expected);
  });
});

function getRouterMock() {
  return {
    navigate: sinon.spy()
  };
}

function getContext() {
  return {
    currentObject: new InstanceObject(TEST_ID)
  };
}