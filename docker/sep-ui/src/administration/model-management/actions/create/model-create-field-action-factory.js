import {Injectable, Inject} from 'app/app';
import {ModelCreateFieldAction} from './model-create-field-action';
import {ModelFieldLinker} from 'administration/model-management/services/linkers/model-field-linker';

/**
 * Factory taking care of creating an action of type {@link ModelCreateFieldAction}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelFieldLinker)
export class ModelCreateFieldActionFactory {

  constructor(modelFieldLinker) {
    this.modelFieldLinker = modelFieldLinker;
  }

  create(meta) {
    return new ModelCreateFieldAction().setMetaData(meta);
  }

  evaluate(action) {
    action.setModel(this.createField(action));
    return action;
  }

  createField(action) {
    return this.modelFieldLinker.createModelField(null, action.getMetaData()).setParent(action.getContext());
  }
}
