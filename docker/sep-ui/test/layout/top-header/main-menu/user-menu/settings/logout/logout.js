import {SettingsLogout} from 'layout/top-header/main-menu/user-menu/settings/logout/logout';
import {Eventbus} from 'services/eventbus/eventbus';
import {stub} from 'test/test-utils';

describe('SettingsLogout', () => {

  describe('logout()', () => {

    it('should call logout from authentication service', () => {
      var spy = sinon.spy();
      var eventbusStub = stub(Eventbus);

      new SettingsLogout({logout: spy}, eventbusStub).logout();

      expect(eventbusStub.publish.called).to.be.true;
      expect(spy.called).to.be.true;
    });
  });
});