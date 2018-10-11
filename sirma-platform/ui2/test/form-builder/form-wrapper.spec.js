import {FormWrapper} from 'form-builder/form-wrapper';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {DefinitionModel, DefinitionModelProperty} from  'models/definition-model';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {EventEmitter} from 'common/event-emitter';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';

describe('FormWrapper', () => {

  let formWrapper;

  let $compile = () => {
    return () => {
      return ['test'];
    };
  };
  let logger = {
    warn: (msg) => {
      console.log(msg);
    }
  };
  let pluginsService = {
    loadComponentModules: () => {
      return Promise.resolve(controls);
    }
  };
  let validationService = {
    init: () => PromiseStub.resolve(),
    validate: () => {
    }
  };
  let configuration = {
    get: () => {
      return 50;
    }
  };
  let eventbus = IdocMocks.mockEventBus();

  afterEach(()=>{
    FormWrapper.prototype.config = {};
    FormWrapper.prototype.formConfig = { models: {}};
  });

  describe('when created', () => {

    let initStub;

    beforeEach(() => {
      initStub = sinon.stub(FormWrapper.prototype, 'init');
    });
    afterEach(() => {
      initStub.restore();
    });

    it('should set watcher for shouldRealod property that triggers init when changed', () => {
      let viewModel = buildViewModel();
      // Given Object properties are rendered
      FormWrapper.prototype.config = {
        shouldReload: false
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel.clone()
        }
      };

      getBuilderInstance();

      expect(initStub.callCount).to.equal(1);
      // When shouldReload flag is set
      FormWrapper.prototype.config.shouldReload = true;
      formWrapper.$scope.$digest();
      // Then the form should be rebuilt
      expect(initStub.callCount).to.equal(2);
    });

    it('should set watcher for formViewMode property', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel.clone()
        },
        writeAllowed: true
      };
      getBuilderInstance();
      expect(initStub.callCount).to.equal(1);
      FormWrapper.prototype.config.formViewMode = FormWrapper.FORM_VIEW_MODE_PREVIEW;
      formWrapper.$scope.$digest();
      expect(initStub.callCount).to.equal(2);
      expect(FormWrapper.prototype.config.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_PREVIEW);
    });

    it('should set watcher for selectedProperties config', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        selectedProperties: {
          TYPE1: ['property1']
        }
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel.clone(),
          validationModel: {}
        }
      };
      getBuilderInstance();
      expect(initStub.callCount).to.equal(1);
      FormWrapper.prototype.config.selectedProperties = ['field1'];
      formWrapper.$scope.$digest();
      expect(initStub.callCount).to.equal(2);
      expect(formWrapper.renderAll).to.be.false;
      FormWrapper.prototype.config.selectedProperties = undefined;
      formWrapper.$scope.$digest();
      expect(initStub.callCount).to.equal(3);
      expect(formWrapper.renderAll).to.be.true;
    });

    it('should set watcher for showAllProperties config', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        showAllProperties: true
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel.clone(),
          validationModel: {}
        }
      };
      getBuilderInstance();
      expect(initStub.callCount).to.equal(1);
      FormWrapper.prototype.config.showAllProperties = false;
      formWrapper.$scope.$digest();
      expect(initStub.callCount).to.equal(2);
      expect(formWrapper.renderAll).to.be.false;
      FormWrapper.prototype.config.showAllProperties = true;
      formWrapper.$scope.$digest();
      expect(initStub.callCount).to.equal(3);
      expect(formWrapper.renderAll).to.be.true;
    });

    it('should initialize local formViewMode variable equal to one provided with the config or EDIT if not provided', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.formConfig.models = {
        viewModel: viewModel.clone()
      };
      FormWrapper.prototype.config = {};
      getBuilderInstance();
      expect(formWrapper.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_EDIT);
      FormWrapper.prototype.config = {
        formViewMode: FormWrapper.FORM_VIEW_MODE_PREVIEW
      };
      getBuilderInstance();
      expect(formWrapper.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_PREVIEW);
    });

    it('should clone provided viewModel', () => {
      let viewModel = buildViewModel().clone();
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT',
        filterFields: ['inputTextPreview']
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel
        }
      };

      getBuilderInstance();

      expect(FormWrapper.prototype.config.viewModel === viewModel).to.be.false;
    });

    it('should clone provided viewModel when object definition type is changed', () => {
      // given I have rendered object with its view model
      let viewModel = buildViewModel();
      let initialViewModel = viewModel.clone();
      let newViewModel = viewModel.clone();
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT',
        filterFields: ['inputTextPreview']
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: initialViewModel,
          validationModel: {},
          definitionId: 'TYPE1'
        }
      };

      getBuilderInstance();

      // when I change the object type and provided a new model
      FormWrapper.prototype.formConfig.models.viewModel = newViewModel;
      FormWrapper.prototype.formConfig.models.definitionId = 'TYPE2';
      formWrapper.$scope.$digest();
      // then the new object model should be cloned
      expect(FormWrapper.prototype.clonedViewModel === newViewModel).to.be.false;
      expect(FormWrapper.prototype.formConfig.models.viewModel).to.eql(newViewModel);
    });

    it('should set selectedProperties attribute if there are selected properties for the given instance type', () => {
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT',
        selectedProperties: {
          TYPE1: {'property1': {'name': 'property1'}}
        }
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: new DefinitionModel({
            fields: []
          }),
          validationModel: {},
          definitionId: 'TYPE1'
        }
      };

      getBuilderInstance();

      expect(formWrapper.selectedProperties).to.eql(['property1']);
      expect(formWrapper.renderAll).to.be.false;
    });

    it('should set selectedProperties attribute to empty array if there are no selected properties for the given instance type', () => {
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT',
        selectedProperties: {
          ANOTHER_TYPE: ['property1']
        }
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: new DefinitionModel({
            fields: []
          }),
          validationModel: {},
          definitionId: 'TYPE1'
        }
      };

      getBuilderInstance();

      expect(formWrapper.selectedProperties).to.not.be.undefined;
      expect(formWrapper.selectedProperties).to.be.empty;
      expect(formWrapper.renderAll).to.be.false;
    });

    it('should set selectedProperties attribute to empty array and renderAll to true if there are no selected properties', () => {
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT'
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: new DefinitionModel({
            fields: []
          }),
          validationModel: {},
          definitionId: 'TYPE1'
        }
      };

      getBuilderInstance();

      expect(formWrapper.selectedProperties).to.eql([]);
      expect(formWrapper.renderAll).to.be.true;
    });

    it('should assign handler for AfterFormValidationEvent and to process model from currently validated object', () => {
      FormWrapper.prototype.formConfig = {
        models: {
          id: 'emf:123456',
          viewModel: new DefinitionModel({
            fields: []
          }),
          validationModel: {},
          definitionId: 'TYPE1'
        },
        eventEmitter: stub(EventEmitter)
      };
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT'
      };

      let $scope = mock$scope();
      let eventbus = {
        subscriptions: {},
        subscribe: (event, handler) => {
          eventbus.subscriptions[event.name] = handler;
        },
        publish: (name, payload) => {
          eventbus.subscriptions[name](payload);
        }
      };
      let spySubscribe = sinon.spy(eventbus, 'subscribe');
      let elementMock = IdocMocks.mockElement();
      elementMock.addClass = sinon.spy();
      getBuilderInstance(elementMock, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration, IdocMocks.mockTimeout());
      formWrapper.ngOnInit();

      expect(spySubscribe.calledOnce).to.be.true;
      expect(spySubscribe.getCall(0).args[0].name).to.equal('AfterFormValidationEvent');
      spySubscribe.restore();

      elementMock.addClass.reset();
      getBuilderInstance(elementMock, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration, IdocMocks.mockTimeout());
      formWrapper.ngOnInit();
      let spyFilterFields = sinon.spy(FormWrapper, 'filterFields');
      eventbus.publish(AfterFormValidationEvent.name, [{
        id: 'emf:123456'
      }]);
      expect(spyFilterFields.calledOnce).to.be.true;
      expect(elementMock.addClass.callCount).to.equals(1);
      expect(elementMock.addClass.getCall(0).args[0]).to.equals('initialized');
      spyFilterFields.restore();
    });

    it('should handle print mode when AfterFormValidationEvent is fired', () => {
      const widgetEventEmitter = stub(EventEmitter);
      FormWrapper.prototype.formConfig = {
        models: {
          id: 'emf:123456',
          viewModel: new DefinitionModel({
            fields: []
          }),
          validationModel: {},
          definitionId: 'TYPE1'
        },
        eventEmitter: widgetEventEmitter
      };

      const eventEmitter = new EventEmitter();
      FormWrapper.prototype.config = {
        formViewMode: 'PRINT',
        eventEmitter
      };

      let $scope = mock$scope();
      let eventbus = {
        subscriptions: {},
        subscribe: (event, handler) => {
          eventbus.subscriptions[event.name] = handler;
        },
        publish: (name, payload) => {
          eventbus.subscriptions[name](payload);
        }
      };
      let elementMock = IdocMocks.mockElement();

      // when all object properties are loaded, the view is considered ready and so the form
      getBuilderInstance(elementMock, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration, IdocMocks.mockTimeout());
      formWrapper.objectProperties = {'emf:createdBy': false, 'emf:references': false};
      formWrapper.fieldsMap = {
        'emf:createdBy': {
          controlId: 'PICKER',
          rendered: true
        },
        'emf:references': {
          controlId: 'PICKER',
          rendered: true
        }
      };
      formWrapper.ngOnInit();
      eventbus.publish(AfterFormValidationEvent.name, [{
        id: 'emf:123456'
      }]);
      eventEmitter.publish('formControlLoaded', {identifier: 'emf:createdBy'});
      expect(widgetEventEmitter.publish.callCount, 'form is not rendered completely').to.equal(0);

      eventEmitter.publish('formControlLoaded', {identifier: 'emf:references'});
      expect(widgetEventEmitter.publish.callCount, 'form is rendered completely').to.equal(1);

      // when no object properties are to be loaded, form should be initialized also
      widgetEventEmitter.publish.reset();
      getBuilderInstance(elementMock, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration, IdocMocks.mockTimeout());
      formWrapper.ngOnInit();

      eventbus.publish(AfterFormValidationEvent.name, [{
        id: 'emf:123456'
      }]);

      expect(widgetEventEmitter.publish.callCount, 'form initialized should be published once when no object properties are present').to.equal(1);
      expect(widgetEventEmitter.publish.getCall(0).args).to.eql(['formInitialized', true]);
    });
  });

  describe('#buildForm', () => {

    it('should render all visible fields if config.renderMandatory=false which is the default', () => {
      let formViewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT'
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: formViewModel
        }
      };
      getBuilderInstance();
      formWrapper.fieldsMap = formViewModel.clone().flatModelMap;
      let createdHtml = formWrapper.buildForm(formViewModel.fields, buildValidationModel(), 'EDIT');

      expect(createdHtml).to.equal('<seip-region id="inputTextFields-wrapper" ng-if="::formWrapper.fieldsMap[\'inputTextFields\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'inputTextFields\'"><seip-input-text id="field1-wrapper" ng-if="::formWrapper.fieldsMap[\'field1\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field1\'"></seip-input-text><seip-input-text id="field2-wrapper" ng-if="::formWrapper.fieldsMap[\'field2\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field2\'"></seip-input-text><seip-input-text id="field3-wrapper" ng-if="::formWrapper.fieldsMap[\'field3\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field3\'"></seip-input-text><seip-input-text id="field4-wrapper" ng-if="::formWrapper.fieldsMap[\'field4\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field4\'"></seip-input-text><seip-input-text id="field5-wrapper" ng-if="::formWrapper.fieldsMap[\'field5\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field5\'"></seip-input-text><seip-input-text id="field6-wrapper" ng-if="::formWrapper.fieldsMap[\'field6\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field6\'"></seip-input-text></seip-region>');
    });

    it('should add style attribute to controls if there is style provided in config', () => {
      let formViewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: 'EDIT',
        styles: {
          columns: {
            field1: {width: '100px;'},
            field2: {width: '100px;'},
            field3: {width: '100px;'},
            field4: {width: '100px;'},
            field5: {width: '100px;'},
            field6: {width: '100px;'}
          }
        }
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: formViewModel
        }
      };
      getBuilderInstance();
      formWrapper.fieldsMap = formViewModel.clone().flatModelMap;
      let createdHtml = formWrapper.buildForm(formViewModel.fields[0].fields, buildValidationModel(), 'EDIT');
      expect(createdHtml).to.equal('<seip-input-text id="field1-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field1\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field1\'"></seip-input-text><seip-input-text id="field2-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field2\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field2\'"></seip-input-text><seip-input-text id="field3-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field3\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field3\'"></seip-input-text><seip-input-text id="field4-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field4\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field4\'"></seip-input-text><seip-input-text id="field5-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field5\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field5\'"></seip-input-text><seip-input-text id="field6-wrapper" ng-style="{ \'width\': \'100px;\' }" ng-if="::formWrapper.fieldsMap[\'field6\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field6\'"></seip-input-text>');
    });

    it('should render fields with not supported control types as empty cells', () => {
      let formViewModel = new ViewModelBuilder()
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false)
        .addField('field2', 'EDITABLE', 'text', 'field 2', false, false, [], 'UNSUPPORTED_CONTROL')
        .endRegion()
        .getModel();

      FormWrapper.prototype.config = {
        formViewMode: 'EDIT'
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: formViewModel
        }
      };
      getBuilderInstance();
      formWrapper.fieldsMap = formViewModel.clone().flatModelMap;
      let createdHtml = formWrapper.buildForm(formViewModel.fields, buildValidationModel(), 'EDIT');
      expect(createdHtml).to.equal('<seip-region id="inputTextFields-wrapper" ng-if="::formWrapper.fieldsMap[\'inputTextFields\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'inputTextFields\'"><seip-empty-cell id="field2-wrapper" ng-if="::formWrapper.fieldsMap[\'field2\'].rendered ? true : undefined" form-wrapper="::formWrapper" identifier="\'field2\'"></seip-empty-cell></seip-region>');
    });

    it('should return true if valid property is not set', () => {
      expect(FormWrapper.isValid(undefined)).to.be.true;
    });
  });

  describe('configureConditions', () => {
    it('should enable all conditions if renderMandatory=true or renderAll=true ', () => {
      let spyToggleConditions = sinon.spy(FormWrapper, 'toggleConditions');
      let clonedViewModel = {
        fields: []
      };
      let selectedFields = [];
      FormWrapper.configureConditions(clonedViewModel, selectedFields, true, 'EDIT', false);
      expect(spyToggleConditions.getCall(0).args).to.eql([[], [], false]);
      spyToggleConditions.restore();
    });

    it('should enable only VISIBLE/HIDDEN conditions if formViewMode=preview', () => {
      let spyToggleConditions = sinon.spy(FormWrapper, 'toggleConditions');
      let clonedViewModel = {
        fields: []
      };
      let selectedFields = [];
      FormWrapper.configureConditions(clonedViewModel, selectedFields, false, 'PREVIEW', false);
      expect(spyToggleConditions.getCall(0).args).to.eql([[], ['visible', 'hidden'], false]);
      spyToggleConditions.restore();
    });

    it('should enable all conditions if formViewMode=edit and selectedProperties > 0', () => {
      let spyToggleConditions = sinon.spy(FormWrapper, 'toggleConditions');
      let clonedViewModel = {
        fields: []
      };
      let selectedFields = ['field1', 'field3'];
      FormWrapper.configureConditions(clonedViewModel, selectedFields, false, 'EDIT', false);
      expect(spyToggleConditions.getCall(0).args).to.eql([[], [], false]);
      spyToggleConditions.restore();
    });

    it('should disable all conditions if formViewMode=edit and selectedProperties = 0', () => {
      let spyToggleConditions = sinon.spy(FormWrapper, 'toggleConditions');
      let clonedViewModel = {
        fields: []
      };
      let selectedFields = [];
      FormWrapper.configureConditions(clonedViewModel, selectedFields, false, 'EDIT', false);
      expect(spyToggleConditions.getCall(0).args).to.eql([[], [], true]);
      spyToggleConditions.restore();
    });
  });

  describe('toggleConditions', () => {

    it('should enable all conditions', () => {
      let viewModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [{'id': 'mandatory'}])
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false, [{'id': 'mandatory'}])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}])
        .addField('field3', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .endRegion()
        .addField('field4', 'EDITABLE', 'text', 'field 3', true, false, [{'id': 'mandatory'}])
        .addField('field5', 'EDITABLE', 'text', 'field 4', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .getModel().serialize();

      FormWrapper.toggleConditions(viewModel.fields, [], true);
      //field1
      expect(viewModel.fields[0].validators[0].disabled).to.be.true;
      //region
      expect(viewModel.fields[1].validators[0].disabled).to.be.true;
      //field2 in region
      expect(viewModel.fields[1].fields[0].validators[0].disabled).to.be.true;
      //field3 in region
      expect(viewModel.fields[1].fields[1].validators[0].disabled).to.be.true;
      expect(viewModel.fields[1].fields[1].validators[1].rules[0].disabled).to.be.true;
      //field4
      expect(viewModel.fields[2].validators[0].disabled).to.be.true;
      //field5
      expect(viewModel.fields[3].validators[0].disabled).to.be.true;
      expect(viewModel.fields[3].validators[1].rules[0].disabled).to.be.true;

      FormWrapper.toggleConditions(viewModel.fields, [], false);
      //field1
      expect(viewModel.fields[0].validators[0].disabled).to.be.false;
      //region
      expect(viewModel.fields[1].validators[0].disabled).to.be.false;
      //field2 in region
      expect(viewModel.fields[1].fields[0].validators[0].disabled).to.be.false;
      //field3 in region
      expect(viewModel.fields[1].fields[1].validators[0].disabled).to.be.false;
      expect(viewModel.fields[1].fields[1].validators[1].rules[0].disabled).to.be.false;
      //field4
      expect(viewModel.fields[2].validators[0].disabled).to.be.false;
      //field5
      expect(viewModel.fields[3].validators[0].disabled).to.be.false;
      expect(viewModel.fields[3].validators[1].rules[0].disabled).to.be.false;
    });

    it('should disable all mandatory conditions', () => {
      let viewModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [{'id': 'mandatory'}])
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false, [{'id': 'mandatory'}])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}])
        .addField('field3', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .endRegion()
        .addField('field4', 'EDITABLE', 'text', 'field 3', true, false, [{'id': 'mandatory'}])
        .addField('field5', 'EDITABLE', 'text', 'field 4', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .getModel().serialize();

      FormWrapper.toggleConditions(viewModel.fields, ['mandatory'], true);
      //field1
      expect(viewModel.fields[0].validators[0].disabled).to.be.true;
      //region
      expect(viewModel.fields[1].validators[0].disabled).to.be.true;
      //field2 in region
      expect(viewModel.fields[1].fields[0].validators[0].disabled).to.be.true;
      //field3 in region
      expect(viewModel.fields[1].fields[1].validators[0].disabled).to.be.true;
      expect(viewModel.fields[1].fields[1].validators[1].rules[0].disabled).to.be.false;
      //field4
      expect(viewModel.fields[2].validators[0].disabled).to.be.true;
      //field5
      expect(viewModel.fields[3].validators[0].disabled).to.be.true;
      expect(viewModel.fields[3].validators[1].rules[0].disabled).to.be.false;
    });

    it('should enable all mandatory and hidden conditions', () => {
      let viewModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [{'id': 'mandatory'}])
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false, [{'id': 'mandatory'}])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}])
        .addField('field3', 'EDITABLE', 'text', 'field 2', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .endRegion()
        .addField('field4', 'EDITABLE', 'text', 'field 3', true, false, [{'id': 'mandatory'}])
        .addField('field5', 'EDITABLE', 'text', 'field 4', true, false, [{'id': 'mandatory'}, {
          'id': 'condition',
          'rules': [{'id': 'hidden', 'renderAs': 'hidden'}]
        }])
        .getModel().serialize();

      FormWrapper.toggleConditions(viewModel.fields, ['hidden', 'mandatory'], false);
      //field1
      expect(viewModel.fields[0].validators[0].disabled).to.be.false;
      //region
      expect(viewModel.fields[1].validators[0].disabled).to.be.false;
      //field2 in region
      expect(viewModel.fields[1].fields[0].validators[0].disabled).to.be.false;
      //field3 in region
      expect(viewModel.fields[1].fields[1].validators[0].disabled).to.be.false;
      expect(viewModel.fields[1].fields[1].validators[1].rules[0].disabled).to.be.false;
      //field4
      expect(viewModel.fields[2].validators[0].disabled).to.be.false;
      //field5
      expect(viewModel.fields[3].validators[0].disabled).to.be.false;
      expect(viewModel.fields[3].validators[1].rules[0].disabled).to.be.false;
    });
  });

  describe('toggleCondition', () => {
    it('should enable the condition when it is selected to be disabled', () => {
      let condition = {'id': 'mandatory'};
      FormWrapper.toggleCondition(condition, ['mandatory'], true);
      expect(condition.disabled).to.be.true;
    });

    it('should disable the condition when it is selected to be enabled', () => {
      let condition = {'id': 'mandatory'};
      FormWrapper.toggleCondition(condition, ['mandatory'], false);
      expect(condition.disabled).to.be.false;
    });

    it('should invert the disabled attribute of the condition if it is not selected', () => {
      let condition = {'id': 'hidden', disabled: true};
      FormWrapper.toggleCondition(condition, ['mandatory'], true);
      expect(condition.disabled).to.be.false;
      FormWrapper.toggleCondition(condition, ['mandatory'], false);
      expect(condition.disabled).to.be.true;
    });
  });

  describe('filterFields', () => {

    let viewModel = {};
    let validationModel = {};
    let renderMandatory = true;
    let selectedProperties = [];
    let renderAll = false;
    let logger = {};
    let element = {
      closest: () => {
        return [{}, {}]
      }
    };

    it('should filter mandatory fields if renderMandatory=true', () => {
      let stub = sinon.stub(FormWrapper, 'filterMandatoryFields');
      FormWrapper.filterFields(viewModel, renderMandatory, selectedProperties, renderAll, FormWrapper.FORM_VIEW_MODE_EDIT, validationModel, logger, element);
      expect(stub.calledOnce).to.be.true;
      stub.restore();
    });

    it('should call toggleAllFields with model and show=true if renderAll=true', () => {
      let stub = sinon.stub(FormWrapper, 'toggleAllFields');
      renderMandatory = false;
      renderAll = true;
      FormWrapper.filterFields(viewModel, renderMandatory, selectedProperties, renderAll, FormWrapper.FORM_VIEW_MODE_EDIT, validationModel, logger, element);
      expect(stub.calledOnce).to.be.true;
      expect(stub.getCall(0).args).to.eql([{}, true, {}, FormWrapper.FORM_VIEW_MODE_EDIT, {}]);
      stub.restore();
    });

    it('should call toggleAllFields with model and show=false if no selected properties in edit mode', () => {
      let stub = sinon.stub(FormWrapper, 'toggleAllFields');
      renderMandatory = false;
      renderAll = false;
      FormWrapper.filterFields(viewModel, renderMandatory, selectedProperties, renderAll, FormWrapper.FORM_VIEW_MODE_EDIT, validationModel, logger, element);
      expect(stub.calledOnce).to.be.true;
      expect(stub.getCall(0).args).to.eql([{}, false, {}, FormWrapper.FORM_VIEW_MODE_EDIT, {}]);
      stub.restore();
    });

    it('should call filterSelectedFields with model and selectedProperties array if there are selected properties in preview and edit mode', () => {
      let stub = sinon.stub(FormWrapper, 'filterSelectedFields');
      selectedProperties = ['field1', 'field2'];
      renderMandatory = false;
      renderAll = false;
      FormWrapper.filterFields(viewModel, renderMandatory, selectedProperties, renderAll, FormWrapper.FORM_VIEW_MODE_EDIT, validationModel, logger, element);
      expect(stub.getCall(0).args).to.eql([{}, ['field1', 'field2'], {}, FormWrapper.FORM_VIEW_MODE_EDIT, {}]);

      FormWrapper.filterFields(viewModel, renderMandatory, selectedProperties, renderAll, FormWrapper.FORM_VIEW_MODE_PREVIEW, validationModel, logger, element);
      expect(stub.getCall(1).args).to.eql([{}, ['field1', 'field2'], {}, FormWrapper.FORM_VIEW_MODE_PREVIEW, {}]);
      stub.restore();
    });

  });

  describe('isPreviewMode', () => {
    it('should return true if formViewMode=PREVIEW', () => {
      expect(FormWrapper.isPreviewMode(FormWrapper.FORM_VIEW_MODE_PREVIEW)).to.be.true;
    });

    it('should return false if formViewMode=EDIT or PRINT', () => {
      expect(FormWrapper.isPreviewMode(FormWrapper.FORM_VIEW_MODE_EDIT)).to.be.false;
      expect(FormWrapper.isPreviewMode(FormWrapper.FORM_VIEW_MODE_PRINT)).to.be.false;
    });
  });

  describe('isEditMode', () => {
    it('should return true if formViewMode=EDIT', () => {
      expect(FormWrapper.isEditMode(FormWrapper.FORM_VIEW_MODE_EDIT)).to.be.true;
    });

    it('should return false if formViewMode=PREVIEW', () => {
      expect(FormWrapper.isEditMode(FormWrapper.FORM_VIEW_MODE_PREVIEW)).to.be.false;
      expect(FormWrapper.isEditMode(FormWrapper.FORM_VIEW_MODE_PRINT)).to.be.false;
    });
  });

  describe('isPrintMode', () => {
    it('should return true if formViewMode=PRINT', () => {
      expect(FormWrapper.isPrintMode(FormWrapper.FORM_VIEW_MODE_PRINT)).to.be.true;
    });

    it('should return false if formViewMode=EDIT or PREVIEW', () => {
      expect(FormWrapper.isPrintMode(FormWrapper.FORM_VIEW_MODE_EDIT)).to.be.false;
      expect(FormWrapper.isPrintMode(FormWrapper.FORM_VIEW_MODE_PREVIEW)).to.be.false;
    });
  });

  describe('writeAllowed', () => {
    it('should set mode PREVIEW when no writeAllowed and mode is EDIT', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT
      };
      FormWrapper.prototype.formConfig = {
        writeAllowed: false,
        models: {
          viewModel: viewModel.clone()
        }
      };
      getBuilderInstance();
      formWrapper.setFormViewMode(FormWrapper.FORM_VIEW_MODE_EDIT);
      expect(formWrapper.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_PREVIEW);
    });

    it('should set mode EDIT when writeAllowed and mode is EDIT', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT
      };
      FormWrapper.prototype.formConfig = {
        writeAllowed: true,
        models: {
          viewModel: viewModel.clone()
        }
      };
      getBuilderInstance();
      formWrapper.setFormViewMode(FormWrapper.FORM_VIEW_MODE_EDIT);
      expect(formWrapper.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_EDIT);
    });

    it('should set provided mode when writeAllowed is undefined', () => {
      let viewModel = buildViewModel();
      FormWrapper.prototype.config = {
        formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT
      };
      FormWrapper.prototype.formConfig = {
        models: {
          viewModel: viewModel.clone()
        }
      };
      getBuilderInstance();
      formWrapper.setFormViewMode(FormWrapper.FORM_VIEW_MODE_EDIT);
      expect(formWrapper.formViewMode).to.equal(FormWrapper.FORM_VIEW_MODE_EDIT);
    });
  });

  describe('toggleAllFields', () => {
    it('should toggle the rendered attribute of all fields in the view model', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let expectedAllHiddenModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let expectedAllVisibleModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'test', true)
        .addProperty('field2', 'test', true)
        .addProperty('field3', 'test', true)
        .addProperty('field4', 'test', true)
        .addProperty('field5', 'test', true)
        .getModel();
      let logger = {};
      FormWrapper.toggleAllFields(model.fields, false, validationModel, 'EDIT', logger);
      expect(model.serialize(), `All fields should be rendered=false (hidden) \nActual:${JSON.stringify(model.serialize())} \nExpected:${JSON.stringify(expectedAllHiddenModel.serialize())}\n`).to.eql(expectedAllHiddenModel.serialize());
      FormWrapper.toggleAllFields(model.fields, true, validationModel, 'EDIT', logger);
      expect(model.serialize(), `All fields should be rendered=true (visible) \nActual:${JSON.stringify(model.serialize())} \nExpected:${JSON.stringify(expectedAllHiddenModel.serialize())}\n`).to.eql(expectedAllVisibleModel.serialize());
    });
  });

  describe('filterSelectedFields', () => {
    it('should set rendered=false to all fields that are not selected and the region is rendered because of one rendered field inside', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .addProperty('field2', 'field2')
        .addProperty('field3', 'field3')
        .addProperty('field4', 'field4')
        .addProperty('field5', 'field5')
        .getModel();
      let selectedFields = ['field2', 'field3', 'field4'];
      FormWrapper.filterSelectedFields(model.fields, selectedFields, validationModel, FormWrapper.FORM_VIEW_MODE_EDIT, {});
      expect(model.serialize()).to.eql(expectedModel.serialize());
    });

    it('should set rendered=false to all fields that are not selected and the region is not rendered because there are no rendered fields inside', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .addProperty('field2', 'field2')
        .addProperty('field3', 'field3')
        .addProperty('field4', 'field4')
        .addProperty('field5', 'field5')
        .getModel();
      let selectedFields = ['field2', 'field3'];
      FormWrapper.filterSelectedFields(model.fields, selectedFields, validationModel, FormWrapper.FORM_VIEW_MODE_EDIT, {});
      expect(model.serialize()).to.eql(expectedModel.serialize());
    });

    it('should set rendered=false to all fields when there is no selection', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .addProperty('field2', 'field2')
        .addProperty('field3', 'field3')
        .addProperty('field4', 'field4')
        .addProperty('field5', 'field5')
        .getModel();
      let selectedFields = [];
      FormWrapper.filterSelectedFields(model.fields, selectedFields, validationModel, FormWrapper.FORM_VIEW_MODE_EDIT, {});
      expect(model.serialize()).to.eql(expectedModel.serialize());
    });

    it('should set rendered=true to all fields when all of them are selected', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .addProperty('field2', 'field2')
        .addProperty('field3', 'field3')
        .addProperty('field4', 'field4')
        .addProperty('field5', 'field5')
        .getModel();
      let selectedFields = ['field1', 'field2', 'field3', 'field4', 'field5'];
      FormWrapper.filterSelectedFields(model.fields, selectedFields, validationModel, FormWrapper.FORM_VIEW_MODE_EDIT, {});
      expect(model.serialize()).to.eql(expectedModel.serialize());
    });
  });

  describe('filterMandatoryFields', () => {
    it('should set rendered=true to all mandatory and invalid fields', () => {
      let model = new ViewModelBuilder()
      // invalid optional field
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, false, [])
        // invalid optional field
        .addField('field5', 'EDITABLE', 'text', 'field 5', false, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', false)
        .addProperty('field2', 'field2', true)
        .addProperty('field3', 'field3', true)
        .addProperty('field4', 'field4', true)
        .addProperty('field5', 'field5', false)
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, true, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', false, true, [])
        .endRegion()
        .getModel();
      let hasRenderedFields = FormWrapper.filterMandatoryFields(model.fields, validationModel);
      expect(hasRenderedFields).to.be.true;
      expect(model.serialize(), `Expected model \n${JSON.stringify(expectedModel)} \n Actual  model \n ${JSON.stringify(model)}`).to.eql(expectedModel.serialize());
    });

    it('should set rendered=false to all optional fields and the region should be rendered because there are mandatory fields inside', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', true)
        .addProperty('field2', 'field2', true)
        .addProperty('field3', 'field3', true)
        .addProperty('field4', 'field4', true)
        .addProperty('field5', 'field5', true)
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();

      let hasRenderedFields = FormWrapper.filterMandatoryFields(model.fields, validationModel);
      expect(hasRenderedFields).to.be.true;
      expect(model.serialize(), `Expected model \n${JSON.stringify(expectedModel)} \n Actual  model \n ${JSON.stringify(model)}`).to.eql(expectedModel.serialize());
    });

    it('should set rendered=false to all optional fields and the region should be rendered because there are mandatory fields inside', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', true)
        .addProperty('field2', 'field2', true)
        .addProperty('field3', 'field3', true)
        .addProperty('field4', 'field4', true)
        .addProperty('field5', 'field5', true)
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, true)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [])
        .endRegion()
        .getModel();

      let hasRenderedFields = FormWrapper.filterMandatoryFields(model.fields, validationModel);
      expect(hasRenderedFields).to.be.true;
      expect(model.serialize(), `Expected model \n${JSON.stringify(expectedModel)} \n Actual  model \n ${JSON.stringify(model)}`).to.eql(expectedModel.serialize());
    });

    it('should set rendered=false to all fields when all of them are optional', () => {
      let model = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', false, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', false, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', true)
        .addProperty('field2', 'field2', true)
        .addProperty('field3', 'field3', true)
        .addProperty('field4', 'field4', true)
        .addProperty('field5', 'field5', true)
        .getModel();
      let expectedModel = new ViewModelBuilder()
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, false, [])
        .addField('field2', 'EDITABLE', 'text', 'field 2', false, false, [])
        .addField('field3', 'EDITABLE', 'text', 'field 3', false, false, [])
        .addRegion('region1', 'region 1', 'EDITABLE', false, false)
        .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
        .addField('field5', 'EDITABLE', 'text', 'field 5', false, false, [])
        .endRegion()
        .getModel();

      let hasRenderedFields = FormWrapper.filterMandatoryFields(model.fields, validationModel);
      expect(hasRenderedFields).to.be.false;
      expect(model.serialize(), `Expected model \n${JSON.stringify(expectedModel)} \n Actual  model \n ${JSON.stringify(model)}`).to.eql(expectedModel.serialize());
    });
  });

  describe('isPreview method', () => {
    it('should return false if no viewModel is provided', function () {
      let isPreview = FormWrapper.isPreview();
      expect(isPreview).to.be.false;
    });

    it('should return false if viewModel.displayType=EDITABLE and no formViewMode is provided', function () {
      let isPreview = FormWrapper.isPreview({
        'displayType': 'EDITABLE'
      });
      expect(isPreview).to.be.true;
    });

    it('should return false if viewModel.displayType=EDITABLE and formViewMode=EDIT is provided', function () {
      let isPreview = FormWrapper.isPreview({
        'displayType': 'EDITABLE'
      }, 'EDIT');
      expect(isPreview).to.be.false;
    });

    it('should return true if viewModel.displayType=READ_ONLY and no formViewMode is provided', function () {
      let isPreview = FormWrapper.isPreview({
        'displayType': 'READ_ONLY'
      });
      expect(isPreview).to.be.true;
    });

    it('should return true if viewModel.displayType=READ_ONLY and formViewMode=EDIT is provided', function () {
      let isPreview = FormWrapper.isPreview({
        'displayType': 'READ_ONLY'
      }, 'EDIT');
      expect(isPreview).to.be.true;
    });

    it('should return true if viewModel.displayType=READ_ONLY and formViewMode=EDIT is provided', function () {
      let isPreview = FormWrapper.isPreview({
        'displayType': 'READ_ONLY'
      }, 'PREVIEW');
      expect(isPreview).to.be.true;
    });
  });

  describe('isVisibleField', () => {
    let viewModel = {};
    let validationModel = {};

    it('should return true if the field is region and is mandatory', () => {
      let viewModel = ViewModelBuilder.createRegion('region', 'Region', 'EDITABLE', false, false);
      viewModel.isMandatory = true;
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should return false if the field visibility can`t be resolved', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'WRONG_TYPE', 'text', 'Field 1', true, true, [], null);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.false;
    });

    it('should return true if key evaluates to true and condition has not changed the display type on hidden field in "PREVIEW" mode', () => {
      let viewModel = new DefinitionModelProperty({
        dataType: 'text',
        _displayType: 'HIDDEN',
        displayType: 'HIDDEN',
        identifier: 'testProp',
        isDataProperty: true,
        isMandatory: false,
        label: 'Test property',
        maxLength: 1024,
        multivalue: false,
        preview: true,
        previewEmpty: true,
        rendered: true,
        validators: []
      });
      let validationModel = new ValidationModelBuilder().addProperty('testProp', 'test', true);
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'PREVIEW', logger)).to.be.true;
    });

    it('should return true if region field is configured to be visible', () => {
      let viewModel = ViewModelBuilder.createRegion('region', 'Region', 'EDITABLE', false, true);
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should return false if region field is configured to be hidden', () => {
      let viewModel = ViewModelBuilder.createRegion('region', 'Region', 'HIDDEN', false, false);
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.false;
    });

    it('should return true if field is configured to be visible and is valid', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', false, true, []);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', true)
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should return true if field is configured to be visible and is invalid', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', false, true, []);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', false)
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should return true if field is configured to be hidden and is invalid', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'HIDDEN', 'text', 'field 1', false, false, []);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', false)
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should return false if field is configured to be hidden and is valid', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'HIDDEN', 'text', 'field 1', false, false, []);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1', true)
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.false;
    });

    // In DTW for selected properties that are not present in given instance (validation model) a dummy view model is
    // added in definitionModel. We consider such properties as visible and valid.
    it('should return true if field is present in viewModel but is missing in validationModel', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'HIDDEN', 'text', 'field 1', false, false, []);
      let validationModel = new ValidationModelBuilder()
        .addProperty('field12', 'field12', true)
        .getModel();
      expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.true;
    });

    it('should resolve region`s visibility to be true if some of its fields are visible', () => {
      let viewModel = new ViewModelBuilder()
        .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false)
        .addField('field1', 'EDITABLE', 'text', 'field 1', false, true, [])
        .addField('field2', 'HIDDEN', 'text', 'field 2', false, false, [])
        .endRegion()
        .getModel();
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', 'field1')
        .addProperty('field2', 'field2')
        .getModel();
      let isVisible = FormWrapper.isVisibleField(viewModel.serialize().fields[0], validationModel, 'EDIT', logger);
      expect(isVisible).to.be.true;
    });

    // undefined keys tests starts
    // When missing or wrong parameters are provided, then the key that is built becomes invalid and isVisible becomes
    // undefined because there is no mapping for undefined keys
    describe('undefined keys tests', function () {
      it('should return false if viewModel and validationModel are provided but missing viewModel.previewEmpty property', () => {
        let viewModel = ViewModelBuilder.createField('field1', 'HIDDEN', 'text', 'field 1', false, false, []);
        viewModel.previewEmpty = undefined;
        let validationModel = new ValidationModelBuilder()
          .addProperty('field1', 'field1', true)
          .getModel();
        expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.false;
      });

      it('should return false if the field view model is missing displayType', () => {
        let viewModel = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', false, false, []);
        viewModel.previewEmpty = true;
        viewModel.displayType = undefined;
        let validationModel = new ValidationModelBuilder()
          .addProperty('field1', 'field1', true)
          .getModel();
        expect(FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger)).to.be.false;
      });
      // <- undefined keys built to here
    });

    it('should return false if viewModel and validationModel are provided viewModel.previewEmpty property, validationModel for requested field and displayType and formViewMode', () => {
      let viewModel = ViewModelBuilder.createField('field1', 'EDITABLE', 'text', 'field 1', false, false, []);
      viewModel.previewEmpty = false;
      let validationModel = new ValidationModelBuilder()
        .addProperty('field1', null, false)
        .getModel();
      let isVisible = FormWrapper.isVisibleField(viewModel, validationModel, 'EDIT', logger);
      expect(isVisible).to.be.true;
    });
  });

  describe('getVisibilityKey', function () {

    let testData = [
      // "FULL_FORCEPREVIEW_EDITABLE_EDIT": true,
      // "FULL_FORCEPREVIEW_HIDDEN_EDIT": false,
      // "FULL_FORCEPREVIEW_SYSTEM_EDIT": false,
      // "FULL_FORCEPREVIEW_READ_ONLY_EDIT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_EDITABLE_EDIT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_HIDDEN_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_SYSTEM_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_READ_ONLY_EDIT',
        'expectedVisibility': true
      },

      //"FULL_FORCEPREVIEW_EDITABLE_PREVIEW": true,
      //"FULL_FORCEPREVIEW_HIDDEN_PREVIEW": true,
      //"FULL_FORCEPREVIEW_SYSTEM_PREVIEW": false,
      //"FULL_FORCEPREVIEW_READ_ONLY_PREVIEW": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_EDITABLE_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_HIDDEN_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_SYSTEM_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_READ_ONLY_PREVIEW',
        'expectedVisibility': true
      },

      //"FULL_FORCEPREVIEW_EDITABLE_PRINT": true,
      //"FULL_FORCEPREVIEW_HIDDEN_PRINT": true,
      //"FULL_FORCEPREVIEW_SYSTEM_PRINT": false,
      //"FULL_FORCEPREVIEW_READ_ONLY_PRINT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_EDITABLE_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_HIDDEN_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_SYSTEM_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_FORCEPREVIEW_READ_ONLY_PRINT',
        'expectedVisibility': true
      },

      //"FULL_NOPREVIEW_EDITABLE_EDIT": true,
      //"FULL_NOPREVIEW_HIDDEN_EDIT": false,
      //"FULL_NOPREVIEW_SYSTEM_EDIT": false,
      //"FULL_NOPREVIEW_READ_ONLY_EDIT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_EDITABLE_EDIT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_HIDDEN_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_SYSTEM_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'EDIT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_READ_ONLY_EDIT',
        'expectedVisibility': true
      },

      //"FULL_NOPREVIEW_EDITABLE_PREVIEW": true,
      //"FULL_NOPREVIEW_HIDDEN_PREVIEW": true,
      //"FULL_NOPREVIEW_SYSTEM_PREVIEW": false,
      //"FULL_NOPREVIEW_READ_ONLY_PREVIEW": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_EDITABLE_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_HIDDEN_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_SYSTEM_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_READ_ONLY_PREVIEW',
        'expectedVisibility': true
      },

      //"FULL_NOPREVIEW_EDITABLE_PRINT": true,
      //"FULL_NOPREVIEW_HIDDEN_PRINT": true,
      //"FULL_NOPREVIEW_SYSTEM_PRINT": false,
      //"FULL_NOPREVIEW_READ_ONLY_PRINT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_EDITABLE_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_HIDDEN_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_SYSTEM_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {'value': 'title'}
        }, 'PRINT', false, {}],
        'expectedKey': 'FULL_NOPREVIEW_READ_ONLY_PRINT',
        'expectedVisibility': true
      },

      //  "EMPTY_FORCEPREVIEW_EDITABLE_EDIT": true,
      //  "EMPTY_FORCEPREVIEW_HIDDEN_EDIT": false,
      //  "EMPTY_FORCEPREVIEW_SYSTEM_EDIT": false,
      //  "EMPTY_FORCEPREVIEW_READ_ONLY_EDIT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_EDITABLE_EDIT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_HIDDEN_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_SYSTEM_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_READ_ONLY_EDIT',
        'expectedVisibility': true
      },

      //  "EMPTY_FORCEPREVIEW_EDITABLE_PREVIEW": true,
      //  "EMPTY_FORCEPREVIEW_HIDDEN_PREVIEW": true,
      //  "EMPTY_FORCEPREVIEW_SYSTEM_PREVIEW": false,
      //  "EMPTY_FORCEPREVIEW_READ_ONLY_PREVIEW": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_EDITABLE_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_HIDDEN_PREVIEW',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_SYSTEM_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_READ_ONLY_PREVIEW',
        'expectedVisibility': true
      },

      //"EMPTY_FORCEPREVIEW_EDITABLE_PRINT": true,
      //"EMPTY_FORCEPREVIEW_HIDDEN_PRINT": true,
      //"EMPTY_FORCEPREVIEW_SYSTEM_PRINT": false,
      //"EMPTY_FORCEPREVIEW_READ_ONLY_PRINT": true,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_EDITABLE_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_HIDDEN_PRINT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_SYSTEM_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': true,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_FORCEPREVIEW_READ_ONLY_PRINT',
        'expectedVisibility': true
      },

      //  "EMPTY_NOPREVIEW_EDITABLE_EDIT": true,
      //  "EMPTY_NOPREVIEW_HIDDEN_EDIT": false,
      //  "EMPTY_NOPREVIEW_SYSTEM_EDIT": false,
      //  "EMPTY_NOPREVIEW_READ_ONLY_EDIT": false,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_EDITABLE_EDIT',
        'expectedVisibility': true
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_HIDDEN_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_SYSTEM_EDIT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'EDIT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_READ_ONLY_EDIT',
        'expectedVisibility': true
      },

      //  "EMPTY_NOPREVIEW_EDITABLE_PREVIEW": false,
      //  "EMPTY_NOPREVIEW_HIDDEN_PREVIEW": false,
      //  "EMPTY_NOPREVIEW_SYSTEM_PREVIEW": false,
      //  "EMPTY_NOPREVIEW_READ_ONLY_PREVIEW": false
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_EDITABLE_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_HIDDEN_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_SYSTEM_PREVIEW',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'PREVIEW', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_READ_ONLY_PREVIEW',
        'expectedVisibility': false
      },

      //"EMPTY_NOPREVIEW_EDITABLE_PRINT": false,
      //"EMPTY_NOPREVIEW_HIDDEN_PRINT": false,
      //"EMPTY_NOPREVIEW_SYSTEM_PRINT": false,
      //"EMPTY_NOPREVIEW_READ_ONLY_PRINT": false,
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'EDITABLE'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_EDITABLE_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'HIDDEN'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_HIDDEN_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'SYSTEM'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_SYSTEM_PRINT',
        'expectedVisibility': false
      },
      {
        'args': [{
          'identifier': 'title',
          'previewEmpty': false,
          'displayType': 'READ_ONLY'
        }, {
          'title': {}
        }, 'PRINT', false, {}],
        'expectedKey': 'EMPTY_NOPREVIEW_READ_ONLY_PRINT',
        'expectedVisibility': false
      }
    ];

    it('should validate if visibility key is properly built according to field and form configuration', () => {
      for (let i = 0; i < testData.length; i++) {
        let data = testData[i];
        let key = FormWrapper.getVisibilityKey.apply(this, data.args);
        let isVisible = FormWrapper.isVisibleField.apply(this, data.args);
        expect(key, `Should be generated key '${data.expectedKey}' but '${key}' was found for configuration ${JSON.stringify(data.args)}!`).to.equal(data.expectedKey);
        expect(isVisible, `The field should be ${data.expectedVisibility ? 'visible' : 'hidden'} when configuration is ${JSON.stringify(data.args)}`).to.equal(data.expectedVisibility);
      }
    });
  });

  describe('#shouldWaitForFormLoaded', () => {
    it('should filter out properties fields correctly', () => {
      let fieldsMap = {
        'field1': {rendered: false},
        'field2': {rendered: false},
        'field3': {rendered: true}
      };
      expect(FormWrapper.shouldWaitForFormLoaded(fieldsMap)).to.be.true;
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe from eventbus event', () => {
      FormWrapper.prototype.formConfig.models = {
        viewModel: buildViewModel()
      };
      getBuilderInstance();
      formWrapper.ngOnInit();
      let stub = sinon.stub(formWrapper.afterFormValidationHandler, 'unsubscribe');
      formWrapper.formViewMode = FormWrapper.FORM_VIEW_MODE_PRINT;
      formWrapper.ngOnDestroy();
      expect(stub.calledOnce).to.be.true;
      stub.restore();
    });
  });

  function getBuilderInstance(_element, _logger, _compile, _scope, _pluginsService, _validationService, _eventbus, _configuration, _timeout) {
    let $scope = mock$scope();
    formWrapper = new FormWrapper(_element || IdocMocks.mockElement(), _logger || logger, _compile || $compile, $scope,
      _pluginsService || pluginsService, _validationService || validationService, _eventbus || eventbus, _configuration || configuration, _timeout || IdocMocks.mockTimeout());
    formWrapper.controls = controls;
  }

  function buildViewModel() {
    return new ViewModelBuilder()
      .addRegion('inputTextFields', 'Input text fields', 'EDITABLE', false, false)
      .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [])
      .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [])
      .addField('field3', 'READ_ONLY', 'text', 'field 3', false, false, [])
      .addField('field4', 'EDITABLE', 'text', 'field 4', false, false, [])
      .addField('field5', 'HIDDEN', 'text', 'field 5', false, false, [])
      .addField('field6', 'SYSTEM', 'text', 'field 6', false, false, [])
      .endRegion()
      .getModel();
  }

  function buildValidationModel() {
    return new ValidationModelBuilder()
      .addProperty('field1', 'field1')
      .addProperty('field2', 'field2')
      .addProperty('field3', 'field3')
      .addProperty('field4', 'field4')
      .addProperty('field5', 'field5')
      .addProperty('field6', 'field6')
      .getModel();
  }

  let controls = {
    "checkbox": {
      "name": "seip-checkbox",
      "component": "seip-checkbox",
      "type": "checkbox",
      "module": "form-builder/checkbox/checkbox"
    },
    "codelist": {
      "name": "seip-codelist",
      "component": "seip-codelist",
      "type": "codelist",
      "module": "form-builder/codelist/codelist"
    },
    "datetime": {
      "name": "seip-datetime",
      "component": "seip-datetime",
      "type": "datetime",
      "module": "form-builder/datetime/datetime"
    },
    "text": {
      "name": "seip-input-text",
      "component": "seip-input-text",
      "type": "text",
      "module": "form-builder/input-text/input-text"
    },
    "region": {
      "name": "seip-region",
      "component": "seip-region",
      "type": "region",
      "module": "form-builder/region/region"
    },
    "PICKLIST": {
      "name": "seip-resource",
      "component": "seip-resource",
      "type": "PICKLIST",
      "module": "form-builder/resource/resource"
    },
    "textarea": {
      "name": "seip-textarea",
      "component": "seip-textarea",
      "type": "textarea",
      "module": "form-builder/textarea/textarea"
    },
    "EMPTY_CELL": {
      "name": "seip-empty-cell",
      "component": "seip-empty-cell",
      "type": "EMPTY_CELL",
      "module": "form-builder/empty-cell/empty-cell"
    }
  };

});