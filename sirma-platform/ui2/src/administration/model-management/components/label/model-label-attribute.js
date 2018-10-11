import {View, Component, Inject} from 'app/app';
import {ModelValuesView} from './model-values-view';
import {DialogService} from 'components/dialog/dialog-service';

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
    'attribute': 'attribute'
  }
})
@View({
  template
})
@Inject(DialogService)
export class ModelLabelAttribute {

  constructor(dialogService) {
    this.dialogService = dialogService;
  }

  openValuesDialog() {
    this.dialogService.create(ModelValuesView, this.getValuesViewComponentConfig(), this.getDialogConfiguration());
  }

  getValuesViewComponentConfig() {
    return {
      values: this.attribute.getValues()
    };
  }

  getDialogConfiguration() {
    return {
      header: this.attribute.getMetaData().getDescription().getValue(),
      largeModal: true,
      buttons: [this.dialogService.createButton(DialogService.CLOSE, 'dialog.button.close')],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        dialogConfig.dismiss();
      }
    };
  }
}