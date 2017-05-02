import {Component, View, Inject} from 'app/app';
import saveIdocDialogTemplate from './save-idoc-dialog.html!text';
import {FormWrapper} from 'form-builder/form-wrapper';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {Eventbus} from 'services/eventbus/eventbus';

@Component({
  selector: 'seip-save-idoc-dialog',
  properties: {
    'config': 'config'
  }
})
@View({
  template: saveIdocDialogTemplate
})
@Inject(Eventbus)
export class SaveIdocDialog {

  constructor(eventbus) {
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;
    // handle validation events fired by the form validation service
    this.afterFormValidationHandler = eventbus.subscribe(AfterFormValidationEvent, (data) => {
      this.config.onFormValidated(this.config.okButton, data);
    });
  }

  ngOnDestroy() {
    this.afterFormValidationHandler.unsubscribe();
  }
}