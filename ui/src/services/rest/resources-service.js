import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

const serviceUrl = '/users';

@Injectable()
@Inject(RestClient, TranslateService, PromiseAdapter)
export class ResourceRestService {

  constructor(restClient, translateService, promiseAdapter) {
    this.restClient = restClient;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
  }

  getResources(opts) {
    let config = {
      params: opts
    };
    return this.restClient.get(serviceUrl, config);
  }

  getResource(id) {
    if (!id) {
      return new Promise((resolve, reject) => {
        reject('Cannot load resource without an ID.');
      });
    }
    return this.restClient.get(`${serviceUrl}/${id}`);
  }

  changePassword(username, oldPassword, newPassword) {
    if (!username || !oldPassword || !newPassword) {
      let message = this.translateService.translateInstant('change.password.dialog.missing.args');
      return this.promiseAdapter.reject(message);
    }
    let payload = {
      username: username,
      oldPassword: oldPassword,
      newPassword: newPassword
    };
    return this.restClient.post('/user/change-password', payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      },
      // TODO: remove skipping when messages in notifications are added
      'skipInterceptor': true
    });
  }

}
