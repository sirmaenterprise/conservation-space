import {Component, View, Inject} from 'app/app';
import {FormWrapper, LAYOUT} from 'form-builder/form-wrapper';
import {ResourceRestService} from 'services/rest/resources-service';
import {TranslateService} from 'services/i18n/translate-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {NotificationService} from 'services/notification/notification-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {UrlUtils} from 'common/url-utils';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';

import './account-confirmation.css!css';
import template from './account-confirmation.html!text';

@Component({
  selector: 'seip-account-confirmation'
})
@View({
  template
})
@Inject(ResourceRestService, AuthenticationService, TranslateService, Eventbus, WindowAdapter, NotificationService)
export class AccountConfirmation {

  constructor(resourceService, authenticationService, translateService, eventbus, windowAdapter, notificationService) {
    this.resourceService = resourceService;
    this.authenticationService = authenticationService;
    this.translateService = translateService;
    this.eventbus = eventbus;
    this.windowAdapter = windowAdapter;
    this.notificationService = notificationService;
  }

  ngOnInit() {
    this.code = UrlUtils.getParameter(this.windowAdapter.location.href, 'code');
    this.username = UrlUtils.getParameter(this.windowAdapter.location.href, 'username');
    this.tenant = UrlUtils.getParameter(this.windowAdapter.location.href, 'tenant');

    this.redirectToHomeIfParamsMissing();

    this.afterFormValidationSubscription = this.eventbus.subscribe(AfterFormValidationEvent, (data) => this.afterFormValidation(data));
    this.finishButtonDisabled = true;

    this.getCaptcha();
    this.buildFormConfig();
  }

  redirectToHomeIfParamsMissing() {
    if (!this.code || !this.username || !this.tenant) {
      this.windowAdapter.navigate('/');
    }
  }

  afterFormValidation(data) {
    this.finishButtonDisabled = !data[0].isValid;
  }

  getCaptcha() {
    this.resourceService.getCaptcha(this.code, this.tenant).then((response) => {
      this.captchaLink = response.data.captchaLink;
    });
  }

  buildFormConfig() {
    this.config = {
      formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT,
      layout: LAYOUT.HORIZONTAL
    };

    this.formConfig = {
      models: {
        validationModel: new InstanceModel({
          'username': {
            'value': this.username,
            'messages': []
          },
          'newPassword': {
            'value': '',
            'messages': []
          },
          'confirmPassword': {
            'value': '',
            'messages': []
          },
          'captcha': {
            'value': '',
            'messages': []
          }
        }),
        viewModel: new DefinitionModel({
          fields: [{
            'dataType': 'text',
            'displayType': 'READ_ONLY',
            'identifier': 'username',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('account.confirmation.username'),
            'validators': []
          }, {
            'dataType': 'password',
            'displayType': 'EDITABLE',
            'identifier': 'newPassword',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('account.confirmation.password'),
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': this.translateService.translateInstant('validation.field.mandatory')
            }, {
              'id': 'regexPlain',
              'message': this.translateService.translateInstant('change.password.dialog.new.length'),
              'level': 'error',
              'context': {
                'pattern': '[\\S]{6,30}'
              }
            }]
          }, {
            'dataType': 'password',
            'displayType': 'EDITABLE',
            'identifier': 'confirmPassword',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('account.confirmation.password.confirm'),
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': this.translateService.translateInstant('validation.field.mandatory')
            }, {
              'id': 'equalFields',
              'message': this.translateService.translateInstant('account.confirmation.password.confirm.match'),
              'level': 'error',
              'context': {
                'value': 'newPassword'
              }
            }]
          }, {
            'dataType': 'text',
            'displayType': 'EDITABLE',
            'identifier': 'captcha',
            'isMandatory': true,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('account.confirmation.captcha'),
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': this.translateService.translateInstant('validation.field.mandatory')
            }]
          }]
        })
      }
    };
  }

  finish() {
    let password = this.formConfig.models.validationModel.newPassword.value;
    let captcha = this.formConfig.models.validationModel.captcha.value;

    this.resourceService.confirmAccount(this.username, password, this.code, captcha, this.tenant).then(() => {
      // redirect to login page after successful confirmation
      this.authenticationService.authenticate();
    }).catch((error) => {
      this.notificationService.error(error.data.message);

      if (error.data.expired) {
        // if link expired/invalid then hide form and show message to the user
        this.expiredMessage = error.data.message;
      } else {
        this.clearFieldsValues();
        // confirmation failed, retrieve new captcha image
        this.getCaptcha();
      }
    });
  }

  clearFieldsValues() {
    let validationModel = this.formConfig.models.validationModel;
    validationModel.newPassword.value = '';
    validationModel.confirmPassword.value = '';
    validationModel.captcha.value = '';
  }

  ngOnDestroy() {
    this.afterFormValidationSubscription.unsubscribe();
  }

}