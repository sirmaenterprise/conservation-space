import {Injectable, Inject} from 'app/app';

import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelManagementCopyService} from 'administration/model-management/services/model-management-copy-service';

import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Processor taking care of executing an action of type {@link ModelChangeAttributeAction}
 * This processor takes care of resolving or creating clone of the changed attribute in case
 * the attribute being changed is inherited. The action can also be restored / reversed
 * meaning that all changes performed by the execute method can be reversed to the original
 * state before the execution.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementCopyService, ModelChangeSetBuilder, ModelPathBuilder)
export class ModelChangeAttributeActionProcessor {

  constructor(modelManagementCopyService, modelChangeSetBuilder, modelPathBuilder) {
    this.modelManagementCopyService = modelManagementCopyService;
    this.modelChangeSetBuilder = modelChangeSetBuilder;
    this.modelPathBuilder = modelPathBuilder;
  }

  execute(action) {
    let attribute = action.getModel();
    let model = action.getOwningContext();

    let owner = ModelManagementUtility.getOwningModel(attribute);
    let modelType = model && ModelManagementUtility.getModelType(model);
    let ownerType = owner && ModelManagementUtility.getModelType(owner);

    // early out make sure attribute is valid and model and owner are valid
    if (owner === model || modelType !== ownerType) {
      this.setComputedContext(action, this.computeContext(action));
      return attribute;
    }

    // compute the attribute to be edited using inheritance model
    let path = this.modelPathBuilder.buildPathFromModel(attribute);
    let copied = this.modelManagementCopyService.copyFromPath(path, model);

    // update the state of the action with the new data state
    action.setModel(copied).setInherited(copied !== attribute);
    this.setComputedContext(action, this.computeContext(action));

    return copied;
  }

  restore(action) {
    if (action.isInherited()) {
      let attribute = action.getModel();
      let model = action.getOwningContext();

      let owner = ModelManagementUtility.getOwningModel(attribute);
      let modelType = ModelManagementUtility.getModelType(model);
      let ownerType = ModelManagementUtility.getModelType(owner);

      // src & dst types must be same
      if (modelType !== ownerType) {
        return attribute;
      }

      // compute the attribute to be restored using inheritance model
      let path = this.modelPathBuilder.buildPathFromModel(attribute);
      let restored = this.modelManagementCopyService.restoreFromPath(path, model);

      // make sure to leave the action in proper state after the revert
      action.setModel(restored).setInherited(restored !== attribute);
      this.setComputedContext(action, this.computeContext(action));

      return restored;
    } else {
      let attribute = action.getModel();
      attribute.restoreValue();
      return attribute;
    }
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSet(action.getModel(), ModelOperation.MODIFY);
  }

  computeContext(action) {
    let source = action.getOwningContext();
    let parent = action.getModel().getParent();

    let parentType = ModelManagementUtility.getModelType(parent);
    let contextType = ModelManagementUtility.getModelType(source);

    if (contextType !== parentType) {
      let path = this.modelPathBuilder.buildPathFromModel(action.getModel());
      // due to the fact that property path does not start with it's class owner
      // workaround taking into account that property path is resolved differently
      path = !ModelManagementUtility.isModelProperty(parent) ? path.getNext() : path;
      return ModelManagementUtility.walk(this.getComputedWalkingPath(path), source);
    }
    return source;
  }

  getComputedWalkingPath(path) {
    return path.cut(path.tail());
  }

  setComputedContext(action, context) {
    return context && action.setContext(context);
  }
}

ModelChangeAttributeActionProcessor.MODIFY_SINGLE_VALUED = 'modifyAttribute';
ModelChangeAttributeActionProcessor.MODIFY_MULTI_VALUED = 'modifyMapAttribute';
