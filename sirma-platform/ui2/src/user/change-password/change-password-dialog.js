import {Component, View, Inject} from 'app/app';
import {FormWrapper, LAYOUT} from 'form-builder/form-wrapper';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import template from './change-password-dialog.html!text';
import './change-password-dialog.css!css';

@Component({
  selector: 'seip-change-password-dialog',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(TranslateService, Eventbus)
export class ChangePasswordDialog {

  constructor(translateService, eventbus) {
    this.translateService = translateService;

    this.config = this.config || {};
    this.formConfig = this.formConfig || {};
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;
    this.config.layout = LAYOUT.HORIZONTAL;

    this.afterFormValidationEvent = eventbus.subscribe(AfterFormValidationEvent, (data) => this.config.afterFormValidation(data));

    this.buildForm();
  }

  ngOnDestroy() {
    this.afterFormValidationEvent.unsubscribe();
  }

  getCurrentPassword() {
    return this.formConfig.models.validationModel.currentPassword;
  }

  getNewPassword() {
    return this.formConfig.models.validationModel.newPassword;
  }

  getNewPasswordConfirmation() {
    return this.formConfig.models.validationModel.confirmNewPassword;
  }

  buildForm() {
    //validation model is passed as an instance of InstanceModel through the whole application.
    //view model is pass as an instance of DefinitionModel through the whole application.
    this.formConfig.models = {
      'validationModel': new InstanceModel({
        'currentPassword': {
          'value': '',
          'messages': []
        },
        'newPassword': {
          'value': '',
          'messages': []
        },
        'confirmNewPassword': {
          'value': '',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [{
          'dataType': 'password',
          'displayType': 'EDITABLE',
          'identifier': 'currentPassword',
          'isMandatory': true,
          'previewEmpty': true,
          'label': this.translateService.translateInstant('change.password.dialog.current'),
          'validators': [{
            'id': 'mandatory',
            'level': 'error',
            'message': this.translateService.translateInstant('change.password.dialog.current.mandatory')
          }]
        },
          {
            'dataType': 'password',
            'displayType': 'EDITABLE',
            'identifier': 'newPassword',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('change.password.dialog.new'),
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': this.translateService.translateInstant('change.password.dialog.new.mandatory')
            }, {
              'id': 'regexPlain',
              'message': this.translateService.translateInstant('change.password.dialog.new.length'),
              'level': 'error',
              'context': {
                'pattern': '[\\S]{6,30}'
              }
            }, {
              'id': 'notEqualFields',
              'message': this.translateService.translateInstant('change.password.dialog.new.same'),
              'level': 'error',
              'context': {
                'value': 'currentPassword'
              }
            }]
          },
          {
            'dataType': 'password',
            'displayType': 'EDITABLE',
            'identifier': 'confirmNewPassword',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('change.password.dialog.confirm'),
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': this.translateService.translateInstant('change.password.dialog.confirm.mandatory')
            }, {
              'id': 'equalFields',
              'message': this.translateService.translateInstant('change.password.dialog.confirm.dont.match'),
              'level': 'error',
              'context': {
                'value': 'newPassword'
              }
            }]
          }]
      })
    };
  }

}
