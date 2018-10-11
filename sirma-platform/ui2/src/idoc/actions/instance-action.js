import {ActionHandler} from 'services/actions/action-handler';

export class InstanceAction extends ActionHandler {

  constructor(logger) {
    super();
    this.logger = logger;
  }

  /**
   * @param actionDefinition
   * @param currentObject
   * @param operation Is the serverOperation as returned by the backend with the action
   * @returns {{operation: *, userOperation: *, contextPath: *, targetInstance: {definitionId: *, properties: *}}}
   */
  buildActionPayload(actionDefinition, currentObject, operation) {
    return {
      operation: operation,
      userOperation: actionDefinition.action,
      contextPath: currentObject.getContextPathIds(),
      targetInstance: {
        definitionId: currentObject.getModels().definitionId,
        properties: currentObject.getChangeset()
      }
    };
  }

  /**
   * Using the response, this method reloads the instance model and updates the underlying object properties and content.
   * @param object The response of instance-service #load or #update
   * @param context current execution context.
   */
  refreshInstance(object, context) {
    if (context.idocContext) {
      context.idocContext.reloadObjectDetails(object.id)
        .then(() => this.afterInstanceRefreshHandler && this.afterInstanceRefreshHandler(context))
        .catch(this.logger.error);
    }
  }

  checkPermissionsForEditAction(context) {
    if (context.idocActionsController) {
      context.idocActionsController.loadActions();
    }
  }
}