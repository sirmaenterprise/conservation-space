import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Processor taking care of executing an operation of type {@link ModelRestoreInheritedControlAction}
 * This processor is directly restoring the given inherited controls from a parent model.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelRestoreInheritedControlActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let source = action.getModel();
    let reference = source.getReference();
    let toRestore = action.getControlsToRestore();

    let extractor = (control) => reference.getControl(control.getId());
    toRestore.forEach(control => {
      let referenceControl = extractor(control);
      referenceControl ? source.addControl(referenceControl) : source.removeControl(control);
    });
    return this.computeModel(action);
  }

  restore(action) {
    let source = action.getModel();
    let toRestore = action.getControlsToRestore();

    toRestore.forEach(control => source.addControl(control));
    return this.computeModel(action);
  }

  //@Override
  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSets(action.getControlsToRestore(), ModelOperation.RESTORE);
  }

  computeModel(action) {
    let source = action.getModel();
    let context = action.getContext();

    if (!source.isOwningModels()) {
      source = source.getReference();
    }
    ModelManagementUtility.addToModel(context, source);
    return source;
  }
}
