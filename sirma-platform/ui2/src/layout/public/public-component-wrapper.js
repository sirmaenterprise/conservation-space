import {Component, View, Inject, NgCompile, NgElement, NgScope} from 'app/app';
import {AuthenticationService} from 'services/security/authentication-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {PluginsService} from 'services/plugin/plugins-service';
import {Logger} from 'services/logging/logger';
import Paths from 'common/paths';

import './public-component-wrapper.css!css';
import template from './public-component-wrapper.html!text';

const COMPONENT_ID_STATE_PARAM = 'componentId';
const COMPONENT_WRAPPER_CLASS = '.component-wrapper';

/**
 * Wrapper component for other components that can be accessed without authenticated user
 *
 * All the public components are registered in the following extension point: public-components
 *
 * Depending on the componentId state param, given component is loaded, if its registered in the extension point,
 * otherwise the user is redirected to login page
 */
@Component({
  selector: 'seip-public-component-wrapper'
})
@View({
  template
})
@Inject(NgCompile, NgScope, NgElement, AuthenticationService, PluginsService, WindowAdapter, StateParamsAdapter, Logger)
export class PublicComponentWrapper {

  constructor($compile, $scope, $element, authenticationService, pluginsService, windowAdapter, stateParamsAdapter, logger) { // NOSONAR
    this.$compile = $compile;
    this.$scope = $scope;
    this.$element = $element;
    this.authenticationService = authenticationService;
    this.pluginsService = pluginsService;
    this.windowAdapter = windowAdapter;
    this.stateParamsAdapter = stateParamsAdapter;
    this.logger = logger;
  }

  ngOnInit() {
    this.componentId = this.stateParamsAdapter.getStateParam(COMPONENT_ID_STATE_PARAM);

    this.redirectToHomeIfAuthenticated();

    this.compileComponent();

    this.mainLogo = Paths.getBaseScriptPath() + 'images/sep-logo.png';
    this.poweredByLogo = Paths.getBaseScriptPath() + 'images/powered-by-logo.jpg';
  }

  redirectToHomeIfAuthenticated() {
    if (this.authenticationService.isAuthenticated() || !this.componentId) {
      this.windowAdapter.navigate('/');
    }
  }

  compileComponent() {
    this.pluginsService.loadComponentModules('public-components', 'name').then((modules) => {
      let module = modules[this.componentId];
      if (!module) {
        this.logger.error('Tried to load component which is not registered as public or does not exist: ' + this.componentId);
        this.windowAdapter.navigate('/');
        return;
      }

      let _html = `<${module.component}></${module.component}>`;
      let compiled = this.$compile(_html)(this.$scope.$new());
      this.$element.find(COMPONENT_WRAPPER_CLASS).append(compiled);
    });
  }

}