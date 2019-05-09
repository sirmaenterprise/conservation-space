import {ModelActionProcessor} from 'administration/model-management/actions/model-action-processor';
import {PluginsService} from 'services/plugin/plugins-service';

import {ModelRestoreInheritedAttributeAction} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action';
import {ModelChangeAttributeAction} from 'administration/model-management/actions/change/model-change-attribute-action';

import {ModelRestoreInheritedAttributeActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action-processor';
import {ModelChangeAttributeActionProcessor} from 'administration/model-management/actions/change/model-change-attribute-action-processor';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelActionProcessor', () => {

  let actions;
  let modelActionProcessor;

  let modelRestoreInheritedAttributeActionProcessorStub;
  let modelChangeAttributeActionProcessorStub;
  let pluginsServiceStub;

  beforeEach(() => {
    let changed = new ModelChangeAttributeAction();
    let inherited = new ModelRestoreInheritedAttributeAction();
    actions = [changed, inherited];

    modelRestoreInheritedAttributeActionProcessorStub = stub(ModelRestoreInheritedAttributeActionProcessor);
    modelChangeAttributeActionProcessorStub = stub(ModelChangeAttributeActionProcessor);

    modelRestoreInheritedAttributeActionProcessorStub.execute.returns('execute-result');
    modelRestoreInheritedAttributeActionProcessorStub.restore.returns('restore-result');
    modelRestoreInheritedAttributeActionProcessorStub.changeset.returns('changeset-result');

    modelChangeAttributeActionProcessorStub.execute.returns(['execute-result1', 'execute-result2']);
    modelChangeAttributeActionProcessorStub.restore.returns(['restore-result1', 'restore-result2']);
    modelChangeAttributeActionProcessorStub.changeset.returns(['changeset-result1', 'changeset-result2']);

    pluginsServiceStub = stub(PluginsService);
    pluginsServiceStub.getPluginDefinitions.returns(getActionMapping());
    pluginsServiceStub.loadPluginServiceModules.returns(PromiseStub.resolve(getProcessorMapping()));

    modelActionProcessor = new ModelActionProcessor(pluginsServiceStub);
  });

  it('should properly call and execute actions', () => {
    let result = modelActionProcessor.execute(actions);

    expect(modelChangeAttributeActionProcessorStub.execute.calledOnce).to.be.true;
    expect(modelRestoreInheritedAttributeActionProcessorStub.execute.calledOnce).to.be.true;
    expect(result).to.deep.eq(['execute-result1', 'execute-result2', 'execute-result']);
  });

  it('should properly call and restore actions', () => {
    let result = modelActionProcessor.restore(actions);

    expect(modelChangeAttributeActionProcessorStub.restore.calledOnce).to.be.true;
    expect(modelRestoreInheritedAttributeActionProcessorStub.restore.calledOnce).to.be.true;
    expect(result).to.deep.eq(['restore-result1', 'restore-result2', 'restore-result']);
  });

  it('should properly call and build changeset for actions', () => {
    let result = modelActionProcessor.changeset(actions);

    expect(modelChangeAttributeActionProcessorStub.changeset.calledOnce).to.be.true;
    expect(modelRestoreInheritedAttributeActionProcessorStub.changeset.calledOnce).to.be.true;
    expect(result).to.deep.eq(['changeset-result1', 'changeset-result2', 'changeset-result']);
  });

  it('should properly call custom executors for actions', () => {
    let executors = {};
    executors[ModelRestoreInheritedAttributeAction.getType()] = () => 'custom-result';
    expect(modelActionProcessor.on(actions, executors)).to.deep.eq([null, 'custom-result']);
  });

  function getProcessorMapping() {
    return {
      'modelChangeAttributeActionProcessor': modelChangeAttributeActionProcessorStub,
      'modelRestoreInheritedAttributeActionProcessor': modelRestoreInheritedAttributeActionProcessorStub
    };
  }

  function getActionMapping() {
    return {
      'modelChangeAttributeActionProcessor': {action: 'ModelChangeAttributeAction'},
      'modelRestoreInheritedAttributeActionProcessor': {action: 'ModelRestoreInheritedAttributeAction'}
    };
  }
});
