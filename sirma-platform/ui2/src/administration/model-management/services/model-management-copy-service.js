import {Injectable, getClassName} from 'app/app';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelHeader} from 'administration/model-management/model/model-header';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelAction} from 'administration/model-management/model/model-action';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

/**
 * Service providing means to copy, revert or otherwise process model nodes
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelManagementCopyService {

  /**
   * Copies all individual model nodes from the provided path to the provided model.
   * The copy is resolved by first checking if the currently processed node is owned
   * by the provided model or not.
   *
   * If the node is not owned by the provided model, the node is copied and replaced
   * in the provided model at the same place.
   *
   * If the node is owned by the provided model, then the node is skipped and the method
   * procedes to the next node in the path.
   *
   * The process is repeated for each node in the path until the end of the path. All nodes
   * in the path will be copied if needed and the last node in the path would be returned.
   *
   * @param path - path describing the nodes to be copied
   * @param model - model for which to copy nodes
   * @returns the last node in the path as a model
   */
  copyFromPath(path, model) {
    path = path.getNext();
    if (!path) {
      return model;
    }

    // step to the current model for the current model path node
    let current = ModelManagementCopyService.getCurrent(path, model);
    let isInherited = ModelManagementUtility.isInherited(current, model);

    if (isInherited) {
      // the current model is actually the inherited one, create a copy of it
      current = ModelManagementCopyService.createCopiedModel(current, model);
    }
    // proceed and process the next model in the path
    return this.copyFromPath(path, current);
  }

  restoreFromPath(path, model, source = model.getParent()) {
    path = path.getNext();
    if (!path) {
      return source;
    }

    // extract both nodes from the source and the destination models
    let currentSrc = ModelManagementCopyService.getCurrent(path, source);
    let currentDst = ModelManagementCopyService.getCurrent(path, model);

    // recurse in depth first and restore the nodes bottom up
    let result = this.restoreFromPath(path, currentDst, currentSrc);

    if (currentSrc !== currentDst) {
      // attributes should be restored unconditionally, while other models can be implicitly restored when
      // all models they currently hold are inherited therefore the entire holding model is considered inherited
      let canBeRestored = ModelManagementUtility.isModelAttribute(currentSrc) || !currentDst.isOwningModels();
      canBeRestored && ModelManagementCopyService.addToModel(model, currentSrc);
    }
    return result;
  }

  static createCopiedModel(toCopy, dstModel) {
    let clazz = ModelManagementUtility.getClassName(toCopy);
    let copy = ModelManagementCopyService.getCopyOfModel(clazz, toCopy);
    copy.setParent(dstModel).setReference(toCopy);
    ModelManagementCopyService.addToModel(dstModel, copy);
    return copy;
  }

  static addToModel(model, toAdd) {
    ModelManagementUtility.addToModel(model, toAdd, ModelManagementCopyService.CUSTOM_APPENDER);
  }

  static getCurrent(path, model) {
    return path.step(model, ModelManagementCopyService.CUSTOM_WALKER);
  }

  static getCopyOfModel(type, from) {
    return new ModelManagementCopyService.TYPE_CREATOR[type]().copyFrom(from);
  }

  static addFieldDeep(model, field) {
    let found = model.getField(field.getId());
    let region = model.getRegion(found.getRegionId());

    model.addField(field);

    if (region) {
      if (this.isRegionCopiable(model, region)) {
        // region is potentially inherited, create a copy of it's contents
        region = ModelManagementCopyService.createCopiedModel(region, model);
      }

      if (this.isRegionRestorable(model, region)) {
        // restore region from parent does not own any models
        region = model.getParent().getRegion(region.getId());
        // we can restore it from the parent model safely
        ModelManagementCopyService.addToModel(model, region);
      }
    }
  }

  static getFieldDeep(model, id) {
    return model.getField(id);
  }

  static isRegionCopiable(model, region) {
    return ModelManagementUtility.isInherited(region, model);
  }

  static isRegionRestorable(model, region) {
    return !region.isOwningModels() && !model.getFields()
        .filter(field => field.getRegionId() === region.getId())
        .filter(field => field.getParent() === region.getParent())
        .length;
  }
}

ModelManagementCopyService.CUSTOM_WALKER = _.cloneDeep(ModelManagementUtility.DEFAULT_WALKER);
ModelManagementCopyService.CUSTOM_WALKER[ModelManagementUtility.TYPES.FIELD] = (model, id) => ModelManagementCopyService.getFieldDeep(model, id);

ModelManagementCopyService.CUSTOM_APPENDER = _.cloneDeep(ModelManagementUtility.DEFAULT_APPENDER);
ModelManagementCopyService.CUSTOM_APPENDER[ModelManagementUtility.TYPES.FIELD] = (model, toAdd) => ModelManagementCopyService.addFieldDeep(model, toAdd);

ModelManagementCopyService.TYPE_CREATOR = {};
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelClass)] = ModelClass;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelDefinition)] = ModelDefinition;

ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelField)] = ModelField;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelControl)] = ModelControl;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelControlParam)] = ModelControlParam;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelRegion)] = ModelRegion;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelProperty)] = ModelProperty;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelHeader)] = ModelHeader;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelAction)] = ModelAction;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelActionGroup)] = ModelActionGroup;


ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelMultiAttribute)] = ModelMultiAttribute;
ModelManagementCopyService.TYPE_CREATOR[getClassName(ModelSingleAttribute)] = ModelSingleAttribute;
