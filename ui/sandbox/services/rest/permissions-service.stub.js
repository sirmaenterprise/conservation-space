import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class PermissionsRestService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  load(instanceId, includeCalculatedInherited) {
    return this.promiseAdapter.promise((resolve)=> {
      let permissions = {
        "permissions": [{
          "type": "system",
          "id": "sec:SYSTEM_ALL_OTHER_USERS",
          "name": "SYSTEM_ALL_OTHER_USERS",
          "label": "SYSTEM_ALL_OTHER_USERS",
          "isManager": false,
          "inherited": "CONSUMER"
        }, {
          "type": "user",
          "id": "emf:admin-niki.c1",
          "name": "admin@niki.c1",
          "label": "niki.c1 Administrator",
          "isManager": true,
          "inherited": "MANAGER"
        }],
        "editAllowed": true,
        "restoreAllowed": true,
        "isRoot": false,
        "inheritedPermissionsEnabled": true,
        "permissionModel": "INHERITED"

      };

      resolve({data: permissions});
    });
  }

  save() {
    return this.promiseAdapter.promise((resolve)=> {
      let permissions = {
        data: {
          "permissions": [{
            "type": "system",
            "id": "sec:SYSTEM_ALL_OTHER_USERS",
            "name": "SYSTEM_ALL_OTHER_USERS",
            "label": "SYSTEM_ALL_OTHER_USERS",
            "isManager": false,
            "inherited": "CONSUMER"
          }, {
            "type": "user",
            "id": "emf:admin-niki.c1",
            "name": "admin@niki.c1",
            "label": "niki.c1 Administrator",
            "isManager": true,
            "special": "CONSUMER",
            "inherited": "MANAGER"
          },
            {
              "type": "user",
              "id": "johndoe@doeandco.com",
              "name": "admin@john.doe",
              "label": "John Doe",
              "isManager": false,
              "inherited": "CONSUMER"
            }],
          "editAllowed": true, "restoreAllowed": true, "isRoot": false, "inheritedPermissionsEnabled": true,
          "permissionModel": "SPECIAL_INHERITED"
        }
      };
      resolve(permissions);
    });
  }


  getRoles() {
    return this.promiseAdapter.promise((resolve)=> {
      let roles = {
        data: [
          {
            label: 'No Permissions',
            value: 'NO_PERMISSIONS'
          },
          {
            label: 'Consumer',
            value: 'CONSUMER'
          }
          ,
          {
            label: 'Contributor',
            value: 'CONTRIBUTOR'
          }
          ,
          {
            label: 'Manager',
            value: 'MANAGER'
          }
        ]
      };
      resolve(roles);
    });
  }

  restoreChildrenPermissions(id) {
    $(document.body).append(`<div>Parent permissions restored for ${id}</div>`);
    return this.promiseAdapter.resolve();
  }

}