import {UserDashboard} from 'user/dashboard/user-dashboard';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';

describe('UserDashboard', () => {
  var userService = {
    getCurrentUser: () => {
      return Promise.resolve({id: 'john'});
    }
  };

  it('should navigate to user landing page', (done) => {
    let route = {
      navigate: function (state, params, options) {
      }
    };
    let stateParamsAdapterMock = StateParamsAdapterMock.mockAdapter();
    let currentUserSpy = sinon.spy(userService, 'getCurrentUser');

    let userDashboard = new UserDashboard(route, stateParamsAdapterMock, userService);

    expect(currentUserSpy.calledOnce).to.be.true;
    userService.getCurrentUser().then(() => {
      expect(stateParamsAdapterMock.getStateParams()).to.deep.equal({id: 'john'});
      done();
    }).catch(done);
  });

});