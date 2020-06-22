import {View, Component, Inject} from 'app/app';
import {ModelValuesView} from './model-values-view';
import {DialogService} from 'components/dialog/dialog-service';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';

import './model-label-attribute.css!css';
import template from './model-label-attribute.html!text';

/**
 * Component responsible for rendering multi valued attribute.
 * Attribute model is provided through a component property and
 * should be of type {@link ModelMultiAttribute}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-label-attribute',
  properties: {
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(DialogService)
export class ModelLabelAttribute extends ModelGenericAttribute {

  constructor(dialogService) {
    super();
    this.dialogService = dialogService;
  }

  openValuesDialog() {
    this.dialogService.create(ModelValuesView, this.getValuesViewComponentConfig(), this.getDialogConfiguration());
  }

  onModelValueChange() {
    return this.onChange && this.onChange();
  }

  getValuesViewComponentConfig() {
    return {
      editable: this.editable,
      attribute: this.attribute,
      onChange: this.onModelValueChange.bind(this)
    };
  }

  getDialogConfiguration() {
    return {
      largeModal: true,
      header: this.attribute.getMetaData().getDescription().getValue(),
      buttons: [this.dialogService.createButton(DialogService.CLOSE, 'dialog.button.close')],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        dialogConfig.dismiss();
      }
    };
  }
}