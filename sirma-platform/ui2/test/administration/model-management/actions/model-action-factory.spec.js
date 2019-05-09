import {ModelActionFactory} from 'administration/model-management/actions/model-action-factory';
import {PluginsService} from 'services/plugin/plugins-service';

import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelHeader} from 'administration/model-management/model/model-header';

import {ModelRestoreInheritedAttributeAction} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action';
import {ModelRestoreInheritedAttributeActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action-factory';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelActionFactory', () => {

  let modelActionFactory;

  let pluginsServiceStub;
  let modelRestoreInheritedAttributeActionFactoryStub;

  beforeEach(() => {
    pluginsServiceStub = stub(PluginsService);
    modelRestoreInheritedAttributeActionFactoryStub = stub(ModelRestoreInheritedAttributeActionFactory);

    pluginsServiceStub.getPluginDefinitions.returns(getActionMapping());
    pluginsServiceStub.loadPluginServiceModules.returns(PromiseStub.resolve(getFactoryMapping()));

    modelActionFactory = new ModelActionFactory(pluginsServiceStub);
  });

  it('should create a given type of action on demand', () => {
    let model = new ModelDefinition();
    let toRestore = [new ModelHeader('default_header')];
    modelRestoreInheritedAttributeActionFactoryStub.create.returns(new ModelRestoreInheritedAttributeAction().setAttributesToRestore(toRestore));
    let action = modelActionFactory.create(ModelRestoreInheritedAttributeAction.getType(), model, toRestore);

    expect(action.getModel()).to.deep.eq(model);
    expect(action.getAttributesToRestore()).to.deep.eq(toRestore);
  });

  function getFactoryMapping() {
    return {'modelRestoreInheritedAttributeActionFactory': modelRestoreInheritedAttributeActionFactoryStub};
  }

  function getActionMapping() {
    return {'modelRestoreInheritedAttributeActionFactory': {action: 'ModelRestoreInheritedAttributeAction'}};
  }
});
