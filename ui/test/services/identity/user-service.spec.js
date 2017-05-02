import {UserService} from 'services/identity/user-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('UserService', () => {

  it('should fetch user info from rest', function (done) {
    let expected = {
      id: 'john@domain',
      name: 'John',
      username: 'john',
      isAdmin: true,
      language: 'en',
      tenantId: 'tenant.com'
    };

    let userService = getUserServiceInstance();

    userService.getCurrentUser().should.eventually.deep.equal(expected).notify(done);
  });

  it('should publish UserLoadedEvent', (done) => {
    let userService = getUserServiceInstance();
    let spyPublish = sinon.spy(userService.eventbus, 'publish');
    userService.getCurrentUser(true).then((user) => {
      expect(spyPublish.called).to.be.true;
      done();
    }).catch(done);
  });

  it('should instantly resolve user info if already fetched', function (done) {
    let userService = getUserServiceInstance();
    userService.currentUser = {id: 'john'};
    let authenticationServiceSpy = sinon.spy(userService.authenticationService, 'getUsername');
    let resourceServiceSpy = sinon.spy(userService.resourceRestService, 'getResource');

    userService.getCurrentUser().then((user) => {
      expect(authenticationServiceSpy.called).to.be.false;
      expect(resourceServiceSpy.called).to.be.false;
      done();
    }).catch(done);
  });

  it('should invoke resource rest service with arguments on changing password', function () {
    let userService = getUserServiceInstance();
    let authenticationServiceSpy = sinon.spy(userService.authenticationService, 'getUsername');
    let resourceServiceSpy = sinon.spy(userService.resourceRestService, 'changePassword');

    userService.changePassword('123', '321');

    expect(authenticationServiceSpy.called).to.be.true;
    expect(resourceServiceSpy.called).to.be.true;
  });

  function getUserServiceInstance() {
    let authenticationService = getAuthenticationServiceMock();
    let resourceRestService = getResourceServiceMock();
    let eventbus = IdocMocks.mockEventBus();
    return new UserService(authenticationService, resourceRestService, PromiseAdapterMock.mockAdapter(), eventbus);
  }

  function getAuthenticationServiceMock() {
    return {
      getUsername: () => {
        return 'john';
      }
    };
  }

  function getResourceServiceMock() {
    return {
      getResource: (username) => {
        return Promise.resolve({
          data: {
            value: 'john',
            label: 'John',
            id: 'john@domain',
            isAdmin: true,
            language: 'en',
            tenantId: 'tenant.com'
          }
        });
      },
      changePassword: (username, oldPass, newPass) => {
      }
    };
  }

});

