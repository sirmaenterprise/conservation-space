import {View, Component} from 'app/app';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

/**
 * Component responsible for rendering specific view for the BYTE_FORMAT control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 *
 * @author Stella D
 */
@Component({
  selector: 'model-byte-format-control',
  properties: {
    'control': 'control'
  }
})
@View({
  template: '<div></div>'
})
export class ModelByteFormatControl extends ModelGenericControl {

}