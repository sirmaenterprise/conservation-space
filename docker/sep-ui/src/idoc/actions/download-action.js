import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {BASE_PATH} from 'services/rest-client';
import {Logger} from 'services/logging/logger';
import {AuthenticationService} from 'security/authentication-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';


@Injectable()
@Inject(ActionsService, AuthenticationService, Logger, PromiseAdapter)
export class DownloadAction extends InstanceAction {

  constructor(actionsService, authenticationService, logger, promiseAdapter) {
    super(logger);
    this.actionsService = actionsService;
    this.authenticationService = authenticationService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition, context) {
    return this.actionsService.download(context.currentObject.getId()).then(response => {
      if (response && response.data) {
        return this.buildIframe(response);
      } else {
        return this.promiseAdapter.reject();
      }
    });
  }

  buildIframe(response) {
    return this.decorateDownloadURI(response.data).then(decoratedUri => {
      let iframe = document.createElement('iframe');
      iframe.id = 'downloadDocumentFrame';
      iframe.style.display = 'none';
      document.body.appendChild(iframe);
      iframe.src = decoratedUri;
      return response.data;
    });
  }

  decorateDownloadURI(downloadURI) {
    return this.authenticationService.getToken().then(token => {
      let decoratedURI = BASE_PATH + downloadURI;
      decoratedURI += decoratedURI.indexOf('?') !== -1 ? '&' : '?';
      decoratedURI += AuthenticationService.TOKEN_REQUEST_PARAM + '=' + token;
      return decoratedURI;
    });
  }
}
