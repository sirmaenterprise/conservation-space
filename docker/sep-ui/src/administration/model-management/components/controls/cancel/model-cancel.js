import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/button/button';

import template from './model-cancel.html!text';

/**
 * Simple action button component which performs a cancel operation by executing a given component event.
 * This component provides a generic cancel button look and feel
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-cancel',
  properties: {
    'enabled': 'enabled',
    'loading': 'loading'
  },
  events: ['onCancel']
})
@View({
  template
})
export class ModelCancel extends Configurable {

  constructor() {
    super({
      label: 'administration.models.management.cancel.changes'
    });
  }

  onCancelButton() {
    this.onCancel && this.onCancel();
  }
}