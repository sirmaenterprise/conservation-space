import {ModelCreatePropertyActionFactory} from 'administration/model-management/actions/create/model-create-property-action-factory';
import {ModelCreatePropertyAction} from 'administration/model-management/actions/create/model-create-property-action';
import {ModelPropertyLinker} from 'administration/model-management/services/linkers/model-property-linker';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelClass} from 'administration/model-management/model/model-class';

import {stub} from 'test/test-utils';

describe('ModelCreatePropertyActionFactory', () => {

  let modelPropertyLinkerStub;
  let modelCreatePropertyActionFactory;

  beforeEach(() => {
    modelPropertyLinkerStub = stub(ModelPropertyLinker);
    modelPropertyLinkerStub.createModelProperty.returns(getCreatedProperty());
    modelCreatePropertyActionFactory = new ModelCreatePropertyActionFactory(modelPropertyLinkerStub);
  });

  it('should properly create property creation action', () => {
    let meta = {meta: 'meta'};
    let expected = new ModelCreatePropertyAction().setMetaData(meta);
    expect(modelCreatePropertyActionFactory.create(meta)).to.deep.eq(expected);
  });

  it('should properly evaluate property creation action', () => {
    let context = getCreateContext(), model = getCreatedProperty().setParent(context);
    let expected = new ModelCreatePropertyAction().setModel(model).setContext(context);

    let target = new ModelCreatePropertyAction().setContext(context);
    expect(modelCreatePropertyActionFactory.evaluate(target)).to.deep.eq(expected);
  });

  function getCreatedProperty() {
    return new ModelProperty('property');
  }

  function getCreateContext() {
    return new ModelClass('class');
  }
});
