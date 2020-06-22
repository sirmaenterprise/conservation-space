import {ModelCreatePropertyActionProcessor} from 'administration/model-management/actions/create/model-create-property-action-processor';
import {ModelCreatePropertyAction} from 'administration/model-management/actions/create/model-create-property-action';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelValue} from 'administration/model-management/model/model-value';

import {stub} from 'test/test-utils';

describe('ModelCreatePropertyActionProcessor', () => {

  let modelChangeSetBuilderStub;
  let modelCreatePropertyActionProcessor;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelCreatePropertyActionProcessor = new ModelCreatePropertyActionProcessor(modelChangeSetBuilderStub);
  });

  it('should properly execute property creation action', () => {
    let model = getCreatedProperty();
    let context = getCreateContext();

    let action = new ModelCreatePropertyAction().setModel(model).setContext(context);
    modelCreatePropertyActionProcessor.execute(action);

    expect(model.getParent()).to.eq(context);
    expect(!!context.getProperty(model.getId())).to.be.true;
  });

  it('should properly restore property creation action', () => {
    let model = getCreatedProperty();
    let context = getCreateContext();

    model.setParent(context);
    context.addProperty(model);

    let action = new ModelCreatePropertyAction().setModel(model).setContext(context);
    modelCreatePropertyActionProcessor.restore(action);

    expect(model.getParent()).to.eq(null);
    expect(!!context.getProperty(model.getId())).to.be.false;
  });

  it('should properly action create change set', () => {
    let model = getCreatedProperty();
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

    let action = new ModelCreatePropertyAction().setModel(model).setContext(context);
    let changes = modelCreatePropertyActionProcessor.changeset(action);

    // not dirty & not empty attributes are expected
    expect(changes).to.deep.eq([notEmptyAttribute]);
  });

  function getCreatedProperty() {
    return new ModelProperty('property');
  }

  function getCreateContext() {
    return new ModelClass('class');
  }
});
