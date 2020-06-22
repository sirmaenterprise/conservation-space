import {Component, View} from 'app/app';

import 'administration/model-management/components/attributes/model-attribute-view';

import template from './model-action-group-view.html!text';

@Component({
  selector: 'seip-model-action-group-view',
  properties: {
    'model': 'model'
  },
  events: ['onAttributeChange']
})
@View({
  template
})
export class ModelActionGroupView {

  onActionGroupAttributeChanged(attribute) {
    return this.onAttributeChange && this.onAttributeChange({attribute});
  }

  getModelTitle() {
    return this.model && this.model.getDescription().getValue();
  }

  getModelAttributes() {
    return this.model && this.model.getAttributes();
  }
}