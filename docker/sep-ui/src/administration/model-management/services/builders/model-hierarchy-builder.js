import {Inject, Injectable} from 'app/app';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelClassHierarchy, ModelDefinitionHierarchy} from 'administration/model-management/model/model-hierarchy';
import _ from 'lodash';

const CLASS_ICON = 'fa fa-fw fa-home';
const DEFINITION_ICON = 'fa fa-fw fa-cog';

/**
 * Service which builds a class and definition hierarchy from a provided response. The built hierarchy
 * is of type {@link ModelClassHierarchy} or {@link ModelDefinitionHierarchy}. It directly represents
 * the hierarchical structure provided by the response.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelDescriptionLinker)
export class ModelHierarchyBuilder {

  constructor(modelDescriptionLinker) {
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  /**
   * Builds a hierarchy of type {@link ModelClassHierarchy} or {@link ModelDefinitionHierarchy} from
   * a provided response.
   *
   * @param hierarchy - response hierarchy provided by a restful service.
   */
  buildHierarchy(hierarchy) {
    return this.constructClassHierarchy(hierarchy);
  }

  constructClassHierarchy(classes, root) {
    let roots = [];
    classes && classes.forEach(clazz => {
      let process = false;
      let node = new ModelClassHierarchy();

      if (!root && this.isRoot(classes, clazz)) {
        process = true;
        roots.push(node);
      } else if (root && clazz.parentId === root.getRoot().getId()) {
        process = true;
        root.insertChild(node);
      }

      if (process) {
        let modelClass = new ModelClass(clazz.id, clazz.parentId);
        modelClass.setIcon(CLASS_ICON);

        node.setParent(root);
        node.setRoot(modelClass);
        node.insertChildren(this.constructDefinitionHierarchy(modelClass, clazz));

        this.insertDescriptions(modelClass, clazz.labels);
        this.constructClassHierarchy(classes, node);
      }
    });
    return roots;
  }

  constructDefinitionHierarchy(model, clazz, root) {
    let roots = [];
    clazz.subTypes && clazz.subTypes.forEach(type => {
      let process = false;
      let node = new ModelDefinitionHierarchy();

      if (!root && this.isRoot(clazz.subTypes, type)) {
        process = true;
        roots.push(node);
      } else if (root && type.parentId === root.getRoot().getId()) {
        process = true;
        root.insertChild(node);
      }

      if (process) {
        let modelDefinition = new ModelDefinition(type.id, type.parentId, type.abstract, model.getId());
        modelDefinition.setIcon(DEFINITION_ICON);

        node.setParent(root);
        node.setRoot(modelDefinition);

        this.insertDescriptions(modelDefinition, type.labels);
        this.constructDefinitionHierarchy(model, clazz, node);
      }
    });
    return roots;
  }

  insertDescriptions(model, labels) {
    this.modelDescriptionLinker.insertDescriptions(model, labels);
  }

  isRoot(collection, element) {
    return !element.parentId || _.every(collection, item => element.parentId !== item.id);
  }
}