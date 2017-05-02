import {OpenUIAction} from 'layout/top-header/main-menu/quick-access/open-ui-action'

describe('Open UI Action', function () {
  describe('Should execute correcly', ()=> {
    it('Should init config with the right parameters', ()=> {
      let windowAdapter = {
        location: {}
      };
      let openAction = new OpenUIAction(windowAdapter);
      let mockActionDefinition = {
        href: '/test/href'
      };
      openAction.execute(mockActionDefinition);
      expect(openAction.windowAdapter.location.href).to.equal('/test/href');
    });
  })
});