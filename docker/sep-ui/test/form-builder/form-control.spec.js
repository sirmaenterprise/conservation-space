import {FormControl} from 'form-builder/form-control';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModelProperty} from 'models/definition-model';
import {EventEmitter} from 'common/event-emitter';
import {LABEL_POSITION_HIDE, LABEL_POSITION_ABOVE} from 'form-builder/form-wrapper';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';
import {stub} from 'test/test-utils';

describe('FormControl', () => {
  let formControlConstructorStub;

  before(() => {
    formControlConstructorStub = sinon.stub(FormControl.prototype, 'constructor');
  });

  after(() => {
    formControlConstructorStub.restore();
  });

  it('should pass through all needed configurations to successor', () => {
    FormControl.prototype.formWrapper = {
      getViewModel: () => {
        return { title: { identifier: 'title' } };
      },
      formConfig: {
        models: {
          id: 'emf:1',
          isNewInstance: true,
          validationModel: {
            title: { value: 'Case 1' }
          }
        }
      },
      fieldsMap: {
        title: { identifier: 'title' }
      },
      objectDataForm: { formController: {} },
      validationService: {},
      config: { config: {} }
    };
    FormControl.prototype.identifier = 'title';
    let control = new FormControl();

    expect(control.fieldViewModel).to.eql({ identifier: 'title'});
    expect(control.validationModel).to.eql({ title: { value: 'Case 1'} });
    expect(control.flatFormViewModel).to.eql({ title: { identifier: 'title' } });
    expect(control.form).to.eql({ formController: {} });
    expect(control.validationService).to.eql({});
    expect(control.widgetConfig).to.eql( { config: {} });
    expect(control.objectId).to.equal('emf:1');
    expect(control.isNewInstance).to.be.true;
  });

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
      let formControl = new FormControlMock(undefined, undefined, undefined, {}, undefined, {}, undefined);
      testData.forEach((data) => {
        formControl.widgetConfig.formViewMode = data.formViewMode;
        formControl.fieldViewModel.preview = data.fieldPreview;
        expect(formControl.getFieldViewMode()).to.equal(data.expected);
      });
    });
  });

  describe('getValidationStatusClass', () => {
    let flatFormViewModel = {};
    let validationModel = {
      field1: {
        valid: false
      }
    };
    let form = {};
    let fieldViewModel = {
      identifier: 'field1'
    };

    it('should return has-error if field is set to be valid=false', () => {
      let control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      let cssClass = control.getValidationStatusClass();
      expect(cssClass).to.equal('has-error');
    });

    it('should return empty string if field has no valid property set yet', () => {
      validationModel.field1.valid = undefined;
      let control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      let cssClass = control.getValidationStatusClass();
      expect(cssClass).to.be.empty;
    });

    it('should return empty string if field is set to be valid=true', () => {
      validationModel.field1.valid = true;
      let control = new FormControlMock({}, validationModel, flatFormViewModel, {}, form, fieldViewModel);
      let cssClass = control.getValidationStatusClass();
      expect(cssClass).to.be.empty;
    });
  });

  describe('renderMandatoryMark', function () {
    it('should throw an Error when the control does not have a viewModel instance property', function () {
      let control = new FormControlMock();
      expect(function () {
        control.renderMandatoryMark();
      }).to.throw(Error);
    });

    it('should return true when field is mandatory and not in preview', function () {
      let control = new FormControlWithViewModelMock({
        isMandatory: true,
        preview: false
      });
      let isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.true;
    });

    it('should return false when field is mandatory and in preview', function () {
      let control = new FormControlWithViewModelMock({
        isMandatory: true,
        preview: true
      });
      let isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });

    it('should return false when field is not mandatory and not in preview', function () {
      let control = new FormControlWithViewModelMock({
        isMandatory: false,
        preview: false
      });
      let isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });

    it('should return false when field is not mandatory and in preview', function () {
      let control = new FormControlWithViewModelMock({
        isMandatory: false,
        preview: true
      });
      let isMandatory = control.renderMandatoryMark();
      expect(isMandatory).to.be.false;
    });
  });

  describe('#getPlaceholder', () => {
    it('should return the control label as placeholder when label position and show placeholder conditions are same', () => {
      let control = new FormControlWithViewModelMock({
        label: 'label'
      }, {
        labelPosition: LABEL_POSITION_HIDE,
        showFieldPlaceholderCondition: LABEL_POSITION_HIDE
      });
      let placeholder = control.getPlaceholder();
      expect(placeholder).to.equal('label');
    });

    it('should return empty string when label position and show placeholder conditions differs', () => {
      let control = new FormControlWithViewModelMock({
        label: 'label'
      }, {
        labelPosition: LABEL_POSITION_ABOVE,
        showFieldPlaceholderCondition: LABEL_POSITION_HIDE
      });
      let placeholder = control.getPlaceholder();
      expect(placeholder).to.equal('');
    });
  });

  it('validateForm should call validation service', function () {
    let spyValidateMethod = sinon.spy();
    let validationService = {
      validate: spyValidateMethod
    };
    //validation model is wrapped in an instance model.
    let validationModel = new InstanceModel({});
    let flatFormViewModel = {};
    let widgetConfig = {
      models: {
        id: 'emf:123456'
      },
      renderMandatory: false
    };
    let control = new FormControlMock(validationService, validationModel, flatFormViewModel, widgetConfig, null, null, 'emf:123456', {});
    control.formWrapper = {definitionId: 'definitionId'};
    control.validateForm();
    expect(spyValidateMethod.callCount).to.equal(1);
    let callArgs = spyValidateMethod.getCall(0).args;
    // expected arguments: validationModel, viewModel, id, execution, formControl
    expect(callArgs[0].serialize()).to.eql({});
    expect(callArgs[1]).to.eql({});
    expect(callArgs[2]).to.eql('emf:123456');
    expect(callArgs[3]).to.be.false;
    expect(callArgs[4]).to.eql(control);
  });

  describe('#ngOnDestroy', ()=> {
    let formControlValue, outerControlValue;
    let formControlChangeHandler = function (payload) {
      let payloadAttribute = Object.keys(payload)[0];
      formControlValue = payload[payloadAttribute];
    };
    let outerControlChangeHandler = function (payload) {
      let payloadAttribute = Object.keys(payload)[0];
      outerControlValue = payload[payloadAttribute];
    };

    let validationModel = new InstanceModel({
      'testModel': {
        value: 10
      }
    });
    let fieldViewModel = new DefinitionModelProperty({identifier: 'testModel', preview: false, rendered: false});

    it('should unsubscribe from control validationModel event emitter but subscriptions from other controls to remain.', ()=> {
      validationModel.testModel.subscribe('propertyChanged', outerControlChangeHandler);
      let control = new FormControlMock(null, validationModel, null, null, null, null, null);
      control.validationModelSubscription = control.validationModel.testModel.subscribe('propertyChanged', formControlChangeHandler);

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
      let control = new FormControlMock(null, null, null, null, null, fieldViewModel, null);

      control.fieldViewModelSubscription = control.fieldViewModel.subscribe('propertyChanged', formControlChangeHandler);
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

  describe('#isControl', ()=> {
    let fieldViewModel = new DefinitionModelProperty({
      identifier: 'testModel',
      control: [{identifier: DEFAULT_VALUE_PATTERN}]
    });

    it('should return true if field is control of given type', ()=> {
      let control = new FormControlMock(null, null, null, null, null, fieldViewModel, null);
      expect(control.isControl(DEFAULT_VALUE_PATTERN)).to.be.true;
    });

    it('should return false if field is not control of given type', ()=> {
      let control = new FormControlMock(null, null, null, null, null, fieldViewModel, null);
      expect(control.isControl('PICKER')).to.be.false;
    });
  });

  describe('#ngAfterViewInit', () => {

    it('should not emit event by default to formWrapper',() => {
      let control = new FormControlMock(null, null, null, null, null, null, null);
      control.ngAfterViewInit();
      expect(control.formEventEmitter.publish.callCount).to.equal(0);
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
    this.formEventEmitter = stub(EventEmitter);
  }
}

class FormControlWithViewModelMock extends FormControl {
  constructor(fieldViewModel, widgetConfig) {
    super();
    this.fieldViewModel = fieldViewModel;
    this.widgetConfig = widgetConfig;
  }
}