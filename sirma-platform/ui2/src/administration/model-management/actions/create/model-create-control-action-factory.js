import {Injectable, Inject} from 'app/app';
import {ModelCreateControlAction} from './model-create-control-action';
import {ModelControlLinker} from 'administration/model-management/services/linkers/model-control-linker';

/**
 * Factory taking care of creating an action of type {@link ModelCreateControlAction}
 *
 * @author Stella D
 */
@Injectable()
@Inject(ModelControlLinker)
export class ModelCreateControlActionFactory {

  constructor(modelControlLinker) {
    this.modelControlLinker = modelControlLinker;
  }

  create(meta, id, definition) {
    return new ModelCreateControlAction().setMetaData(meta).setId(id).setDefinition(definition);
  }

  evaluate(action, id) {
    action.setModel(this.createControl(action));
    return action;
  }

  createControl(action) {
    return this.modelControlLinker.createModelControl(action.getId(), action.getMetaData());
  }
}
