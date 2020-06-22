import {ModelActionLinker} from 'administration/model-management/services/linkers/model-action-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {stub} from 'test/test-utils';

describe('ModelActionLinker', () => {
  let model;
  let modelActionLinker;
  let modelAttributeLinkerStub;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    model = new ModelDefinition('PR0001');
    modelAttributeLinkerStub = getModelAttributeStub();
    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);

    modelActionLinker = new ModelActionLinker(modelAttributeLinkerStub, modelDescriptionLinkerStub);
  });

  it('should link provided actions to a given model', () => {
    modelActionLinker.linkActions(model, getActions(), {});

    // should link two regions to the model
    expect(model.getActions().length).to.eq(2);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(2);
    expect(modelDescriptionLinkerStub.insertDescriptions.callCount).to.equal(2);

    assertAction('complete', model);
    assertAction('editDetails', model);
  });

  function assertAction(name, parent) {
    let action = parent.getAction(name);
    expect(action.getParent()).to.eq(parent);
    expect(action.getInherited()).to.be.false;
  }

  function getModelAttributeStub() {
    let modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelAttributeLinkerStub.linkAttributes.returns({});
    return modelAttributeLinkerStub;
  }

  function getActions() {
    return [
      {
        id: 'complete'
      }, {
        id: 'editDetails'
      }
    ];
  }
});