import {OpenUIAction} from 'layout/top-header/main-menu/quick-access/open-ui-action';
import {Router} from 'adapters/router/router';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'promise-stub';

describe('Open UI Action', function () {
  it('Should execute open ui action correctly', () => {
    let routerStub = stub(Router);
    let openAction = new OpenUIAction(routerStub, PromiseStub);

    let mockActionDefinition = {
      state: 'ui-test-configuration',
      params: {q: 'sample-param'}
    };
    openAction.execute(mockActionDefinition);
    expect(routerStub.navigate.calledOnce).to.be.true;
    expect(routerStub.navigate.calledWith('ui-test-configuration', {q: 'sample-param'}, {reload: true})).to.be.true;
  });
});