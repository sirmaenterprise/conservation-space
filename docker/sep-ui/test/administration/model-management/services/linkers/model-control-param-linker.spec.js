import {ModelControlParamLinker} from 'administration/model-management/services/linkers/model-control-param-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {stub} from 'test/test-utils';

describe('ModelControlParamLinker', () => {

  let model;
  let modelControlParamLinker;
  let modelAttributeLinkerStub;

  beforeEach(() => {
    model = new ModelControl('PICKER');
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelControlParamLinker = new ModelControlParamLinker(modelAttributeLinkerStub);
  });

  it('should link provided controls to a given field model', () => {
    let meta = new ModelsMetaData();
    modelControlParamLinker.linkControlParams(model, getControlParams(), meta);

    expect(model.getControlParams().length).to.eq(2);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(2);

    expect(model.getControlParam('range').getParent()).to.eq(model);
    expect(model.getControlParam('restrictions').getParent()).to.eq(model);
  });

  function getControlParams() {
    return [
      {
        id: 'range'
      },
      {
        id: 'restrictions'
      }
    ];
  }
});