import {Injectable} from 'app/app';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelRestoreInheritedAttributeAction} from './model-restore-inherited-attribute-action';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Factory taking care of creating an action of type {@link ModelRestoreInheritedAttributeAction}
 * this action requires additional parameter to be provided representing the attribute to be restored.
 * Additionally after creation there is an option to attach any related models to the provided attribute
 * for restoration.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelRestoreInheritedAttributeActionFactory {

  //@Override
  create(attribute) {
    return new ModelRestoreInheritedAttributeAction().setAttributesToRestore([attribute]);
  }

  evaluate(action) {
    action.setAttributesToRestore(this.getAttributesToRestore(action));
    return action;
  }

  getAttributesToRestore(action) {
    let models = action.getAttributesToRestore();
    let attrId = models[0].getId();

    let related = ModelRestoreInheritedAttributeActionFactory.RELATED_ATTRIBUTES[attrId] || [];
    related = related.map(id => action.getModel().getAttribute(id)).concat(models);
    return related.filter(a => !!a && !ModelManagementUtility.isInherited(a, action.getModel()));
  }
}

ModelRestoreInheritedAttributeActionFactory.RELATED_ATTRIBUTES = {};
ModelRestoreInheritedAttributeActionFactory.RELATED_ATTRIBUTES[ModelAttribute.CODELIST_ATTRIBUTE] = [
  ModelAttribute.TYPE_OPTION_ATTRIBUTE, ModelAttribute.TYPE_ATTRIBUTE, ModelAttribute.VALUE_ATTRIBUTE
];
ModelRestoreInheritedAttributeActionFactory.RELATED_ATTRIBUTES[ModelAttribute.TYPE_ATTRIBUTE] = [
  ModelAttribute.CODELIST_ATTRIBUTE, ModelAttribute.TYPE_OPTION_ATTRIBUTE, ModelAttribute.VALUE_ATTRIBUTE
];
ModelRestoreInheritedAttributeActionFactory.RELATED_ATTRIBUTES[ModelAttribute.TYPE_OPTION_ATTRIBUTE] = [
  ModelAttribute.CODELIST_ATTRIBUTE, ModelAttribute.TYPE_ATTRIBUTE, ModelAttribute.VALUE_ATTRIBUTE
];
