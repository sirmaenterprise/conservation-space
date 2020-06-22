import {ModelCreateControlActionProcessor} from 'administration/model-management/actions/create/model-create-control-action-processor';
import {ModelCreateControlAction} from 'administration/model-management/actions/create/model-create-control-action';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelControl} from 'administration/model-management/model/model-control';

import {stub} from 'test/test-utils';

describe('ModelCreateControlActionProcessor', () => {

  let modelChangeSetBuilderStub;
  let modelCreateControlActionProcessor;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelCreateControlActionProcessor = new ModelCreateControlActionProcessor(modelChangeSetBuilderStub);
  });

  it('should properly execute control creation action', () => {
    let definition = getDefinition();
    let context = getCreateContext();
    context.setParent(definition);
    let model = getCreatedControl();

    let action = new ModelCreateControlAction().setModel(model).setContext(context).setDefinition(definition);
    modelCreateControlActionProcessor.execute(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getControl(model.getId())).to.be.true;
  });

  it('should properly restore control creation action', () => {
    let definition = getDefinition();
    let context = getCreateContext();
    context.setParent(definition);
    let model = getCreatedControl();

    model.setParent(context);
    context.addControl(model);

    let action = new ModelCreateControlAction().setModel(model).setContext(context).setDefinition(definition);
    modelCreateControlActionProcessor.restore(action);

    expect(model.getParent()).to.eq(null);
    expect(!!context.getControl(model.getId())).to.be.false;
  });

  it('should properly construct changeset for target model', () => {
    let model = getCreatedControl();

    let attribute = new ModelSingleAttribute('DEFAULT_VALUE_PATTERN');
    model.addAttribute(attribute);

    let action = new ModelCreateControlAction().setModel(model);
    modelCreateControlActionProcessor.changeset(action);

    expect(modelChangeSetBuilderStub.buildChangeSets.calledOnce).to.be.true;
    expect(modelChangeSetBuilderStub.buildChangeSets.calledWith([attribute], ModelOperation.MODIFY)).to.be.true;
  });

  function getCreatedControl() {
    return new ModelControl('DEFAULT_VALUE_PATTERN');
  }

  function getCreateContext() {
    return new ModelField('field');
  }

  function getDefinition() {
    return new ModelDefinition('definition');
  }
});
