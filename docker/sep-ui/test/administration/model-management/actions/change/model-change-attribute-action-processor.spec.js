import {ModelChangeAttributeActionProcessor} from 'administration/model-management/actions/change/model-change-attribute-action-processor';
import {ModelChangeAttributeAction} from 'administration/model-management/actions/change/model-change-attribute-action';

import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelPath} from 'administration/model-management/model/model-path';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelChangeSet} from 'administration/model-management/model/model-changeset';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

import {ModelManagementCopyService} from 'administration/model-management/services/model-management-copy-service';

import {stub} from 'test/test-utils';

describe('ModelChangeAttributeActionProcessor', () => {

  let modelChangeAttributeActionProcessor;
  let modelManagementCopyServiceStub;

  let modelPathBuilderStub;
  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelPathBuilderStub = stub(ModelPathBuilder);
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);

    modelManagementCopyServiceStub = stub(ModelManagementCopyService);

    modelPathBuilderStub.buildPathFromModel.returns(new ModelPath());
    modelChangeSetBuilderStub.buildChangeSet.returns(new ModelChangeSet());
    modelManagementCopyServiceStub.copyFromPath.returns(new ModelSingleAttribute());
    modelManagementCopyServiceStub.restoreFromPath.returns(new ModelSingleAttribute());

    modelChangeAttributeActionProcessor = new ModelChangeAttributeActionProcessor(modelManagementCopyServiceStub,
      modelChangeSetBuilderStub, modelPathBuilderStub);
  });

  it('should be able to override inherited attribute when the owner differs from destination', () => {
    let owner = new ModelDefinition('owner');
    let context = new ModelDefinition('destination');

    let attribute = new ModelSingleAttribute('attribute').setParent(owner);
    attribute.setValue(new ModelValue('EN', 'new-value').setOldValue('old-value'));

    modelManagementCopyServiceStub.copyFromPath.returns(new ModelSingleAttribute().setParent(owner));
    let result = modelChangeAttributeActionProcessor.execute(new ModelChangeAttributeAction()
      .setModel(attribute).setContext(context).setOwningContext(context));

    // should create a new model attribute when the owner differs from the destination model
    expect(result).to.not.equal(attribute);
  });

  it('should be able to restore inherited attribute when the owner differs from destination', () => {
    let owner = new ModelDefinition('owner');
    let context = new ModelDefinition('destination');

    let attribute = new ModelSingleAttribute('attribute').setParent(owner);
    attribute.setValue(new ModelValue('EN', 'new-value').setOldValue('old-value'));

    modelManagementCopyServiceStub.restoreFromPath.returns(new ModelSingleAttribute().setParent(owner));
    let result = modelChangeAttributeActionProcessor.restore(new ModelChangeAttributeAction()
      .setModel(attribute).setContext(context).setOwningContext(context).setInherited(true));

    // should restore the model attribute
    expect(result).to.not.equal(attribute);
  });

  it('should be able to restore non inherited attribute when the owner differs from destination', () => {
    let owner = new ModelDefinition('owner');
    let attribute = new ModelSingleAttribute('attribute').setParent(owner);
    attribute.setValue(new ModelValue('EN', 'new-value').setOldValue('old-value'));

    let result = modelChangeAttributeActionProcessor.restore(new ModelChangeAttributeAction()
      .setModel(attribute).setContext(new ModelDefinition('destination')).setInherited(false));

    expect(result).to.equal(attribute);
    // should restore only the value back to the original value
    expect(attribute.getValue().getValue()).to.equal('old-value');
  });

  it('should be able to build change set for the given action', () => {
    let attribute = new ModelSingleAttribute('attribute');
    attribute.setValue(new ModelValue('EN', 'new-value').setOldValue('old-value'));

    let result = modelChangeAttributeActionProcessor.changeset(new ModelChangeAttributeAction()
      .setModel(attribute).setContext(new ModelDefinition('destination')));

    expect(result).to.deep.eq(new ModelChangeSet());
    expect(modelChangeSetBuilderStub.buildChangeSet.calledOnce).to.be.true;
    expect(modelChangeSetBuilderStub.buildChangeSet.calledWith(attribute)).to.be.true;
  });
});
