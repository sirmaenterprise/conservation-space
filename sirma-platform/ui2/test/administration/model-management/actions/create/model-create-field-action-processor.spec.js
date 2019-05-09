import {ModelCreateFieldActionProcessor} from 'administration/model-management/actions/create/model-create-field-action-processor';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelCreateFieldAction} from 'administration/model-management/actions/create/model-create-field-action';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelValue} from 'administration/model-management/model/model-value';

import {stub} from 'test/test-utils';

describe('ModelCreateFieldActionProcessor', () => {

  let modelChangeSetBuilderStub;
  let modelCreateFieldActionProcessor;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelCreateFieldActionProcessor = new ModelCreateFieldActionProcessor(modelChangeSetBuilderStub);
  });

  it('should properly execute field creation action', () => {
    let model = getCreatedField();
    let context = getCreateContext();

    let action = new ModelCreateFieldAction().setModel(model).setContext(context);
    modelCreateFieldActionProcessor.execute(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getField(model.getId())).to.be.true;
  });

  it('should properly restore field creation action', () => {
    let model = getCreatedField();
    let context = getCreateContext();

    model.setParent(context);
    context.addField(model);

    let action = new ModelCreateFieldAction().setModel(model).setContext(context);
    modelCreateFieldActionProcessor.restore(action);

    expect(model.getParent()).to.eq(null);
    expect(!!context.getField(model.getId())).to.be.false;
  });

  it('should properly action create change set', () => {
    let model = getCreatedField();
    let context = getCreateContext();

    let emptyAttribute = new ModelSingleAttribute('empty', 'string',
      new ModelValue('en', '').setOldValue('')).setParent(model);

    let notEmptyAttribute = new ModelSingleAttribute('not-empty', 'string',
      new ModelValue('en', 'value').setOldValue('value')).setParent(model);

    let notEmptyDirtyAttribute = new ModelSingleAttribute('not-empty-dirty', 'string',
      new ModelValue('en', 'new-value').setOldValue('old-value')).setParent(model);

    model
      .addAttribute(emptyAttribute)
      .addAttribute(notEmptyAttribute)
      .addAttribute(notEmptyDirtyAttribute);

    // stub the change set builder to return first argument
    modelChangeSetBuilderStub.buildChangeSets.returnsArg(0);

    let action = new ModelCreateFieldAction().setModel(model).setContext(context);
    let changes = modelCreateFieldActionProcessor.changeset(action);

    // not dirty & not empty attributes are expected
    expect(changes).to.deep.eq([notEmptyAttribute]);
  });

  function getCreatedField() {
    return new ModelField('field');
  }

  function getCreateContext() {
    return new ModelDefinition('definition');
  }
});
