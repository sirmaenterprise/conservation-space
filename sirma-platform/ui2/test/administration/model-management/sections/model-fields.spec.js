import {ModelFields} from 'administration/model-management/sections/field/model-fields';
import {ModelAttributeTypes, ModelSingleAttribute} from 'administration/model-management/model/model-attribute';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelValue, ModelDescription} from 'administration/model-management/model/model-value';
import {EventEmitter} from 'common/event-emitter';

import {stub} from 'test/test-utils';

const DISPLAY_TYPE = ModelAttributeTypes.SINGLE_VALUE.MODEL_DISPLAY_TYPE;

describe('ModelFields', () => {

  let model;
  let modelFields;

  beforeEach(() => {
    model = new ModelDefinition('definition');
    attachModels(model);

    modelFields = new ModelFields();
    modelFields.emitter = stub(EventEmitter);
    modelFields.model = model;
    modelFields.ngOnInit();
  });

  it('should provide default filter configurations', () => {
    expect(modelFields.config).to.deep.equal({
      filterTerm: '',
      showSystem: false,
      showHidden: false,
      showInherited: true
    });
  });

  it('should initialize filter rules from the configuration', () => {
    // should clone the provided configuration to the filter rules
    expect(modelFields.filterRules).to.deep.equal(modelFields.config);
  });

  it('should subscribe to model change or reload', () => {
    expect(modelFields.emitter.subscribe.calledOnce).to.be.true;
  });

  it('should properly filter models based on boolean filter rules', () => {
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = false;
    modelFields.triggerFilter();
    let region = model.getRegion('region');

    // system fields should be hidden from any region or on base level
    assertFilteredFields([model.getField('1'), region.getField('4')], false);

    // every other field should remain hidden in any region or base
    assertFilteredFields([model.getField('2'), region.getField('3'),
      region.getField('5'), region.getField('6')], true);
  });

  it('should properly filter models based on combined filter rules', () => {
    modelFields.filterRules.filterTerm = 'th';
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = true;
    modelFields.filterRules.showInherited = false;
    modelFields.triggerFilter();
    let region = model.getRegion('region');

    // fields should be hidden they do match keyword and are not inherited
    assertFilteredFields([region.getField('4'), region.getField('6')], true);

    // fields should be hidden since they do not match keyword or are inherited
    assertFilteredFields([model.getField('1'), model.getField('2'),
      region.getField('3'), region.getField('5')], false);
  });

  it('should properly filter models based on keyword filter rules', () => {
    modelFields.filterRules.filterTerm = 'th';
    modelFields.filterRules.showHidden = true;
    modelFields.filterRules.showSystem = true;
    modelFields.triggerFilter();
    let region = model.getRegion('region');

    // all fields should be hidden from the base level since they do not match filter keyword
    assertFilteredFields([model.getField('1'), model.getField('2')], false);

    // all fields should be shown from the region level since they do contain filter keyword
    assertFilteredFields([region.getField('3'), region.getField('4'),
      region.getField('5'), region.getField('6')], true);
  });

  it('should resolve if a field is inherited', () => {
    let field = new ModelField().setParent({});
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
    modelFields.filterRules.filterTerm = 'random';
    let field = createField('1', 'some random name');
    expect(modelFields.isFieldNameMatchingKeyword(field)).to.be.true;
  });

  function attachModels(model) {
    model.addField(createField('1', 'first', 'SYSTEM', 20, {}));
    model.addField(createField('2', 'second', 'HIDDEN', null, model));

    let region = createRegion('region', 30);
    region.addField(createField('3', 'third', 'READONLY', 40, {}));
    region.addField(createField('4', 'fourth', 'SYSTEM', 20, model));
    region.addField(createField('5', 'fifth', 'HIDDEN', 10, {}));
    region.addField(createField('6', 'sixth', 'READONLY', null, model));
    model.addRegion(region);
  }

  function createRegion(id, order) {
    let region = new ModelRegion(id);

    let ordering = new ModelSingleAttribute('order');
    ordering.setValue(new ModelValue().setValue(order));
    region.addAttribute(ordering);

    return region;
  }

  function createField(id, name, type, order, model) {
    let field = new ModelField(id);

    let display = new ModelSingleAttribute(DISPLAY_TYPE);
    display.setValue(new ModelValue().setValue(type));
    field.addAttribute(display);

    let ordering = new ModelSingleAttribute('order');
    ordering.setValue(new ModelValue().setValue(order));
    field.addAttribute(ordering);

    let description = new ModelDescription().setValue(name);
    field.setDescription(description);

    field.setParent(model);
    return field;
  }

  function assertFilteredFields(fields, visible) {
    fields.forEach(field => {
      let view = field.getView();
      expect(view.isVisible()).to.eq(visible);
    });
  }
});