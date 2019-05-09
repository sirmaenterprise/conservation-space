import {ModelCreateControlParamActionFactory} from 'administration/model-management/actions/create/model-create-control-param-action-factory';
import {ModelCreateControlParamAction} from 'administration/model-management/actions/create/model-create-control-param-action';
import {ModelControlParamLinker} from 'administration/model-management/services/linkers/model-control-param-linker';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';

import {stub} from 'test/test-utils';

describe('ModelCreateControlParamActionFactory', () => {

  let modelControlParamLinker;
  let modelCreateControlParamActionFactory;

  beforeEach(() => {
    modelControlParamLinker = stub(ModelControlParamLinker);
    modelControlParamLinker.createModelControlParam.returns(new ModelControlParam('template'));
    modelCreateControlParamActionFactory = new ModelCreateControlParamActionFactory(modelControlParamLinker);
  });

  it('should properly create control param creation action', () => {
    let meta = {meta: 'meta'};
    let attributes = {attributes: 'attributes'};
    let expected = new ModelCreateControlParamAction().setMetaData(meta).setAttributes(attributes);
    expect(modelCreateControlParamActionFactory.create(meta, attributes)).to.deep.eq(expected);
  });

  it('should properly evaluate control param creation action', () => {
    let expected = new ModelCreateControlParamAction().setModel(getCreatedControlParam());
    expect(modelCreateControlParamActionFactory.evaluate(new ModelCreateControlParamAction())).to.deep.eq(expected);
  });

  function getCreatedControlParam() {
    return new ModelControlParam('template');
  }

});
