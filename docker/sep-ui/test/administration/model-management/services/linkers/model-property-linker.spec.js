import {ModelPropertyLinker} from 'administration/model-management/services/linkers/model-property-linker';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';

const URI = ModelAttributeTypes.SINGLE_VALUE.MODEL_URI_TYPE;
const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;

describe('ModelPropertyLinker', () => {

  let modelField;
  let modelClass;
  let modelPropertyLinker;

  beforeEach(() => {
    modelField = new ModelField('type');
    modelClass = new ModelClass('clazz');

    modelPropertyLinker = new ModelPropertyLinker();
  });

  it('should link model field with a property associated with semantic URI', () => {
    modelField.addAttribute(new ModelSingleAttribute(URI).setType(URI));
    modelField.addAttribute(new ModelMultiAttribute(LABEL).setType(LABEL));
    modelField.getAttributeByType(URI).setValue(new ModelValue(null, 'rdf:type'));

    modelPropertyLinker.linkPropertiesToFieldModel(modelField, getProperties());
    expect(modelField.getProperty()).to.exist;
  });

  it('should not link model field with a property when no such property is present', () => {
    modelField.addAttribute(new ModelSingleAttribute(URI).setType(URI));
    modelField.addAttribute(new ModelMultiAttribute(LABEL).setType(LABEL));
    modelField.getAttributeByType(URI).setValue(new ModelValue(null, 'rdf:missing'));

    modelPropertyLinker.linkPropertiesToFieldModel(modelField, getProperties());
    expect(modelField.getProperty()).to.not.exist;
  });

  it('should link model class with related properties', () => {
    modelPropertyLinker.linkPropertiesToClassModel(modelClass, getProperties());

    expect(modelClass.hasProperties()).to.be.true;
    expect(modelClass.getProperties()).to.deep.eq([getRdfTypeProperty()]);
  });

  function getProperties() {
    let properties = new ModelList();
    properties.insert(getRdfTypeProperty());
    return properties;
  }

  function getRdfTypeProperty() {
    let property = new ModelProperty('rdf:type');
    let label = new ModelMultiAttribute(LABEL).setType(LABEL);
    label.addValue(new ModelValue('en', 'Type'));
    label.addValue(new ModelValue('bg', 'Тип'));
    label.setValue(label.getValueByLanguage('en'));
    property.addAttribute(label);
    property.setParent(modelClass);
    return property;
  }
});