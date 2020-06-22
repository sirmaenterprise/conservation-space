import {View, Component, Inject, NgScope} from 'app/app';
import {TenantConfiguration} from 'administration/tenant/tenant-configuration';
import template from 'tenant-configuration-stub-template!text';

@Component({
  selector: 'seip-tenant-configuration-stub'
})
@View({
  template: template
})
@Inject(NgScope)
export class TenantConfigurationStub {

  constructor($scope) {
    this.$scope = $scope;
  }
}
