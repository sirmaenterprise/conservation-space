import {ModelRemoveControlActionProcessor} from 'administration/model-management/actions/remove/model-remove-control-action-processor';
import {ModelRemoveControlAction} from 'administration/model-management/actions/remove/model-remove-control-action';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelControl} from 'administration/model-management/model/model-control';

import {stub} from 'test/test-utils';

describe('ModelRemoveControlActionProcessor', () => {

  let modelChangeSetBuilderStub;
  let modelCreateControlActionProcessor;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelCreateControlActionProcessor = new ModelRemoveControlActionProcessor(modelChangeSetBuilderStub);
  });

  it('should properly execute control remove action', () => {
    let context = getCreateContext();
    let model = getCreatedControl();
    model.setParent(context);

    let action = new ModelRemoveControlAction().setModel(model).setContext(context);
    modelCreateControlActionProcessor.execute(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getControl(model.getId())).to.be.false;
  });


  it('should properly restore control remove action', () => {
    let context = getCreateContext();
    let model = getCreatedControl();
    context.addControl(model);

    let action = new ModelRemoveControlAction().setModel(model).setContext(context);
    modelCreateControlActionProcessor.restore(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getControl(model.getId())).to.be.true;
  });

  it('should properly construct changeset for target model', () => {
    let model = getCreatedControl();

    let action = new ModelRemoveControlAction().setModel(model);
    modelCreateControlActionProcessor.changeset(action);

    expect(modelChangeSetBuilderStub.buildChangeSet.calledOnce).to.be.true;
    expect(modelChangeSetBuilderStub.buildChangeSet.calledWith(action.getModel(), ModelOperation.REMOVE)).to.be.true;
  });

  function getCreatedControl() {
    return new ModelControl('DEFAULT_VALUE_PATTERN');
  }

  function getCreateContext() {
    return new ModelField('field');
  }

});
