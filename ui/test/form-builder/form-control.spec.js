import {FormControl} from 'form-builder/form-control';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModelProperty} from 'models/definition-model';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_POSITION_ABOVE} from 'form-builder/form-wrapper';

describe('FormControl', () => {

  describe('getFieldViewMode()', () => {
    it('should resolve the field view mode using the form view mode and field.preview property', () => {
      let testData = [
        {formViewMode: 'EDIT', fieldPreview: true, expected: 'PREVIEW'},
        {formViewMode: 'EDIT', fieldPreview: false, expected: 'EDIT'},
        {formViewMode: 'PREVIEW', fieldPreview: true, expected: 'PREVIEW'},
        {formViewMode: 'PREVIEW', fieldPreview: false, expected: 'PREVIEW'},
        {formViewMode: 'PRINT', fieldPreview: true, expected: 'PRINT'},
        {formViewMode: 'PRINT', fieldPreview: false, expected: 'PRINT'}
      ];
      let formControl = new FormControl();
      formControl.widgetConfig = {};
      formControl.fieldViewModel = {};
      testData.forEach((data) => {
        formControl.widgetConfig.formViewMode = data.formViewMode;
        formControl.fieldViewModel.preview = data.fieldPreview;
        expect(formControl.getFieldViewMode()).to.equal(data.expected);
      });
    });
  });

  describe('getValidationStatusClass', () => {
    var flatFormViewModel = {};
    var validationModel = {
      field1: {
        valid: false
      }
    };
    var form = {};
    var fieldViewModel = {
      identifier: 'field1'
    };

    it('should return has-error if field is set to be valid=false', () => {
      var control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      var cssClass = control.getValidationStatusClass();
      expect(cssClass).to.equal('has-error');
    });

    it('should return empty string if field has no valid property set yet', () => {
      validationModel.field1.valid = undefined;
      var control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      var cssClass = control.getValidationStatusClass();
      expect(cssClass).to.be.empty;
    });

    it('should return empty string if field is set to be valid=true', () => {
      validationModel.field1.valid = true;
      var control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      var cssClass = control.getValidationStatusClass();
      expect(cssClass).to.be.empty;
    });
  });

  describe('renderMandatoryMark', function () {
    it('should throw an Error when the control does not have a viewModel instance property', function () {
      var control = new FormControlMock();
      expect(function () {
        control.renderMandatoryMark()
      }).to.throw(Error);
    });

    it('should return true when field is mandatory and not in preview', function () {
      var control = new FormControlWithViewModelMock({
        isMandatory: true,
        preview: false
      });
      var isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.true;
    });

    it('should return false when field is mandatory and in preview', function () {
      var control = new FormControlWithViewModelMock({
        isMandatory: true,
        preview: true
      });
      var isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });

    it('should return false when field is not mandatory and not in preview', function () {
      var control = new FormControlWithViewModelMock({
        isMandatory: false,
        preview: false
      });
      var isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });

    it('should return false when field is not mandatory and in preview', function () {
      var control = new FormControlWithViewModelMock({
        isMandatory: false,
        preview: true
      });
      var isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });
  });

  describe('#getPlaceholder', () => {
    it('should return the control label as placeholder when label position and show placeholder conditions are same', () => {
      var control = new FormControlWithViewModelMock({
        label: 'label'
      }, {
        labelPosition: LABEL_POSITION_HIDE,
        showFieldPlaceholderCondition: LABEL_POSITION_HIDE
      });
      var placeholder = control.getPlaceholder();
      expect(placeholder).to.equal('label');
    });

    it('should return empty string when label position and show placeholder conditions differs', () => {
      var control = new FormControlWithViewModelMock({
        label: 'label'
      }, {
        labelPosition: LABEL_POSITION_ABOVE,
        showFieldPlaceholderCondition: LABEL_POSITION_HIDE
      });
      var placeholder = control.getPlaceholder();
      expect(placeholder).to.equal('');
    });
  });

  it('validateForm should call validation service', function () {
    var spyValidateMethod = sinon.spy();
    var validationService = {
      validate: spyValidateMethod
    };
    //validation model is wrapped in an instance model.
    var validationModel = new InstanceModel({});
    var flatFormViewModel = {};
    var widgetConfig = {
      models: {
        id: 'emf:123456'
      },
      renderMandatory: false
    };
    var control = new FormControlMock(validationService, validationModel, flatFormViewModel, widgetConfig, null, null, 'emf:123456', {});
    control.validateForm();
    expect(spyValidateMethod.callCount).to.equal(1);
    var callArgs = spyValidateMethod.getCall(0).args;
    // expected arguments: validationModel, viewModel, id, execution, formControl
    expect(callArgs[0].serialize()).to.eql({});
    expect(callArgs[1]).to.eql({});
    expect(callArgs[2]).to.eql('emf:123456');
    expect(callArgs[3]).to.be.false;
    expect(callArgs[4]).to.eql(control);
  });

  describe('#ngOnDestroy', ()=> {
    var formControlValue, outerControlValue;
    var formControlChangeHandler = function (payload) {
      let payloadAttribute = Object.keys(payload)[0];
      formControlValue = payload[payloadAttribute];
    };
    var outerControlChangeHandler = function (payload) {
      let payloadAttribute = Object.keys(payload)[0];
      outerControlValue = payload[payloadAttribute];
    };

    var validationModel = new InstanceModel({
      'testModel': {
        value: 10
      }
    });
    var fieldViewModel = new DefinitionModelProperty({identifier: 'testModel', preview: false, rendered: false});

    it('should unsubscribe from control validationModel event emitter but subscriptions from other controls to remain.', ()=> {
      validationModel.testModel.subscribe('propertyChanged', outerControlChangeHandler);
      var control = new FormControlMock(null, validationModel, null, null, null, null, null);
      var validationModelSubscription = control.validationModel.testModel.subscribe('propertyChanged', formControlChangeHandler);
      control.validationModelSubscription = validationModelSubscription;

      validationModel.testModel.value = 100;
      expect(formControlValue).to.equal(100);
      expect(outerControlValue).to.equal(100);
      control.ngOnDestroy();

      validationModel.testModel.value = 200;
      expect(formControlValue).to.not.equal(200);
      expect(outerControlValue).to.equal(200);
    });

    it('should unsubscribe from control fieldViewModel event emitter but subscriptions from other controls to remain.', ()=> {
      fieldViewModel.subscribe('propertyChanged', outerControlChangeHandler);
      var control = new FormControlMock(null, null, null, null, null, fieldViewModel, null);
      var fieldViewModelSubscription = control.fieldViewModel.subscribe('propertyChanged', formControlChangeHandler);

      control.fieldViewModelSubscription = fieldViewModelSubscription;
      fieldViewModel.rendered = true;
      expect(formControlValue).to.be.true;
      expect(outerControlValue).to.be.true;
      control.ngOnDestroy();

      validationModel.testModel.value = 200;
      expect(formControlValue).to.not.equal(200);
      fieldViewModel.rendered = false;
      expect(formControlValue).to.not.be.false;
      expect(outerControlValue).to.be.false;
    });

  });
});

class FormControlMock extends FormControl {
  constructor(validatetionService, validationModel, flatFormViewModel, widgetConfig, form, fieldViewModel, objectId) {
    super();
    this.validationService = validatetionService;
    this.validationModel = validationModel;
    this.flatFormViewModel = flatFormViewModel;
    this.widgetConfig = widgetConfig;
    this.form = form;
    this.fieldViewModel = fieldViewModel;
    this.objectId = objectId;
  }
}

class FormControlWithViewModelMock extends FormControl {
  constructor(fieldViewModel, widgetConfig) {
    super();
    this.fieldViewModel = fieldViewModel;
    this.widgetConfig = widgetConfig;
  }
}