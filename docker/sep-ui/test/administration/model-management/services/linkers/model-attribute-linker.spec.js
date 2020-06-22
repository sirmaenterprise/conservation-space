import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelValuesLinker} from 'administration/model-management/services/linkers/model-values-linker';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelList} from 'administration/model-management/model/model-list';

import {stub} from 'test/test-utils';

describe('ModelAttributeLinker', () => {

  let model;
  let modelValuesLinkerStub;
  let modelAttributesLinker;

  beforeEach(() => {
    model = new ModelDefinition('PR0001');
    modelValuesLinkerStub = stub(ModelValuesLinker);
    modelAttributesLinker = new ModelAttributeLinker(modelValuesLinkerStub);
  });

  it('should link provided attributes to a given model along with provided meta data', () => {
    modelAttributesLinker.linkAttributes(model, getAttributesResponse(), getMetaData());

    // should link four attributes to the model
    expect(model.getAttributes().length).to.eq(4);
    assertAttribute('title', 'label', ModelAttribute.SOURCE.MODEL_DATA, model);
    assertAttribute('creator', 'string', ModelAttribute.SOURCE.MODEL_DATA, model);
    assertAttribute('description', 'multiLangString', ModelAttribute.SOURCE.MODEL_DATA, model);
    assertAttribute('meta', 'multiLangString', ModelAttribute.SOURCE.META_DATA, model);
  });

  it('should order provided attributes according to their meta data order', () => {
    modelAttributesLinker.linkAttributes(model, getAttributesResponse(), getMetaData());

    // should order the provided attributes in a specific order based on the meta data
    expect(model.getAttributes()[0].getId()).to.eq('description');
    expect(model.getAttributes()[1].getId()).to.eq('creator');
    expect(model.getAttributes()[2].getId()).to.eq('title');
    expect(model.getAttributes()[3].getId()).to.eq('meta');
  });

  function assertAttribute(name, type, source, parent) {
    let attribute = parent.getAttribute(name);
    expect(attribute.getType()).to.eq(type);
    expect(attribute.getValidation()).to.exist;
    expect(attribute.getParent()).to.eq(parent);
    expect(attribute.getSource()).to.eq(source);
    expect(attribute.getMetaData().getId()).to.eq(name);
  }

  function getMetaData() {
    let meta = new ModelList();
    meta.insert(new ModelAttributeMetaData('title').setType('label').setOrder(30));
    meta.insert(new ModelAttributeMetaData('creator').setType('string').setOrder(20));
    meta.insert(new ModelAttributeMetaData('description').setType('multiLangString').setOrder(10));
    meta.insert(new ModelAttributeMetaData('meta').setType('multiLangString').setOrder(40));
    meta.getModels().forEach(meta => meta.getValidationModel().getRestrictions().setVisible(true));
    return meta;
  }

  function getAttributesResponse() {
    return [
      {
        'name': 'title',
        'type': 'label'
      },
      {
        'name': 'description',
        'type': 'multiLangString'
      },
      {
        'name': 'creator',
        'type': 'string'
      }
    ];
  }
});