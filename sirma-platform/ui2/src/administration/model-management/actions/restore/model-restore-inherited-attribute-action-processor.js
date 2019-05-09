import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Processor taking care of executing an operation of type {@link ModelRestoreInheritedAttributeAction}
 * This processor is directly restoring the given inherited attributes from a parent model. Even though
 * the action takes a single attribute to be restored multiple attributes can be restored when there is
 * a relationship between them.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelRestoreInheritedAttributeActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let source = action.getModel();
    let reference = source.getReference();
    let toRestore = action.getAttributesToRestore();

    let extractor = (attr) => reference.getAttribute(attr.getId());
    toRestore.forEach(attr => source.addAttribute(extractor(attr)));
    return this.computeModel(action);
  }

  restore(action) {
    let source = action.getModel();
    let toRestore = action.getAttributesToRestore();

    toRestore.forEach(attr => source.addAttribute(attr));
    return this.computeModel(action);
  }

  //@Override
  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSets(action.getAttributesToRestore(), ModelOperation.RESTORE);
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
