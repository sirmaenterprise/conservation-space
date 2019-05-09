import {ModelHeaderLinker} from 'administration/model-management/services/linkers/model-header-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelHeaderMetaData} from 'administration/model-management/meta/model-header-meta';

import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';

import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('ModelHeaderLinker', () => {

  let model;
  let modelHeaderLinker;
  let modelAttributesLinkerStub;

  beforeEach(() => {
    model = new ModelDefinition('PR0001');
    modelAttributesLinkerStub = stub(ModelAttributeLinker);

    // Stub attributes so the linker could assign values
    modelAttributesLinkerStub.linkAttributes = (header, attr, meta) => {
      // Insert from meta
      meta.getModels().forEach(m => header.addAttribute(createAttribute(m.getId(), m.getType(), m.getDefaultValue(), m)));
      // Override with available
      attr.forEach(a => header.addAttribute(createAttribute(a.name, a.type, a.value, meta.getModel(a.name))));
    };

    modelHeaderLinker = new ModelHeaderLinker(modelAttributesLinkerStub);
  });

  it('should link provided headers to headers model', () => {
    modelHeaderLinker.linkHeaders(model, getHeaders(), getMetaData());
    expect(model.getHeaders().length).to.eq(4);

    // should insert all headers provided by the model data
    assertHeader('default_header', model, 'Default Header');
    assertHeader('compact_header', model, 'Compact Header');
    assertHeader('breadcrumb_header', model, 'Breadcrumb Header');

    // should insert missing headers based on the meta data
    assertHeader('tooltip_header', model, '');
    assertHeaderType('tooltip_header', model);
  });

  function assertHeader(name, parent, label) {
    let header = parent.getHeader(name);
    expect(header.getId()).to.equal(name);
    expect(header.getParent()).to.equal(parent);
    expect(header.getLabelAttribute().getValue().getValue()).to.eq(label);
  }

  function assertHeaderType(name, parent) {
    let header = parent.getHeader(name);
    let headerTypeAttribute = header.getAttribute(ModelAttribute.HEADER_TYPE_ATTRIBUTE);
    expect(headerTypeAttribute.getValue().getValue()).to.equal(name);
  }

  function createAttribute(name, type, value, meta) {
    let attribute = null;

    if (_.isObject(value)) {
      let languages = Object.keys(value);
      attribute = new ModelMultiAttribute(name, type);
      languages.forEach(key => attribute.addValue(new ModelValue(key, value[key])));
      languages.length && attribute.setValue(attribute.getValueByLanguage('en'));
    } else {
      attribute = new ModelSingleAttribute(name);
      let modelValue = new ModelValue().setValue(value);
      attribute.setType(type).setValue(modelValue);
    }
    attribute.setMetaData(meta);
    return attribute;
  }

  function getHeaders() {
    return [
      getHeader('default_header', {bg: 'Основно заглавие', en: 'Default Header'}),
      getHeader('compact_header', {bg: 'Съкратено заглавие', en: 'Compact Header'}),
      getHeader('breadcrumb_header', {bg: 'Кратко заглавие', en: 'Breadcrumb Header'})
    ];
  }

  function getHeader(id, labels) {
    return {
      id,
      attributes: [
        getAttribute(ModelAttribute.LABEL_ATTRIBUTE, ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE, labels),
        getAttribute(ModelAttribute.HEADER_TYPE_ATTRIBUTE, ModelAttributeTypes.SINGLE_VALUE.MODEL_OPTION_TYPE, id)
      ]
    };
  }

  function getAttribute(name, type, value) {
    return {
      name, type, value
    };
  }

  function getMetaData() {
    let meta = new ModelList();

    meta.insert(
      createModelHeaderMetaData(
        ModelAttribute.LABEL_ATTRIBUTE,
        ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE,
        0, {bg: '', en: ''}
      ), model => model.id);

    meta.insert(
      createModelHeaderMetaData(
        ModelAttribute.HEADER_TYPE_ATTRIBUTE,
        ModelAttributeTypes.SINGLE_VALUE.MODEL_OPTION_TYPE,
        1, '',
        [
          getOption('default_header'),
          getOption('compact_header'),
          getOption('breadcrumb_header'),
          getOption('tooltip_header')
        ]
      ), model => model.id);

    return meta;
  }

  function getOption(value) {
    return {value};
  }

  function createModelHeaderMetaData(id, type, order, defaultValue, options) {
    return new ModelHeaderMetaData(id)
      .setType(type)
      .setOrder(order)
      .setDefaultValue(defaultValue)
      .setOptions(options || []);
  }
});
