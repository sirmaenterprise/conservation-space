import {Injectable, Inject} from 'app/app';
import {ModelCreateControlParamAction} from './model-create-control-param-action';
import {ModelControlParamLinker} from 'administration/model-management/services/linkers/model-control-param-linker';

/**
 * Factory taking care of creating an action of type {@link ModelCreateControlParamAction}
 *
 * @author Stella D
 */
@Injectable()
@Inject(ModelControlParamLinker)
export class ModelCreateControlParamActionFactory {

  constructor(modelControlParamLinker) {
    this.modelControlParamLinker = modelControlParamLinker;
  }

  create(meta, attributes) {
    return new ModelCreateControlParamAction().setMetaData(meta).setAttributes(attributes);
  }

  evaluate(action) {
    action.setModel(this.createControlParam(action));
    return action;
  }

  createControlParam(action) {
    return this.modelControlParamLinker.createModelControlParam(action.getAttributes(), action.getMetaData());
  }
}
