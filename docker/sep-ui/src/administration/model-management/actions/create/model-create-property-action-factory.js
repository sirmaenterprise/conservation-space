import {Injectable, Inject} from 'app/app';
import {ModelCreatePropertyAction} from './model-create-property-action';
import {ModelPropertyLinker} from 'administration/model-management/services/linkers/model-property-linker';

/**
 * Factory taking care of creating an action of type {@link ModelCreatePropertyAction}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelPropertyLinker)
export class ModelCreatePropertyActionFactory {

  constructor(modelPropertyLinker) {
    this.modelPropertyLinker = modelPropertyLinker;
  }

  create(meta) {
    return new ModelCreatePropertyAction().setMetaData(meta);
  }

  evaluate(action) {
    action.setModel(this.createProperty(action));
    return action;
  }

  createProperty(action) {
    return this.modelPropertyLinker.createModelProperty(null, action.getMetaData()).setParent(action.getContext());
  }
}
