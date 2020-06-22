import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';

import {ModelPath} from 'administration/model-management/model/model-path';
import {ModelChangeSet} from 'administration/model-management/model/model-changeset';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelValue} from 'administration/model-management/model/model-value';

import {stub} from 'test/test-utils';

describe('ModelChangeSetBuilder', () => {

  let modelChangeSetBuilder;
  let modelPathBuilderStub;

  beforeEach(() => {
    modelPathBuilderStub = stub(ModelPathBuilder);
    modelPathBuilderStub.buildPathFromModel.returns(getModelPath());
    modelPathBuilderStub.buildStringFromPath.returns(getStringPath());
    modelChangeSetBuilder = new ModelChangeSetBuilder(modelPathBuilderStub);
  });

  it('should build proper change set based on single valued attribute', () => {
    let attribute = new ModelSingleAttribute('abstract');
    let definition = new ModelDefinition('PR0001');

    attribute.setParent(definition);
    definition.addAttribute(attribute);
    attribute.setValue(new ModelValue().setValue('actual-value').setOldValue('old-value'));

    // should build a single change set for the single valued attribute provided as parameter
    expect(modelChangeSetBuilder.buildChangeSet(attribute, 'operation')).to.deep.eq(
      getChangeSet(attribute, 'definition=PR0001/attribute=abstract',
        'actual-value', 'old-value', 'operation'));
  });

  it('should build proper change set based on multi valued attribute', () => {
    let attribute = new ModelMultiAttribute('abstract').setType(ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE);
    let definition = new ModelDefinition('PR0001');

    attribute.setParent(definition);
    definition.addAttribute(attribute);
    attribute.addValue(new ModelValue('en').setValue('new-value-1').setOldValue('old-value-1'));
    attribute.addValue(new ModelValue('bg').setValue('new-value-2').setOldValue('old-value-2'));
    attribute.addValue(new ModelValue('de').setValue('old-value-3').setOldValue('old-value-3'));

    // should build a single change set for the single valued attribute provided as parameter
    expect(modelChangeSetBuilder.buildChangeSet(attribute, 'operation')).to.deep.eq(
      getChangeSet(attribute, 'definition=PR0001/attribute=abstract',
        {'en': 'new-value-1', 'bg': 'new-value-2', 'de': 'old-value-3'},
        {'en': 'old-value-1', 'bg': 'old-value-2', 'de': 'old-value-3'},
        'operation')
    );
  });

  function getChangeSet(model, path, newValue, oldValue, operation) {
    let change = new ModelChangeSet();
    change.setSelector(path)
          .setNewValue(newValue)
          .setOldValue(oldValue)
          .setOperation(operation)
          .setModel(model);
    return change;
  }

  function getModelPath() {
    return new ModelPath('definition', 'PR0001').setNext(new ModelPath('attribute', 'abstract'));
  }

  function getStringPath() {
    return 'definition=PR0001/attribute=abstract';
  }
});