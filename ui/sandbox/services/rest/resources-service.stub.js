import { Injectable, Inject } from 'app/app';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import {StatusCodes} from 'services/rest/status-codes';
import config from 'sandbox/services/rest/services.config.json!';

@Injectable()
@Inject(PromiseAdapter)
export class ResourceRestService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.config = config['resources'];
  }

  getResources(opts) {
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
      }, this.config.timeout)
    });
  }

  getResource(id) {
    return this.promiseAdapter.promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: {
            id: 'janedoe@doeandco.com', label: 'Jane Doe', type: 'user', value: 'janedoe'
          }
        });
      }, this.config.timeout)
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

}