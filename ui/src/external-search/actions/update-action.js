import { Injectable, Inject } from 'app/app';
import { ExternalObjectService } from 'services/rest/external-object-service';
import { ReloadSearchEvent } from 'external-search/actions/reload-search-event';
import { Eventbus } from 'services/eventbus/eventbus';
import { TranslateService } from 'services/i18n/translate-service';
import { ExternalAction } from 'external-search/actions/external-action';
import { NotificationService } from 'services/notification/notification-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';

@Injectable()
@Inject(NotificationService, TranslateService ,ExternalObjectService, Eventbus, WindowAdapter)
export class UpdateIntAction extends ExternalAction {

  constructor(notificationService, translateService, externalObjectService, eventbus, windowAdapter) {
    super(notificationService, translateService, externalObjectService, eventbus);
    this.windowAdapter = windowAdapter;
  }

  resolveAction(){
    if (this.context.idocContext){
      this.windowAdapter.location.reload();
    }
  }
}