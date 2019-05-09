import {Injectable} from 'app/app';
import {ModelBase} from 'administration/model-management/model/model-base';

import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

/**
 * Service which builds and links an already constructed class or definition hierarchy. This service traverses
 * the either hierarchy and links all parent - child relations between different semantic or definition models
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelStoreLinker {

  /**
   * Link all relations between class models in a provided model store
   *
   * @param store - the model store
   */
  linkModelClasses(store) {
    let clazzProvider = (id) => store.getModel(id, ModelClass);
    store.getModels(ModelClass).forEach(clazz => this.linkParent(clazz, clazzProvider));
    return store;
  }

  /**
   * Link all relations between definition models in a provided model store
   *
   * @param store - the model store
   */
  linkModelDefinitions(store) {
    let clazzProvider = (id) => store.getModel(id, ModelClass);
    let defProvider = (id) => store.getModel(id, ModelDefinition);
    store.getModels(ModelDefinition).forEach(definition => {
      // link parent of the definition and it's type
      this.linkParent(definition, defProvider);
      this.linkType(definition, clazzProvider);

      // add definition as type to class
      let type = definition.getType();
      type && type.addType(definition);
    });
    return store;
  }

  /**
   * Link all relations between property models in a provided model store
   *
   * @param store - the model store
   */
  linkModelProperties(store) {
    let clazzProvider = (id) => store.getModel(id, ModelClass);
    store.getModels(ModelProperty).map(property => this.linkParent(property, clazzProvider));
    return store;
  }

  linkParent(model, provider) {
    return this.linkGeneric(model, provider, 'getParent', 'setParent');
  }

  linkType(model, provider) {
    return this.linkGeneric(model, provider, 'getType', 'setType');
  }

  linkGeneric(model, provider, getter, setter) {
    let identifier = model[getter] && model[getter]();
    if (identifier && !(identifier instanceof ModelBase)) {
      model[setter](provider(identifier));
    }
    return this;
  }
}