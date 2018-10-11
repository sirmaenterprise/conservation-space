import {DisabledUser} from 'user/disabled-user/disabled-user';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {AuthenticationService} from 'services/security/authentication-service';
import {stub} from 'test/test-utils';

describe('DisabledUser', () => {

  let disabledUser;
  let windowAdapter;
  let authenticationService;

  beforeEach(() => {
    windowAdapter = mockWindowAdapter();
    authenticationService = stub(AuthenticationService);

    disabledUser = new DisabledUser(windowAdapter, authenticationService);
  });

  describe('ngOnInit', () => {
    it('should extract user id from url', () => {
      disabledUser.ngOnInit();

      expect(disabledUser.userId).to.equal('regularuser@tenant.com');
    });
  });

  describe('redirectToLogin', () => {
    it('should invoke authentication service', () => {
      disabledUser.redirectToLogin();

      expect(authenticationService.authenticate.called).to.be.true;
    });
  });

  function mockWindowAdapter() {
    return new WindowAdapter({
      location: {
        href: '#/public/disabledUser?id=regularuser@tenant.com'
      }
    });
  }

});