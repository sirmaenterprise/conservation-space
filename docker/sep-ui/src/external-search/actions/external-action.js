import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {ReloadSearchEvent} from 'external-search/actions/reload-search-event';
import {StatusCodes} from 'services/rest/status-codes';

@Injectable()
@Inject()
export class ExternalAction extends ActionHandler {

  constructor(notificationService, translateService, externalObjectService, eventbus) {
    super();
    this.notificationService = notificationService;
    this.externalObjectService = externalObjectService;
    this.translateService = translateService;
    this.eventbus = eventbus;
  }

  execute(actionDefinition, context) {
    this.context = context;
    let instance;
    if (this.context.currentObject.models.instance) {
      // This is where the instance data is located that is neaded for the rest in the external system.
      instance = this.context.currentObject.models.instance.data;
    } else {
      instance = this.context.currentObject;
    }
    return this.externalObjectService.importObjects([instance]).then((response) => {
      this.executeRefresh(response);
      return response;
    }).catch((err) => {
      this.executeRefresh(err);
    });
  }

  executeRefresh(response) {
    let ms = {};
    ms.opts = {
      closeButton: true
    };

    if (response.status === StatusCodes.FORBIDDEN) {
      // This is a special case because we may have a situation where the loaded
      // instance might try to access a 3-rd party system.
      ms.message = this.translateService.translateInstant('external.operation.warning');
      this.notificationService.warning(ms);
    } else if (response.data.cause) {
      ms.message = response.data.cause.message.replace('\r\n', '<br>');
      this.notificationService.error(ms);
    } else {
      ms.message = this.translateService.translateInstant('external.operation.success');
      this.notificationService.success(ms);
    }
    this.eventbus.publish(new ReloadSearchEvent({}));
    this.resolveAction();
  }

  resolveAction() {
    //Interface method so that the action handler may apply after the action is resolved.
  }

}