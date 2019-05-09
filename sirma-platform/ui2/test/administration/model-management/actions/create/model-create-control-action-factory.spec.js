import {ModelCreateControlActionFactory} from 'administration/model-management/actions/create/model-create-control-action-factory';
import {ModelCreateControlAction} from 'administration/model-management/actions/create/model-create-control-action';
import {ModelControlLinker} from 'administration/model-management/services/linkers/model-control-linker';
import {ModelControl} from 'administration/model-management/model/model-control';

import {stub} from 'test/test-utils';

describe('ModelCreateControlActionFactory', () => {

  let modelControlLinkerStub;
  let modelCreateControlActionFactory;

  beforeEach(() => {
    modelControlLinkerStub = stub(ModelControlLinker);
    modelControlLinkerStub.createModelControl.returns(new ModelControl('DEFAULT_VALUE_PATTERN'));
    modelCreateControlActionFactory = new ModelCreateControlActionFactory(modelControlLinkerStub);
  });

  it('should properly create control creation action', () => {
    let meta = {meta: 'meta'};
    let definition = {definition: 'definition'};
    let expected = new ModelCreateControlAction().setMetaData(meta).setId('DEFAULT_VALUE_PATTERN').setDefinition(definition);
    expect(modelCreateControlActionFactory.create(meta, 'DEFAULT_VALUE_PATTERN', definition)).to.deep.eq(expected);
  });

  it('should properly evaluate control creation action', () => {
    let expected = new ModelCreateControlAction().setModel(getCreatedControl());
    expect(modelCreateControlActionFactory.evaluate(new ModelCreateControlAction())).to.deep.eq(expected);
  });

  function getCreatedControl() {
    return new ModelControl('DEFAULT_VALUE_PATTERN');
  }

});
