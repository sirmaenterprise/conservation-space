import {View, Component} from 'app/app';

import './model-controls.css!css';
import template from './model-controls.html!text';

/**
 * Simplistic wrapper component for model controls. This component provides a common styling and functionality
 * to house unlimited number of model controls which can be injected using standard angular transclusion method
 * Example usage of this component:
 *
 * <model-controls>
 *    <model-save></model-save>
 *    <model-cancel></model-cancel>
 *    ...
 *    <model-refresh></model-refresh>
 * </model-controls>
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-controls',
  transclude: true
})
@View({template})
export class ModelControls {
  // Wrapping component left empty
}
