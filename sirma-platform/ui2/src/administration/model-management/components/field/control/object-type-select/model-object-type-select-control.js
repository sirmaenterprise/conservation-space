import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

/**
 * Component responsible for rendering specific view for the OBJECT_TYPE_SELECT control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author Stella D
 */
@Component({
  selector: 'model-object-type-select-control',
  properties: {
    'control': 'control'
  }
})
@View({
  template: '<div></div>'
})
export class ModelObjectTypeSelectControl extends ModelGenericControl {

}