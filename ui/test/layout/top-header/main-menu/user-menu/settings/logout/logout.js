import {SettingsLogout} from 'layout/top-header/main-menu/user-menu/settings/logout/logout';

describe('SettingsLogout', () => {

  describe('logout()', () => {

    it('should call logout from authentication service', () => {
      var spy = sinon.spy();

      new SettingsLogout({logout: spy}).logout();

      expect(spy.called).to.be.true;
    });
  });
});