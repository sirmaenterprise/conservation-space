import {AdminMenu} from 'layout/top-header/main-menu/quick-access/admin/admin-menu';
import {PromiseStub} from 'test/promise-stub'

describe('AdminMenuTest', function () {
  let trueResponse = {
    data: {isAdmin: true}
  };
  let falseResponse = {
    data: {isAdmin: false}
  };
  let userService = {
    getCurrentUser: ()=> {
      return PromiseStub.resolve(trueResponse);
    }
  };

  describe('InitAdmin', ()=> {
    it('Should initialize config correctly', ()=> {
      let admin = new AdminMenu(userService);
      let testConfig = {
        extensionPoint: 'admin-menu-items',
        triggerLabel: '<i class="fa fa-lg fa-fw fa fa-cogs"></i>',
        wrapperClass: 'admin-menu',
        buttonAsTrigger: false,
        context: {},
        title: 'menu.admin'
      };
      expect(admin.config).to.eql(testConfig);
    })
  });

  describe('RenderMenu', ()=> {
    it('Should return true or false depending on the received response', ()=> {

      let trueUserService = {
        getCurrentUser: ()=> {
          return PromiseStub.resolve(trueResponse);
        }
      };
      let trueRenderedAdmin = new AdminMenu(trueUserService);
      expect(trueRenderedAdmin.renderMenu).to.equal.true;

      let falseUserService = {
        getCurrentUser: ()=> {
          return PromiseStub.resolve(falseResponse);
        }
      };
      let falseRenderedAdmin = new AdminMenu(falseUserService);
      expect(falseRenderedAdmin.renderMenu).to.equal.false
    })
  });

});

