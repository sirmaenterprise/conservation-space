import {Inject} from 'app/app';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ValidationService} from 'form-builder/validation/validation-service';
import {Configuration} from 'common/application-config';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_TEXT_LEFT} from 'form-builder/form-wrapper';

@Inject(ValidationService)
export class FormWidgetStub {

  constructor(validationService) {
    this.formConfig = {};
    this.config = {};
    this.config.labelPosition = LABEL_POSITION_LEFT;
    this.config.showFieldPlaceholderCondition = LABEL_POSITION_HIDE;
    this.config.labelTextAlign = LABEL_TEXT_LEFT;
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;
    this.validationService = validationService;
  }

  togglePreview() {
    this.config.formViewMode = this.config.formViewMode === FormWrapper.FORM_VIEW_MODE_EDIT ? FormWrapper.FORM_VIEW_MODE_PREVIEW : FormWrapper.FORM_VIEW_MODE_EDIT;
  }

  toggleLabelPosition(position) {
    this.config.labelPosition = position;
  }

  toggleLabelTextPosition(position) {
    this.config.labelTextAlign = position;
  }
}
