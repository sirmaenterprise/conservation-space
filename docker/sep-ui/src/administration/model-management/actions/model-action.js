import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Generic representation of a model action. It holds the actual model upon which
 * a given action has been created and provides interface for retrieving the type
 * and id of the action.
 *
 * @author Svetlozar Iliev
 */
export class ModelAction {

  getType() {
    return ModelManagementUtility.getActionType(this);
  }

  setModel(model) {
    this.model = model;
    return this;
  }

  getModel() {
    return this.model;
  }

  getContext() {
    return this.context;
  }

  setContext(context) {
    this.context = context;
    return this;
  }

  static getType() {
    return ModelManagementUtility.getActionType(this);
  }
}