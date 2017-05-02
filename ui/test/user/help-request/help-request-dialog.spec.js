import {HelpDialog} from 'user/help-request/help-request-dialog';
import {FormWrapper, LAYOUT} from 'form-builder/form-wrapper';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';

const URL = 'url of bage';
const CURRENT_URL = 'help.request.current.url.description <a href=\"url of bage\">help.request.current.url</a>';
const CKEDITOR_CURRENT_URL = "<p>" + CURRENT_URL + "</p>"

describe('HelpDialog', () => {

  describe('constructor()', () => {
    it('should create dialog with properly configuration', () => {
      let helpDialog = constructHelpRequestDialog();

      expect(helpDialog.eventbus.subscribe.calledOnce).to.be.true;
      expect(helpDialog.config.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_EDIT);
      expect(helpDialog.config.layout).to.equal(LAYOUT.VERTICAL);
      expect(helpDialog.helpSupportCodelits).to.equal(15);
      expect(helpDialog.currentURL).to.equal(CURRENT_URL);
      expect(helpDialog.ckeditorCurrenURL).to.equal(CKEDITOR_CURRENT_URL);
      expect(helpDialog.formConfig).to.deep.equal(buildForm());

    });
  });

  describe('prepareRequestModel()', () => {
    it('should return json data with filled value in form fields', () => {
      let subject = "subject entered by user";
      let type = "RH02";
      let description = "text entered by user";
      let helpDialog = constructHelpRequestDialog();
      helpDialog.getSubject = () => {
        return subject
      };
      helpDialog.getType = () => {
        return type
      };
      helpDialog.getDescription = () => {
        return description
      };

      let requestModel = helpDialog.prepareRequestModel();

      expect(requestModel.subject).to.equal(subject);
      expect(requestModel.type).to.equal(type);
      expect(requestModel.description).to.equal(description);
    });
  });

  describe('isDescriptionEmpty()', () => {

    it('should return false when content is not empty and is different than auto generated text', () => {
      let helpDialog = constructHelpRequestDialog();
      helpDialog.getDescription = () => {
        return "text entered by user"
      };

      expect(helpDialog.isDescriptionEmpty()).to.be.false;
    });

    it('should be true when content is exactly as auto generated text', () => {
      let helpDialog = constructHelpRequestDialog();
      helpDialog.getDescription = () => {
        return CKEDITOR_CURRENT_URL
      };

      expect(helpDialog.isDescriptionEmpty()).to.be.true;
    });

    it('should be true when content is undefined', () => {
      let helpDialog = constructHelpRequestDialog();
      helpDialog.getDescription = () => {
        return undefined
      };

      expect(helpDialog.isDescriptionEmpty()).to.be.true;
    });

    it('should be true when content is empty', () => {
      let helpDialog = constructHelpRequestDialog();
      helpDialog.getDescription = () => {
        return ""
      };

      expect(helpDialog.isDescriptionEmpty()).to.be.true;
    });
  });

  describe('ngOnDestroy', () => {
    it('should destroy dialog and unsubscribe event', () => {
      let helpDialog = constructHelpRequestDialog();
      let afterFormValidationEvent = {
        unsubscribe: sinon.spy()
      }
      helpDialog.afterFormValidationEvent = afterFormValidationEvent;
      let editor = {
        destroy: sinon.spy()
      }
      helpDialog.editor = editor;

      helpDialog.ngOnDestroy();

      expect(helpDialog.afterFormValidationEvent.unsubscribe.calledOnce).to.be.true;
      expect(helpDialog.editor.destroy.calledOnce).to.be.true;
      expect(helpDialog.editor.destroy.args[0][0]).to.equal(true);
    });
  });

  function constructHelpRequestDialog() {
    let configuration = {
      get: (configConstant) => {
        return 15;
      }
    };

    let translateService = {
      translateInstant: (key) => {
        return key;
      }
    };

    let eventbus = {
      subscribe: sinon.spy()
    };
    let locationAdapter = {
      url: () => {
        return URL
      }
    };

    return new HelpDialog(translateService, eventbus, configuration, locationAdapter);
  };

  function buildForm() {
    return {
      models: {
        'definitionId':'help-request',
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
            'label': 'help.request.dialog.subject.label',
            'validators': [{
              'id': 'mandatory',
              'level': 'error',
              'message': 'validation.field.mandatory'
            }]
          },
            {
              'identifier': 'type',
              'dataType': 'text',
              'displayType': 'EDITABLE',
              'previewEmpty': true,
              'disabled': false,
              'isMandatory': true,
              'codelist': 15,
              'label': 'help.request.dialog.type.label',
              'validators': [
                {
                  id: 'mandatory',
                  'message': 'validation.field.mandatory',
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
              'label': 'help.request.dialog.subject.label',
              'validators': [{
                'id': 'mandatory',
                'level': 'error',
                'message': 'validation.field.mandatory'
              }]
            }
          ]
        })
      }
    }
  }
});