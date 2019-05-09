import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {ModelActionExecutionLinker} from 'administration/model-management/services/linkers/model-action-execution-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';

import {stub} from 'test/test-utils';

describe('ModelActionExecutionLinker', () => {
  let model;
  let modelActionExecutionLinker;
  let modelAttributeLinkerStub;

  beforeEach(() => {
    model = getModel();
    modelAttributeLinkerStub = getModelAttributeStub();

    modelActionExecutionLinker = new ModelActionExecutionLinker(modelAttributeLinkerStub);
  });

  it('should link provided actionExecutions to a given model', () => {
    modelActionExecutionLinker.linkActionExecutions(model, getActions(), {});

    // should link two regions to the model
    let action = model.getAction('complete');
    expect(action.getActionExecutions().length).to.eq(1);
    expect(modelAttributeLinkerStub.linkAttributes.callCount).to.equal(1);

    assertActionExecution('execute', action);
  });

  function getModel() {
    let model = new ModelDefinition('PR0001');
    let action = new ModelAction('complete');
    model.addAction(action);
    return model;
  }

  function assertActionExecution(name, parent) {
    let actionExecution = parent.getActionExecution(name);
    expect(actionExecution.getParent()).to.eq(parent);
  }

  function getModelAttributeStub() {
    let modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelAttributeLinkerStub.linkAttributes.returns({});
    return modelAttributeLinkerStub;
  }

  function getActions() {
    return [
      {
        id: 'complete',
        actionExecutions: [{id: 'execute'}]
      }, {
        id: 'editDetails'
      }
    ];
  }
});