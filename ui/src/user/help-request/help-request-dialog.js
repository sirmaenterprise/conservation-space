import {Component, View, Inject, NgTimeout} from 'app/app';
import {Configuration} from 'common/application-config';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {FormWrapper, LAYOUT} from 'form-builder/form-wrapper';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import 'common/lib/ckeditor/ckeditor';
import './help-request-dialog.css!css';

import template from './help-request-dialog.html!text';

const EDITOR_TOOLBAR = 'help-request-editor-toolbar';
const EDITOR_SELECTOR = 'help-request-editor';
const DEFAULT_CODELIST = 15;

@Component({
  selector: 'seip-help-request-dialog',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(TranslateService, Eventbus, Configuration, LocationAdapter, NgTimeout)
export class HelpDialog {

  constructor(translateService, eventbus, configuration, locationAdapter, $timeout) {
    this.config = this.config || {};
    this.formConfig = this.formConfig || {};
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;
    this.config.layout = LAYOUT.VERTICAL;
    this.$timeout = $timeout;
    let configurationProperty = configuration.get(Configuration.HELP_SUPPORT_CODELIST_MAIL_TYPE);
    this.helpSupportCodelits = configurationProperty ? configurationProperty : DEFAULT_CODELIST;

    this.locationAdapter = locationAdapter;
    this.translateService = translateService;
    this.eventbus = eventbus;

    this.afterFormValidationEvent = eventbus.subscribe(AfterFormValidationEvent, (data) => this.config.afterFormValidation(data));

    //Link to current page. this link will attach to mail context(description) entered by user.
    this.currentURL = translateService.translateInstant('help.request.current.url.description') + " <a href=\"" + this.locationAdapter.url() + "\">" + translateService.translateInstant('help.request.current.url') + "</a>";
    // This string will be used for check if description is empty. See method isDescriptionEmpty().
    //After initialization of ckeditor we will add current page link. But ckeditor surround it with paragraph. And
    //for verification of description we have to bear in mind.
    this.ckeditorCurrenURL = "<p>" + this.currentURL + "</p>";

    this.buildForm();
    this.formConfig.models.definitionId = 'help-request';
    this.config.selectedProperties = {[this.formConfig.definitionId]: ['type', 'subject']};
  }

  /**
   * Attach listener on change event to ckeditor. When event
   * occurred we read content from ckeditor and set it to hidden input with id "message" (see model).
   * This will trigger validation process (this is needed because ckeditor is not part of form builder and
   * we have manually care about visualisation of editor).
   */
  ngAfterViewInit() {
    this.editor = CKEDITOR.replace(EDITOR_SELECTOR, {
        toolbar: PluginRegistry.get(EDITOR_TOOLBAR)[0].data,
        extraPlugins: 'colorbutton'
      }
    );
    this.editor.on('change', () => {
      var description = this.getDescription();
      var inputMessage = $("input[id='message']");
      //We remove auto added link to current page to prevent wrong validation of
      //description. Description is mandatory but link to current page is auto added.
      inputMessage.val(description.replace(this.ckeditorCurrenURL, ""));
      inputMessage.trigger('change');
      this.updateCkeditorStyle();
    });
    this.$timeout(() => {
      this.updateCkeditorStyle();
      this.setDescription(this.currentURL);
    });
  }

  /**
   * Check if content of ckeditor is empty and update
   * look of component (this is needed because ckeditor is not part of form builder and
   * we have manually care about visualisation of editor).
   */
  updateCkeditorStyle() {
    if (this.isDescriptionEmpty()) {
      $('#cke_help-request-editor').addClass("description-error");
      $('#ckeditor-mandatory-message').show();
    } else {
      $('#cke_help-request-editor').removeClass("description-error");
      $('#ckeditor-mandatory-message').hide();
    }
  }

  /**
   * @returns true if ckeditor content is empty or contains current page link only.
   */
  isDescriptionEmpty() {
    var content = this.getDescription();
    return !(content && content.length > 0 && content !== this.ckeditorCurrenURL);
  }

  /**
   * Prepare json data whick will be send to server.
   * @returns the json data.
   */
  prepareRequestModel() {
    return {
      subject: this.getSubject(),
      type: this.getType(),
      description: this.getDescription()
    };
  }

  /**
   * @returns value of subject.
   */
  getSubject() {
    return this.formConfig.models.validationModel.subject.value;
  }

  /**
   * @returns value of type.
   */
  getType() {
    return this.formConfig.models.validationModel.type.value;
  }

  /**
   * @returns value of ckeditor content.
   */
  getDescription() {
    return this.editor.getData();
  }

  /**
   * Add text to ckeditor content.
   * @param data which will be added.
   */
  setDescription(data) {
    this.editor.setData(data);
  }

  /**
   * Builds dialog models. There is a hidden field with identifier "message".
   * It will be used to triger validation of ckeditor content (this is needed because ckeditor is not part
   * of form builder and we have manually care about visualisation of editor).
   */
  buildForm() {
    this.formConfig.models = {
      'validationModel': new InstanceModel({
        'subject': {
          'value': '',
          'messages': []
        },
        'type': {
          'value': '',
          'messages': []
        },
        'message': {
          'value': '',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [{
          'identifier': 'subject',
          'dataType': 'text',
          'displayType': 'EDITABLE',
          'isMandatory': true,
          'disabled': false,
          'previewEmpty': true,
          'label': this.translateService.translateInstant('help.request.dialog.subject.label'),
          'validators': [{
            'id': 'mandatory',
            'level': 'error',
            'message': this.translateService.translateInstant('validation.field.mandatory')
          }]
        },
          {
            'identifier': 'type',
            'dataType': 'text',
            'displayType': 'EDITABLE',
            'previewEmpty': true,
            'disabled': false,
            'isMandatory': true,
            'codelist': this.helpSupportCodelits,
            'label': this.translateService.translateInstant('help.request.dialog.type.label'),
            'validators': [
              {
                id: 'mandatory',
                'message': this.translateService.translateInstant('validation.field.mandatory'),
                level: 'error'
              }
            ]
          },
          {
            'identifier': 'message',
            'dataType': 'text',
            'displayType': 'HIDDEN',
            'isMandatory': true,
            'disabled': false,
            'previewEmpty': true,
            'label': this.translateService.translateInstant('help.request.dialog.subject.label'),
            'validators': [{
              id: 'mandatory',
              'message': this.translateService.translateInstant('validation.field.mandatory'),
              level: 'error'
            }]
          }
        ]
      })
    };
  }

  /**
   * Unsubscribe form validation event and destroying ckeditor.
   */
  ngOnDestroy() {
    this.afterFormValidationEvent.unsubscribe();
    this.editor.destroy(true);
  }
}