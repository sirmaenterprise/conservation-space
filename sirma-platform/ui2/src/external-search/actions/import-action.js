import { Injectable, Inject } from 'app/app';
import { ExternalObjectService } from 'services/rest/external-object-service';
import { NotificationService } from 'services/notification/notification-service';
import { Eventbus } from 'services/eventbus/eventbus';
import { TranslateService } from 'services/i18n/translate-service';
import { ExternalAction } from 'external-search/actions/external-action';

@Injectable()
@Inject(NotificationService, TranslateService ,ExternalObjectService, Eventbus)
export class ImportAction extends ExternalAction {

  constructor(notificationService, translateService, externalObjectService, eventbus) {
    super(notificationService, translateService, externalObjectService, eventbus);
  }

}