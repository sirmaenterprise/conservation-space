import {ModelFields} from 'administration/model-management/sections/field/model-fields';

import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';

import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {ModelValue, ModelDescription} from 'administration/model-management/model/model-value';

import {EventEmitter} from 'common/event-emitter';
import {ModelEvents} from 'administration/model-management/model/model-events';

import {stub} from 'test/test-utils';

const CLASS = 'class';
const DEFINITION = 'definition';

const ORDER_ATTRIBUTE = ModelAttribute.ORDER_ATTRIBUTE;
const DISPLAY_ATTRIBUTE = ModelAttribute.DISPLAY_ATTRIBUTE;
const MODEL_LABEL_TYPE = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;

describe('ModelFields', () => {

  let model;
  let modelFields;

  function createFieldsSection(modelId = DEFINITION, ngOnInit = false) {
    if (modelId === DEFINITION) {
      model = new ModelDefinition(modelId);
      attachModelsToDefinition(model);
    } else if (modelId = CLASS) {
      model = new ModelClass(modelId);
      attachModelsToClass(model);
    }
    modelFields = new ModelFields();
    modelFields.emitter = stub(EventEmitter);
    modelFields.model = model;
    ngOnInit && modelFields.ngOnInit();
  }

  it('should provide default filter configurations', () => {
    createFieldsSection();
    expect(modelFields.config).to.deep.equal({
      filterTerm: '',
      showSystem: false,
      showHidden: false,
      showInherited: true
    });
  });

  it('should subscribe to model changed event', () => {
    createFieldsSection();

    modelFields.subscribeToModelChanged();
    expect(modelFields.emitter.subscribe.calledOnce).to.be.true;
    expect(modelFields.emitter.subscribe.calledWith(ModelEvents.MODEL_CHANGED_EVENT)).to.be.true;
  });

  it('should initialize and prepare component properly', () => {
    createFieldsSection();

    // emulate arbitrary previously selected model
    let selectedModel = new ModelField('selected');

    // spy methods that are going to be called
    modelFields.selectedModel = selectedModel;
    modelFields.triggerFilter = sinon.spy();
    modelFields.afterModelChange = sinon.spy();

    modelFields.initialize(model);

    // should prepare section based on model
    expect(modelFields.model).to.eq(model);

    // should remove selected item and init filter
    expect(modelFields.selectedModel).to.not.exist;
    expect(modelFields.config.showInherited).to.be.true;
    expect(modelFields.filterRules).to.deep.equal(modelFields.config);

    // should trigger filtering and call after model change
    expect(modelFields.triggerFilter.calledOnce).to.be.true;

    // should deselect any previously selected models
    expect(modelFields.selectedModel).to.not.exist;
  });

  it('should properly filter definition models based on boolean filter rules', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = false;
    modelFields.triggerFilter();

    // system fields should be hidden from any region or on base level
    assertFilteredFields([model.getField('1'), model.getField('4')], false);

    // every other field should remain hidden in any region or base
    assertFilteredFields([
      model.getField('2'), model.getField('3'),
      model.getField('5'), model.getField('6')
    ], true);
  });

  it('should properly filter definition models based on combined filter rules', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.filterRules.filterTerm = 'th';
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = true;
    modelFields.filterRules.showInherited = false;
    modelFields.triggerFilter();

    // fields should be hidden they do match keyword and are not inherited
    assertFilteredFields([model.getField('4'), model.getField('6')], true);

    // fields should be hidden since they do not match keyword or are inherited
    assertFilteredFields([
      model.getField('1'), model.getField('2'),
      model.getField('3'), model.getField('5')
    ], false);
  });

  it('should properly filter definition models based on keyword filter rules', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.filterRules.filterTerm = 'th';
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = true;
    modelFields.triggerFilter();

    // all fields should be hidden from the base level since they do not match filter keyword
    assertFilteredFields([model.getField('1'), model.getField('2')], false);

    // all fields should be shown from the region level since they do contain filter keyword
    assertFilteredFields([
      model.getField('3'), model.getField('4'),
      model.getField('5'), model.getField('6')
    ], true);
  });

  it('should properly filter class models based on keyword filter rules', () => {
    createFieldsSection(CLASS, true);
    modelFields.filterRules.filterTerm = 'th';
    modelFields.filterRules.showInherited = true;
    modelFields.triggerFilter();

    // all properties which do not match the keyword should be hidden
    assertFilteredFields([model.getProperty('1'), model.getProperty('2')], false);

    // all properties which match the keyword should be shown
    assertFilteredFields([model.getProperty('3')], true);
  });

  it('should resolve if a field is inherited', () => {
    let field = new ModelField().setParent(new ModelDefinition());
    expect(modelFields.isFieldInherited(field)).to.be.true;

    field.setParent(model);
    expect(modelFields.isFieldInherited(field)).to.be.false;
  });

  it('should resolve if a field is hidden', () => {
    let field = createField('1', '1', 'HIDDEN');
    expect(modelFields.isFieldHidden(field)).to.be.true;
  });

  it('should resolve if a field is system', () => {
    let field = createField('1', '1', 'SYSTEM');
    expect(modelFields.isFieldSystem(field)).to.be.true;
  });

  it('should resolve if a keyword matches field description and be case insensitive', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.filterRules.filterTerm = 'random';
    let field = createField('1', 'some random name');
    expect(modelFields.isFieldNameMatchingKeyword(field)).to.be.true;
  });

  it('should use provided model as selected and highlight it', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.notifyForModelStateCalculation = sinon.spy();

    let field = createField('1', 'Selected field');
    modelFields.selectModel(field);

    expect(modelFields.selectedModel).to.equal(field);
  });

  it('should remove highlight from any previously selected model', () => {
    createFieldsSection(DEFINITION, true);
    modelFields.notifyForModelStateCalculation = sinon.spy();

    let previousField = createField('1', 'Previous field');
    modelFields.selectModel(previousField);

    let field = createField('1', 'Selected field');
    modelFields.selectModel(field);

    expect(modelFields.selectedModel).to.equal(field);
  });

  function attachModelsToDefinition(model) {
    let parent = new ModelDefinition('parent');

    let regions = [
      createRegion('region', 30, model)
    ];

    let fields = [
      createField('1', 'first', 'SYSTEM', 20, parent).setRegionId(null),
      createField('2', 'second', 'HIDDEN', null, model).setRegionId(null),
      createField('3', 'third', 'READONLY', 40, parent).setRegionId('region'),
      createField('4', 'fourth', 'SYSTEM', 20, model).setRegionId('region'),
      createField('5', 'fifth', 'HIDDEN', 10, parent).setRegionId('region'),
      createField('6', 'sixth', 'READONLY', null, model).setRegionId('region')
    ];

    // attach created regions & field models
    fields.forEach(f => model.addField(f));
    regions.forEach(r => model.addRegion(r));

    // attach arbitrary properties to some of the fields in the model
    model.getField('1').setProperty(createProperty('1'));
    model.getField('6').setProperty(createProperty('6'));

    return model;
  }

  function attachModelsToClass(model) {
    let parent = new ModelClass('parent');
    // attach owning and inherited properties to the class
    model.addProperty(createProperty('1', 'first', model));
    model.addProperty(createProperty('2', 'second', parent));
    model.addProperty(createProperty('3', 'third', model));

    return model;
  }

  function createRegion(id, order, model) {
    let region = new ModelRegion(id);

    let ordering = new ModelSingleAttribute('order');
    ordering.setValue(new ModelValue('en', order));
    region.addAttribute(ordering);

    region.setParent(model);
    return region;
  }

  function createField(id, name, type, order, model) {
    let field = new ModelField(id);

    let ordering = new ModelSingleAttribute(ORDER_ATTRIBUTE);
    ordering.setValue(new ModelValue('en', order));
    field.addAttribute(ordering);

    let display = new ModelSingleAttribute(DISPLAY_ATTRIBUTE);
    display.setValue(new ModelValue('en', type));
    field.addAttribute(display);

    let label = new ModelMultiAttribute('label').setType(MODEL_LABEL_TYPE);
    label.addValue(new ModelValue('en', name));
    label.setValue(label.getValueByLanguage('en'));
    field.addAttribute(label);

    let description = new ModelDescription().setValue(name);
    field.setDescription(description);

    field.setParent(model);
    return field;
  }

  function createProperty(id, name, model) {
    let property = new ModelProperty(id);
    let description = new ModelDescription().setValue(name);

    property.setDescription(description);
    property.setParent(model);
    return property;
  }

  function assertFilteredFields(fields, visible) {
    fields.forEach(field => expect(modelFields.visible[field.getId()]).to.be[visible]);
  }
});