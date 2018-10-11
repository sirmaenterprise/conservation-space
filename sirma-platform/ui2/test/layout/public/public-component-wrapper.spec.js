import {PublicComponentWrapper} from 'layout/public/public-component-wrapper';
import {AuthenticationService} from 'services/security/authentication-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {Logger} from 'services/logging/logger';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('PublicComponentWrapper', () => {

  let publicComponentWrapper;
  let authenticationService;
  let windowAdapter;
  let stateParamsAdapter;
  let pluginsService;
  let logger;

  beforeEach(() => {
    authenticationService = mockAuthenticationService();
    windowAdapter = stub(WindowAdapter);
    stateParamsAdapter = mockStateParamsAdapter();
    pluginsService = mockPluginsService();
    logger = stub(Logger);

    publicComponentWrapper = new PublicComponentWrapper(mockCompile(), mock$scope(), mockElement(), authenticationService,
      pluginsService, windowAdapter, stateParamsAdapter, logger);
  });

  describe('ngOnInit()', () => {
    it('should retrieve component id', () => {
      publicComponentWrapper.ngOnInit();

      expect(publicComponentWrapper.componentId).to.equal('myComponent');
    });

    it('should check if user is authenticated', () => {
      publicComponentWrapper.ngOnInit();

      expect(authenticationService.isAuthenticated.called).to.be.true;
    });

    it('should load component', () => {
      publicComponentWrapper.ngOnInit();

      expect(pluginsService.loadComponentModules.called).to.be.true;
    });
  });

  describe('redirectToHomeIfAuthenticated()', () => {
    it('should navigate to home page if user is authenticated', () => {
      authenticationService.isAuthenticated.returns(true);

      publicComponentWrapper.redirectToHomeIfAuthenticated();

      expect(windowAdapter.navigate.calledWith('/')).to.be.true;
    });

    it('should navigate to home page if component id missing', () => {
      publicComponentWrapper.redirectToHomeIfAuthenticated();

      expect(windowAdapter.navigate.calledWith('/')).to.be.true;
    });

    it('should do nothing when user not authenticated and component id available', () => {
      publicComponentWrapper.componentId = 'myComponent';

      publicComponentWrapper.redirectToHomeIfAuthenticated();

      expect(windowAdapter.navigate.called).to.be.false;
    });
  });

  describe('compileComponent()', () => {
    it('should redirect to home when component not found in the registry', () => {
      publicComponentWrapper.componentId = 'notExisting';

      publicComponentWrapper.compileComponent();

      expect(windowAdapter.navigate.calledWith('/')).to.be.true;
    });

    it('should log error when component not found in the registry', () => {
      publicComponentWrapper.componentId = 'notExisting';

      publicComponentWrapper.compileComponent();

      expect(logger.error.called).to.be.true;
    });

    it('should compile component and append it to the dom when exists in the registry', () => {
      publicComponentWrapper.componentId = 'home';

      publicComponentWrapper.compileComponent();

      expect(windowAdapter.navigate.called).to.be.false;
    });
  });

  function mockAuthenticationService() {
    let authService = stub(AuthenticationService);
    authService.isAuthenticated.returns(false);
    return authService;
  }

  function mockPluginsService() {
    let pluginsService = stub(PluginsService);
    pluginsService.loadComponentModules.returns(PromiseStub.resolve({
      home: {
        component: 'home'
      }
    }));
    return pluginsService;
  }

  function mockStateParamsAdapter() {
    let adapter = stub(StateParamsAdapter);
    adapter.getStateParam.returns('myComponent');
    return adapter;
  }

  function mockElement() {
    return {
      find: () => {
        return {
          append: () => {
          }
        };
      }
    };
  }

  function mockCompile() {
    return () => {
      return () => {
      };
    };
  }

});