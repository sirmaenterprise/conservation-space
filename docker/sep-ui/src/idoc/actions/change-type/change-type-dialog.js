import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';

import 'components/contextselector/context-selector';
import 'create/instance-create-configuration';
import 'idoc/template/idoc-template-selector';

import './change-type-dialog.css!';
import template from './change-type-dialog.html!text';

/**
 * A dialog which allows a user to change the type of the selected object as well as changing its context, template and
 * populating/changing properties during the process.
 *
 * @author svelikov
 */
@Component({
  selector: 'change-type-dialog',
  properties: {
    'config': 'config'
  }
})
@View({
  template
})
export class ChangeTypeDialog extends Configurable {

  constructor() {
    super({});

    this.config.contextSelectorDisabled = this.config.formConfig.models.contextSelectorDisabled || false;
    this.config.parentId = this.config.formConfig.models.parentId;
  }

  onFormLoaded(event) {
    this.config.onFormLoaded(event);
  }
}
