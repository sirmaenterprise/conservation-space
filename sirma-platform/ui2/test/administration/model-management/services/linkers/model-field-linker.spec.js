import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelControlLinker} from 'administration/model-management/services/linkers/model-control-linker';
import {ModelFieldLinker} from 'administration/model-management/services/linkers/model-field-linker';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {stub} from 'test/test-utils';

describe('ModelFieldLinker', () => {

  let model;
  let modelFieldLinker;
  let modelControlLinkerStub;
  let modelAttributeLinkerStub;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    model = new ModelDefinition('PR0001');
    modelControlLinkerStub = stub(ModelControlLinker);
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);
    modelFieldLinker = new ModelFieldLinker(modelAttributeLinkerStub, modelDescriptionLinkerStub, modelControlLinkerStub);
  });

  it('should link provided fields to a given model', () => {
    let meta = new ModelsMetaData();
    modelFieldLinker.linkFields(model, getFields(), meta);

    // should link three fields to the model
    expect(model.getFields().length).to.eq(3);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(3);
    expect(modelDescriptionLinkerStub.insertDescriptions.callCount).to.equal(3);
    expect(modelControlLinkerStub.linkControls.callCount).to.equal(3);

    assertField('title', model);
    assertField('description', model);
    assertField('emailAddress', model);
  });

  function assertField(name, parent) {
    let field = parent.getField(name);
    expect(field.getParent()).to.eq(parent);
  }

  function getFields() {
    return [
      {
        id: 'title',
        parent: null,
        regionId: null,
        controls: [
          { id: 'DEFAULT_VALUE_PATTERN' }
        ]
      }, {
        id: 'description',
        parent: null,
        regionId: null,
        controls: [
          { id: 'RICHTEXT' }
        ]
      }, {
        id: 'emailAddress',
        parent: null,
        regionId: null,
        controls: [
          { id: 'DEFAULT_VALUE_PATTERN' }
        ]
      }
    ];
  }
});