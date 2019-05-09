import {ModelControlParamLinker} from 'administration/model-management/services/linkers/model-control-param-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelControlLinker} from 'administration/model-management/services/linkers/model-control-linker';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {ModelControlMetaData} from 'administration/model-management/meta/model-control-meta';
import {stub} from 'test/test-utils';

describe('ModelControlLinker', () => {

  let model;
  let modelControlLinker;
  let modelAttributeLinkerStub;
  let modelControlParamLinkerStub;

  beforeEach(() => {
    model = new ModelField('title');
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelControlParamLinkerStub = stub(ModelControlParamLinker);
    modelControlLinker = new ModelControlLinker(modelControlParamLinkerStub, modelAttributeLinkerStub);
  });

  it('should link provided controls to a given field model', () => {
    let meta = new ModelsMetaData();
    meta.addControl(new ModelControlMetaData('id'));
    modelControlLinker.linkControls(model, getControls(), meta);

    expect(model.getControls().length).to.eq(1);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(1);
    expect(modelControlParamLinkerStub.linkControlParams.callCount).to.equal(1);
    expect(model.getControl('DEFAULT_VALUE_PATTERN').getParent()).to.eq(model);
  });

  function getControls() {
    return [
      {
        id: 'DEFAULT_VALUE_PATTERN'
      }
    ];
  }
});