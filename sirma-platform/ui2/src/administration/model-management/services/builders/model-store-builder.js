import {Injectable, Inject} from 'app/app';
import {ModelStore} from 'administration/model-management/model/models-store';
import {ModelStoreLinker} from 'administration/model-management/services/linkers/model-store-linker';

/**
 * Service providing means of constructing a {@link ModelStore} from different types
 * of model representations such as hierarchy, map etc. Model store collects only
 * class or definition models
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelStoreLinker)
export class ModelStoreBuilder {

  constructor(modelStoreLinker) {
    this.modelStoreLinker = modelStoreLinker;
  }

  /**
   * Constructs a model store from a class or definition hierarchy of either the
   * {@link ModelClassHierarchy} or {@link ModelDefinitionHierarchy}
   *
   * @param hierarchy - class or definition hierarchy
   * @returns {ModelStore} - constructed model store
   */
  buildStoreFromHierarchy(hierarchy, store = new ModelStore()) {
    store = this.buildFromHierarchy(hierarchy, store);
    this.modelStoreLinker.linkModelClasses(store);
    this.modelStoreLinker.linkModelDefinitions(store);
    return store;
  }

  /**
   * Constructs a model store from a list of model properties of type
   * {@link ModelProperty}
   *
   * @param properties - list of model properties
   * @returns {ModelStore} - constructed model store
   */
  buildStoreFromProperties(properties, store = new ModelStore()) {
    properties.getModels().forEach(property => store.addModel(property));
    this.modelStoreLinker.linkModelProperties(store);
    return store;
  }

  buildFromHierarchy(hierarchy, store) {
    if (Array.isArray(hierarchy)) {
      hierarchy.forEach(node => this.buildFromHierarchy(node, store));
    } else {
      store.addModel(hierarchy.getRoot());
      hierarchy.getChildren().forEach(child => this.buildFromHierarchy(child, store));
    }
    return store;
  }
}