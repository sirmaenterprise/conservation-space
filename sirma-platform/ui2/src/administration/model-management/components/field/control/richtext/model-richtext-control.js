import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

/**
 * Component responsible for rendering specific view for the RICHTEXT control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-richtext-control',
  properties: {
    'control': 'control'
  }
})
@View({
  template: '<div></div>'
})
export class ModelRichtextControl extends ModelGenericControl {

}