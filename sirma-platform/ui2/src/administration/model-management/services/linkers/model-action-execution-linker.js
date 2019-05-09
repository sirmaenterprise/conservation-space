import {Inject, Injectable} from 'app/app';
import {ModelActionExecution} from 'administration/model-management/model/model-action-execution';

import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';

/**
 * Service which builds and links {@link ModelActionExecution} to a given {@link ModelAction}.
 *
 * @author B.Tonchev
 */
@Injectable()
@Inject(ModelAttributeLinker)
export class ModelActionExecutionLinker {

  constructor(modelAttributeLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
  }

  linkActionExecutions(model, modelActions, meta) {
    let actions = modelActions || [];
    actions.forEach(modelAction => {
      let action = model.getAction(modelAction.id);
      action && this.fillActionExecutions(action, modelAction, meta);
    });
  }

  fillActionExecutions(actionModel, action, actionExecutionMeta) {
    if (action.actionExecutions) {
      action.actionExecutions.forEach(actionExecution => {
        let actionExecutionModel = new ModelActionExecution(actionExecution.id);
        this.modelAttributeLinker.linkAttributes(actionExecutionModel, actionExecution.attributes, actionExecutionMeta);
        actionModel.addActionExecution(actionExecutionModel);
        actionExecutionModel.setParent(actionModel);
      });
    }
  }
}