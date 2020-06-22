import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/services/rest/role-management-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class RoleManagementService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getRoleActions() {
    return this.promiseAdapter.resolve({
      data: data
    });
  }

  getFilters() {
    return this.promiseAdapter.resolve({
      data: ['CREATEDBY', 'ASSIGNEE', 'IS_UPLOADED', 'LOCKEDBY']
    });
  }

  saveRoleActions(roleActions) {
    data.roleActions = _.union(data.roleActions, roleActions);
    return this.promiseAdapter.resolve({
      data: data
    });
  }

}