import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';

const serviceUrl = '/instances';

@Injectable()
@Inject(RestClient, PromiseAdapter, TranslateService)
export class EmailAccountDelegationService {

  constructor(restClient, promiseAdapter, translateService) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
  }

  /**
   * Delegate rights to current user (target) to send emails on behalf of given business object (granteeId).
   *
   * @param target
   *        the user email account which will have his delegation rights updated
   * @param granteeId
   *        object Id, which the user will have delegated rights on.
   * @returns true if operation is successful,false otherwise.
   */
  delegateRights(target, granteeId) {
    if (!target || !granteeId) {
      let message = this.translateService.translateInstant('email.delegation.error');
      message += `target=${target}, granteeId=${granteeId}`;
      return this.promiseAdapter.reject(message);
    }
    this.config.params = {
      'target': target
    };
    return this.restClient.patch(`${serviceUrl}/${granteeId}/email-account-delegate-permission`, {}, this.config);
  }

  /**
   * Remove rights to current user (target) to send emails on behalf of given business object (granteeId).
   *
   * @param target
   *        the user email account which will have his delegation rights updated
   * @param granteeId
   *        object Id, which the user will have delegated rights on.
   */
  removeRights(target, granteeId) {
    if (!target || !granteeId) {
      let message = this.translateService.translateInstant('email.delegation.error');
      message += `target=${target}, granteeId=${granteeId}`;
      return this.promiseAdapter.reject(message);
    }
    this.config.params = {
      'target': target
    };
    return this.restClient.patch(`${serviceUrl}/${granteeId}/email-account-remove-permission`, {}, this.config);
  }

  /**
   * Get email address and display name for external account. If no external account is set default values are returned
   *
   * @param emailAccount
   *        the email account
   * @returns email address and display name
   */
  getEmailAccountAttributes(emailAccount) {
    return this.restClient.get(`${serviceUrl}/${emailAccount}/email-account-attributes`, {}, this.config);
  }

}
