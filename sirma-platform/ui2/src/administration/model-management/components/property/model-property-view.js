import {View, Component} from 'app/app';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import 'administration/model-management/components/controls/select/model-select';
import 'components/hint/hint';

import './model-property-view.css!css';
import template from './model-property-view.html!text';

const DESCRIPTION_ATTRIBUTE = ModelAttribute.DESCRIPTION_ATTRIBUTE;

/**
 * A component in charge of displaying a single model property.
 * The provided model is supplied through a component property.
 * The proved property should be of type {@link ModelProperty}
 * or any types extending off of it.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-property-view',
  properties: {
    'model': 'model',
    'context': 'context'
  },
  events: ['onModelSelected']
})
@View({
  template
})
export class ModelPropertyView {

  getTooltip() {
    let attr = this.model && this.model.getAttribute(DESCRIPTION_ATTRIBUTE);
    return attr && attr.getValue().getValue();
  }

  selectModel() {
    this.onModelSelected && this.onModelSelected({model: this.model});
  }

  isParentVisible() {
    return ModelManagementUtility.isInherited(this.model, this.context);
  }
}