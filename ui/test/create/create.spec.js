import {CreateUrlHandler} from 'create/create-url-handler';

describe('Create component', function () {

  it('should open the entity creation dialog when creating a new entity', function () {
    const PARENT_ID = 'emf:9a25c5b8-9f41-4f89-832e-42dd9df30a8b';
    const RETURN_URL = 'http://localhost:8080/return';
    const $scope = '$sc0pe';

    var windowAdapter = constructWindow(`http://localhost:5000/#/create?parentId=${PARENT_ID}&return-url=${RETURN_URL}`);
    var createPanelService = {};
    createPanelService.openCreateInstanceDialog = sinon.spy();

    var open = new CreateUrlHandler(createPanelService, $scope, windowAdapter);

    expect(createPanelService.openCreateInstanceDialog.getCall(0).args[0].parentId).to.equal(PARENT_ID);
    expect(createPanelService.openCreateInstanceDialog.getCall(0).args[0].returnUrl).to.equal(RETURN_URL);
    expect(createPanelService.openCreateInstanceDialog.getCall(0).args[0].scope).to.equal($scope);
  });

});

function constructWindow(href) {
  return {
    'location': {
      'href': href
    }
  };
}
