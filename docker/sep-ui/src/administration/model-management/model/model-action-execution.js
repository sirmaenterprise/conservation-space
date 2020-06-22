import {ModelBase} from 'administration/model-management/model/model-base';

/**
 * Holds definition information about a {@link ModelAction} behavior.
 * <pre>
 *  There are two kind of action executions:
 *  1.  Action executions which hold configuration about creation of relations.
 *       1.1 Definition representation:
 *          Such action execution is described by control tag with attribute id which value is "configuration";
 *       2.1 Modeling representation:
 *          Such action execution will be a {@link ModelActionExecution} object with type {@link ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION}.
 *  2.  Action executions which holds javascript which have to be executed during action processing.
 *      2.1 Definition representation:
 *          Such action execution is described by control tag with attribute id which value is "SCRIPT";
 *      2.1 Modeling representation:
 *           Such action execution will be {@link ModelActionExecution} object with type {@link ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_EXECUTE_SCRIPT}.
 * </pre>
 *
 * @author Boyan Tonchev.
 */
export class ModelActionExecution extends ModelBase {

  constructor(id) {
    super(id);
    this.inherited = true;
  }
}

export class ModelActionExecutionTypes {
}

ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION = 'createRelation';
ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_EXECUTE_SCRIPT = 'executeScript';