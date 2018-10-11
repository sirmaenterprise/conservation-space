import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Select} from 'components/select/select';
import {RoleManagementService} from 'services/rest/role-management-service';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';

import template from './advanced-search-action-criteria.html!text';

/**
 * Component for choosing action values in the advanced search criteria form.
 *
 * The component relies heavily on providing a criteria object and the property for which will display action values.
 *
 * The component can be configured, example configuration:
 *  config: {
 *    disabled: false
 *  }
 *
 * @author Hristo Lungov
 */
@Component({
  selector: 'seip-advanced-search-action-criteria',
  properties: {
    'config': 'config',
    'property': 'property',
    'criteria': 'criteria'
  }
})
@View({
  template: template
})
@Inject(NgScope, RoleManagementService, PromiseAdapter, TranslateService)
export class AdvancedSearchActionCriteria extends Configurable {
  constructor($scope, roleManagementService, promiseAdapter, translateService) {
    super({
      disabled: false
    });
    this.$scope = $scope;
    this.roleManagementService = roleManagementService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;

    this.config.isDisabled = () => this.config.disabled;
    this.createSelectConfig();
  }

  createSelectConfig() {
    this.loadLabelValues().then((values) => {
      this.selectConfig = {
        multiple: true,
        data: values,
        isDisabled: () => this.config.isDisabled(),
        selectOnClose: true
      };
    });
  }

  loadLabelValues() {
    return this.roleManagementService.getRoleActions().then((response) => {
      return this.convertActions(response.data);
    });
  }

  convertActions(data = []) {

    var values = data.actions.map((action) => {
      return this.convertAction(action);
    });

    values.push({
      id: 'login',
      text: this.translateService.translateInstant('label.login')
    });

    values.push({
      id: 'logout',
      text: this.translateService.translateInstant('label.logout')
    });

    values.push({
      id: 'addRelation',
      text: this.translateService.translateInstant('label.addRelation')
    });

    values.push({
      id: 'removeRelation',
      text: this.translateService.translateInstant('label.removeRelation')
    });

    values.push({
      id: 'changePassword',
      text: this.translateService.translateInstant('label.changePassword')
    });

    return _.sortBy(values, (value) => {
      return value.text;
    });
  }

  convertAction(action) {
    return {
      id: action.id,
      text: action.label
    };
  }
}