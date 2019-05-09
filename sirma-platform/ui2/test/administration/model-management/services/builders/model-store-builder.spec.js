import {ModelStoreBuilder} from 'administration/model-management/services/builders/model-store-builder';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelStore} from 'administration/model-management/model/models-store';

import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {ModelStoreLinker} from 'administration/model-management/services/linkers/model-store-linker';
import {ModelClassHierarchy, ModelDefinitionHierarchy} from 'administration/model-management/model/model-hierarchy';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelStoreBuilder', () => {

  let modelStoreBuilder;
  let modelStoreLinkerStub;

  beforeEach(() => {
    modelStoreLinkerStub = stub(ModelStoreLinker);
    modelStoreLinkerStub.linkModelClasses.returns(PromiseStub.resolve());
    modelStoreLinkerStub.linkModelDefinitions.returns(PromiseStub.resolve());
    modelStoreLinkerStub.linkModelProperties.returns(PromiseStub.resolve());

    modelStoreBuilder = new ModelStoreBuilder(modelStoreLinkerStub);
  });

  it('should build a model store from a hierarchy', () => {
    let store = new ModelStore();
    modelStoreBuilder.buildStoreFromHierarchy(getModelHierarchy(), store);

    expect(store.getModels(ModelClass).length).to.eq(2);
    expect(store.getModel('emf:Entity')).to.exist;
    expect(store.getModel('emf:Project')).to.exist;

    expect(store.getModels(ModelDefinition).length).to.eq(2);
    expect(store.getModel('media')).to.exist;
    expect(store.getModel('object')).to.exist;

    expect(modelStoreLinkerStub.linkModelClasses.called).to.be.true;
    expect(modelStoreLinkerStub.linkModelDefinitions.called).to.be.true;
  });

  it('should build a model store from properties', () => {
    let store = new ModelStore();
    modelStoreBuilder.buildStoreFromProperties(getModelProperties(), store);

    expect(store.getModels(ModelProperty).length).to.eq(3);
    expect(store.getModel('prop1')).to.exist;
    expect(store.getModel('prop2')).to.exist;
    expect(store.getModel('prop3')).to.exist;

    expect(modelStoreLinkerStub.linkModelProperties.called).to.be.true;
  });

  function getModelProperties() {
    return new ModelList()
      .insert(new ModelProperty('prop1'))
      .insert(new ModelProperty('prop2'))
      .insert(new ModelProperty('prop3'));
  }

  function getModelHierarchy() {
    let entity = new ModelClassHierarchy(new ModelClass('emf:Entity'));
    let project = new ModelClassHierarchy(new ModelClass('emf:Project'));

    let media = new ModelDefinitionHierarchy(new ModelDefinition('media'));
    let object = new ModelDefinitionHierarchy(new ModelDefinition('object'));

    entity.insertChild(project);
    project.insertChild(media);
    project.insertChild(object);

    return entity;
  }
});