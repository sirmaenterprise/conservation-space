import {View, Component} from 'app/app';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import 'administration/model-management/components/controls/select/model-select';

import './model-container-view.css!css';
import template from './model-container-view.html!text';

/**
 * A component in charge of displaying a single model as container.
 * The provided model is supplied through a component property.
 * The proved region should be of type {@link ModelBase} or any
 * types extending off of it. This component is used as a container
 * to display any desired content. Component supports transclusion
 * which can be used to inject any number of elements inside the body
 * of the region. It is a containment visualisation component.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-container-view',
  transclude: true,
  properties: {
    'model': 'model',
    'context': 'context'
  },
  events: ['onModelSelected']
})
@View({
  template
})
export class ModelContainerView {

  constructor() {
    this.expanded = true;
  }

  onContainerToggle() {
    this.expanded = !this.expanded;
  }

  selectModel(event) {
    this.onModelSelected && this.onModelSelected({model: this.model});
    // Prevent expand/collapse
    event.stopPropagation();
  }

  isParentVisible() {
    return ModelManagementUtility.isInherited(this.model, this.context);
  }
}