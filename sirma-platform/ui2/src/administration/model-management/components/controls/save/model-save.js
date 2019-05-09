import {View, Component, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Configurable} from 'components/configurable';
import 'components/button/button';

import template from './model-save.html!text';

/**
 * Simple action button component which notifies a user when a save operation has completed.
 * A notification is displayed which signals the user for the state of the saving action performed.
 * This component provides a generic save button look and feel.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-save',
  properties: {
    'enabled': 'enabled',
    'loading': 'loading'
  },
  events: ['onSave']
})
@View({
  template
})
@Inject(NotificationService, TranslateService)
export class ModelSave extends Configurable {

  constructor(notificationService, translateService) {
    super({
      primary: true,
      label: 'administration.models.management.save.changes'
    });
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.message = this.translateService.translateInstant('administration.models.management.save.success.message');
  }

  onSaveButton() {
    return this.onSave && this.onSave().then(() => this.notificationService.success(this.message));
  }
}