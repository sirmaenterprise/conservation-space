import {View, Component} from 'app/app';
import {RoleActionsTable} from 'administration/role-actions-table/role-actions-table';
import template from 'role-actions-table-stub-template!text';

@Component({
  selector: 'seip-role-actions-table-stub'
})
@View({
  template: template
})
export class RoleActionsTableStub {

}
