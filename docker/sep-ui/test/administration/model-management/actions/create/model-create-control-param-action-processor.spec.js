import {ModelCreateControlParamActionProcessor} from 'administration/model-management/actions/create/model-create-control-param-action-processor';
import {ModelCreateControlParamAction} from 'administration/model-management/actions/create/model-create-control-param-action';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';

import {stub} from 'test/test-utils';

describe('ModelCreateControlParamActionProcessor', () => {

  let modelChangeSetBuilderStub;
  let modelCreateControlParamActionProcessor;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelCreateControlParamActionProcessor = new ModelCreateControlParamActionProcessor(modelChangeSetBuilderStub);
  });

  it('should properly execute control param creation action', () => {
    let model = getCreatedControlParam();
    let context = getCreateContext();

    let action = new ModelCreateControlParamAction().setModel(model).setContext(context);
    modelCreateControlParamActionProcessor.execute(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getControlParam(model.getId())).to.be.true;
  });


  it('should properly restore control param creation action', () => {
    let model = getCreatedControlParam();
    let context = getCreateContext();

    model.setParent(context);
    context.addControlParam(model);

    let action = new ModelCreateControlParamAction().setModel(model).setContext(context);
    modelCreateControlParamActionProcessor.restore(action);

    expect(model.getParent()).to.eq(null);
    expect(!!context.getControlParam(model.getId())).to.be.false;
  });

  it('should properly construct changeset for target model', () => {
    let model = getCreatedControlParam();

    let attribute = new ModelSingleAttribute('id');
    model.addAttribute(attribute);

    let action = new ModelCreateControlParamAction().setModel(model);
    modelCreateControlParamActionProcessor.changeset(action);

    expect(modelChangeSetBuilderStub.buildChangeSets.calledOnce).to.be.true;
    expect(modelChangeSetBuilderStub.buildChangeSets.calledWith([attribute], ModelOperation.MODIFY)).to.be.true;
  });

  function getCreatedControlParam() {
    return new ModelControlParam('template');
  }

  function getCreateContext() {
    return new ModelControl('DEFAULT_VALUE_PATTERN');
  }
});
