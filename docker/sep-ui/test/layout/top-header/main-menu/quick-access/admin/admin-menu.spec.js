import {AdminMenu} from 'layout/top-header/main-menu/quick-access/admin/admin-menu';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('AdminMenuTest', function () {
  let trueResponse = {
    data: {isAdmin: true}
  };
  let falseResponse = {
    data: {isAdmin: false}
  };
  let userService = {
    getCurrentUser: () => {
      return PromiseStub.resolve(trueResponse);
    }
  };

  describe('InitAdmin', () => {
    it('Should initialize config correctly', () => {
      let admin = new AdminMenu(userService, stub(TranslateService));

      let testConfig = {
        extensionPoint: 'admin-menu-items',
        triggerLabel: '<i class="fa fa-lg fa-fw fa-gear"></i>',
        wrapperClass: 'admin-menu',
        buttonAsTrigger: false,
        context: {},
        tooltip: 'menu.admin'
      };
      // extract all functions from the config
      let functions = _.functions(admin.config);
      // omit all functions attached to the config
      let sanitizedConfig = _.omit(admin.config, functions);

      expect(functions.length).to.eq(1);
      expect(admin.config.sortComparator).to.exist;
      expect(sanitizedConfig).to.deep.equal(testConfig);
    })
  });

  describe('RenderMenu', () => {
    it('Should return true or false depending on the received response', () => {

      let trueUserService = {
        getCurrentUser: () => {
          return PromiseStub.resolve(trueResponse);
        }
      };
      let trueRenderedAdmin = new AdminMenu(trueUserService);
      expect(trueRenderedAdmin.renderMenu).to.equal.true;

      let falseUserService = {
        getCurrentUser: () => {
          return PromiseStub.resolve(falseResponse);
        }
      };
      let falseRenderedAdmin = new AdminMenu(falseUserService);
      expect(falseRenderedAdmin.renderMenu).to.equal.false
    })
  });

  describe('Sort compare method', () => {
    it('Should properly compare menu elements labels', () => {
      let admin = new AdminMenu(userService, stub(TranslateService));
      admin.translateService.translateInstant = (key) => key;

      expect(admin.menuElementsComparator({label: 'b'}, {label: 'b'})).to.eq(0);
      expect(admin.menuElementsComparator({label: 'b'}, {label: 'a'})).to.eq(1);
      expect(admin.menuElementsComparator({label: 'a'}, {label: 'b'})).to.eq(-1);

      expect(admin.menuElementsComparator({label: 'T'}, {label: 'Z'})).to.eq(-1);
      expect(admin.menuElementsComparator({label: 'y'}, {label: 'F'})).to.eq(1);
      expect(admin.menuElementsComparator({label: 'L'}, {label: 'L'})).to.eq(0);
    });
  });

});

