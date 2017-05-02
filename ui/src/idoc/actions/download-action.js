import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {BASE_PATH} from 'services/rest-client';
import {Logger} from 'services/logging/logger';
import {AuthenticationService} from 'services/security/authentication-service';

@Injectable()
@Inject(ActionsService, AuthenticationService, Logger)
export class DownloadAction extends InstanceAction {

  constructor(actionsService, authenticationService, logger) {
    super(logger);
    this.actionsService = actionsService;
    this.authenticationService = authenticationService;
  }

  execute(actionDefinition, context) {
    return this.actionsService.download(context.currentObject.getId()).then((response) => {
      if(response && response.data) {
        let iframe = document.createElement('iframe');
        iframe.id = 'downloadDocumentFrame';
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
        iframe.src = this.decorateDownloadURI(response.data);
      }
    });
  }

  decorateDownloadURI(downloadURI) {
    let decoratedURI = BASE_PATH + downloadURI;
    decoratedURI += decoratedURI.indexOf('?') !== -1 ? '&' : '?';
    decoratedURI += AuthenticationService.TOKEN_REQUEST_PARAM + '=' + this.authenticationService.getToken();
    return decoratedURI;
  }
}
