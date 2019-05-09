import {ModelCreateFieldActionFactory} from 'administration/model-management/actions/create/model-create-field-action-factory';
import {ModelCreateFieldAction} from 'administration/model-management/actions/create/model-create-field-action';
import {ModelFieldLinker} from 'administration/model-management/services/linkers/model-field-linker';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';

import {stub} from 'test/test-utils';

describe('ModelCreateFieldActionFactory', () => {

  let modelFieldLinkerStub;
  let modelCreateFieldActionFactory;

  beforeEach(() => {
    modelFieldLinkerStub = stub(ModelFieldLinker);
    modelFieldLinkerStub.createModelField.returns(getCreatedField());
    modelCreateFieldActionFactory = new ModelCreateFieldActionFactory(modelFieldLinkerStub);
  });

  it('should properly create field creation action', () => {
    let meta = {meta: 'meta'};
    let expected = new ModelCreateFieldAction().setMetaData(meta);
    expect(modelCreateFieldActionFactory.create(meta)).to.deep.eq(expected);
  });

  it('should properly evaluate field creation action', () => {
    let context = getCreateContext(), model = getCreatedField().setParent(context);
    let expected = new ModelCreateFieldAction().setModel(model).setContext(context);

    let target = new ModelCreateFieldAction().setContext(context);
    expect(modelCreateFieldActionFactory.evaluate(target)).to.deep.eq(expected);
  });

  function getCreatedField() {
    return new ModelField('field');
  }

  function getCreateContext() {
    return new ModelDefinition('definition');
  }
});
