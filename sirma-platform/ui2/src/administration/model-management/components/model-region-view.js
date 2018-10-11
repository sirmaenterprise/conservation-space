import {View, Component} from 'app/app';
import 'administration/model-management/components/model-field-view';

import './model-region-view.css!css';
import template from './model-region-view.html!text';

/**
 * A component in charge of displaying a single model region.
 * The provided model is supplied through a component property.
 * The proved region should be of type {@link ModelRegion}
 * or any types extending off of it.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-region-view',
  properties: {
    'model': 'model'
  }
})
@View({
  template
})
export class ModelRegionView {

  isRegionVisible() {
    return this.model.getView().isVisible();
  }

  onRegionToggle() {
    let view = this.model.getView();
    view.setVisible(!view.isVisible());
  }
}