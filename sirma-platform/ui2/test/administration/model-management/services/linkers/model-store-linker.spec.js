import {ModelStoreLinker} from 'administration/model-management/services/linkers/model-store-linker';
import {ModelStore} from 'administration/model-management/model/models-store';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelClass} from 'administration/model-management/model/model-class';

describe('ModelStoreLinker', () => {

  let modelStoreLinker;

  beforeEach(() => {
    modelStoreLinker = new ModelStoreLinker();
  });

  it('should link parent and child relations inside the provided model store for classes', () => {
    let store = getModelStore();
    modelStoreLinker.linkModelClasses(store);

    // root elements of the hierarchy have no parents to be linked
    expect(store.getModel('entity').getParent()).to.eq(null);

    // child elements of the hierarchy have their respective parents to be linked
    expect(store.getModel('media').getParent()).to.eq(store.getModel('entity'));
  });

  it('should link parent and child relations inside the provided model store for definitions', () => {
    let store = getModelStore();
    modelStoreLinker.linkModelDefinitions(store);

    // root elements of the hierarchy have no parents to be linked
    expect(store.getModel('object').getParent()).to.eq(null);

    // child elements of the hierarchy have their respective parents to be linked
    expect(store.getModel('project').getParent()).to.eq(store.getModel('object'));

    // definition type models also refer to their respective semantic class type
    expect(store.getModel('project').getType()).to.eq(store.getModel('entity'));
    expect(store.getModel('object').getType()).to.eq(store.getModel('entity'));
  });

  it('should link parent and child relations inside the provided model store for properties', () => {
    let store = getModelStore();
    modelStoreLinker.linkModelProperties(store);

    // should link property parents by resolving the string parent as actual model
    expect(store.getModel('prop2').getParent()).to.eq(store.getModel('media'));
    expect(store.getModel('prop1').getParent()).to.eq(store.getModel('entity'));
  });

  function getModelStore() {
    let store = new ModelStore();
    store.addModel(new ModelClass('entity', null));
    store.addModel(new ModelClass('media', 'entity'));

    store.addModel(new ModelDefinition('object', null, false, 'entity'));
    store.addModel(new ModelDefinition('project', 'object', false, 'entity'));

    store.addModel(new ModelProperty('prop1').setParent('entity'));
    store.addModel(new ModelProperty('prop2').setParent('media'));
    return store;
  }
});