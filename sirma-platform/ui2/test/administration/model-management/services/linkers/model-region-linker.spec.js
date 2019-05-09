import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelRegionLinker} from 'administration/model-management/services/linkers/model-region-linker';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {stub} from 'test/test-utils';

describe('ModelRegionLinker', () => {

  let model;
  let modelRegionLinker;
  let modelAttributeLinkerStub;
  let modelDescriptionLinkerStub;

  beforeEach(() => {
    model = new ModelDefinition('PR0001');
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelDescriptionLinkerStub = stub(ModelDescriptionLinker);

    modelAttributeLinkerStub.linkAttributes.returns({});
    modelRegionLinker = new ModelRegionLinker(modelAttributeLinkerStub, modelDescriptionLinkerStub);
  });

  it('should link provided regions to a given model', () => {
    modelRegionLinker.linkRegions(model, getRegions(), {});

    // should link two regions to the model
    expect(model.getRegions().length).to.eq(2);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(2);
    expect(modelDescriptionLinkerStub.insertDescriptions.callCount).to.equal(2);

    assertRegion('generalDetails', model);
    assertRegion('specificDetails', model);
  });

  function assertRegion(name, parent) {
    let region = parent.getRegion(name);
    expect(region.getParent()).to.eq(parent);
  }

  function getRegions() {
    return [
      {
        id: 'generalDetails'
      }, {
        id: 'specificDetails'
      }
    ];
  }
});