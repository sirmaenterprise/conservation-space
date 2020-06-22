import {Injectable, Inject} from 'app/app';
import {USER} from 'services/rest/resources-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StatusCodes} from 'services/rest/status-codes';
import _ from 'lodash';
import config from 'sandbox/services/rest/services.config.json!';
import data from 'sandbox/services/rest/resources-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class ResourceRestService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.config = config['resources'];
  }

  getResources() {
    return this.promiseAdapter.promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: {
            items: [
              {id: 'johndoe@doeandco.com', label: 'John Doe', type: 'user', value: 'johndoe'},
              {id: 'janedoe@doeandco.com', label: 'Jane Doe', type: 'user', value: 'janedoe'},
              {id: 'fakedoe@doeandco.com', type: 'user', value: 'fakedoe'}
            ]
          }
        });
      }, this.config.timeout);
    });
  }

  getResource() {
    return this.promiseAdapter.promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: {
            id: 'janedoe@doeandco.com', label: 'Jane Doe', type: 'user', value: 'janedoe'
          }
        });
      }, this.config.timeout);
    });
  }

  changePassword(username, oldPassword, newPassword) {
    return this.promiseAdapter.promise((resolve, reject) => {
      setTimeout(() => {
        if (oldPassword === '123456' && oldPassword !== newPassword) {
          resolve({
            data: {
              status: StatusCodes.SUCCESS
            }
          });
        } else {
          reject({
            data: {
              status: StatusCodes.BAD_REQUEST
            }
          });
        }
      }, this.config.timeout);
    });
  }

  static getPagedResponse(data, pageNumber, pageSize) {
    let offset = (pageNumber - 1) * pageSize;
    let limit = offset + pageSize;
    data.values = data.values.slice(offset, limit);
    return data;
  }

  getAllUsers(pageNumber, pageSize) {
    let users = ResourceRestService.getPagedResponse(_.clone(data.users), pageNumber, pageSize);
    return this.promiseAdapter.resolve({data: users});
  }

  getAllGroups(pageNumber, pageSize) {
    let groups = ResourceRestService.getPagedResponse(_.clone(data.groups), pageNumber, pageSize);
    return this.promiseAdapter.resolve({data: groups});
  }

  getAllResources(resourceType) {
    if (resourceType === USER) {
      return this.getAllUsers();
    }
    return this.getAllGroups();
  }

  getCaptcha() {
    return this.promiseAdapter.resolve({
      data: {}
    });
  }

  confirmAccount(username, password, code, captchaAnswer, tenant) {
    return this.promiseAdapter.promise((resolve, reject) => {
      if (username === 'john' && password === '123456' && code === '123' && captchaAnswer === '95mm8' && tenant === 't.com') {
        resolve();
      } else if (code === 'expired') {
        reject({
          data: {
            message: 'Link expired',
            expired: true
          }
        });
      }
    });
  }

}