import {ModelDataBuilder} from 'administration/model-management/services/builders/model-data-builder';
import {ModelDataLinker} from 'administration/model-management/services/linkers/model-data-linker';
import {ModelFieldLinker} from 'administration/model-management/services/linkers/model-field-linker';
import {ModelRegionLinker} from 'administration/model-management/services/linkers/model-region-linker';
import {ModelHeaderLinker} from 'administration/model-management/services/linkers/model-header-linker';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelActionGroupLinker} from 'administration/model-management/services/linkers/model-action-group-linker';
import {ModelActionLinker} from 'administration/model-management/services/linkers/model-action-linker';
import {ModelActionExecutionLinker} from 'administration/model-management/services/linkers/model-action-execution-linker';

import {ModelStore} from 'administration/model-management/model/models-store';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelDataBuilder', () => {

  let modelDataBuilder;

  let modelDataLinkerStub;
  let modelFieldLinkerStub;
  let modelRegionLinkerStub;
  let modelHeaderLinkerStub;
  let modelAttributeLinkerStub;
  let modelModelActionGroupLinkerStub;
  let modelModelActionLinkerStub;
  let modelModelActionExecutionLinkerStub;

  beforeEach(() => {
    modelDataLinkerStub = stub(ModelDataLinker);
    modelFieldLinkerStub = stub(ModelFieldLinker);
    modelRegionLinkerStub = stub(ModelRegionLinker);
    modelHeaderLinkerStub = stub(ModelHeaderLinker);
    modelAttributeLinkerStub = stub(ModelAttributeLinker);
    modelModelActionGroupLinkerStub = stub(ModelActionGroupLinker);
    modelModelActionLinkerStub = stub(ModelActionLinker);
    modelModelActionExecutionLinkerStub = stub(ModelActionExecutionLinker);
    modelDataBuilder = new ModelDataBuilder(modelDataLinkerStub, modelFieldLinkerStub, modelRegionLinkerStub, modelAttributeLinkerStub, modelModelActionGroupLinkerStub, modelModelActionLinkerStub, modelModelActionExecutionLinkerStub, modelHeaderLinkerStub);
  });

  it('should properly build links for the requested model', () => {
    let models = getModels();
    let model = new ModelClass();
    modelDataBuilder.buildModelLinks(model, models);

    // data linker should be called for the given model
    expect(modelDataLinkerStub.linkInheritanceModel.calledOnce).to.be.true;
    expect(modelDataLinkerStub.linkInheritanceModel.calledWith(model)).to.be.true;
  });

  it('should properly build requested models by the provided response data', () => {
    let models = getModels();
    let meta = new ModelsMetaData();
    modelDataBuilder.buildModels(models, meta, getData());

    // core component linkers should be called for each model
    expect(modelFieldLinkerStub.linkFields.called).to.be.true;
    expect(modelRegionLinkerStub.linkRegions.called).to.be.true;
    expect(modelHeaderLinkerStub.linkHeaders.called).to.be.true;
    expect(modelAttributeLinkerStub.linkAttributes.called).to.be.true;
    expect(modelModelActionGroupLinkerStub.linkActionGroups.called).to.be.true;
    expect(modelModelActionLinkerStub.linkActions.called).to.be.true;
    expect(modelModelActionExecutionLinkerStub.linkActionExecutions.called).to.be.true;

    // models should be flagged as loaded once they have been built completely
    models.getModels().forEach(model => expect(model.isLoaded()).to.be.true);
  });

  function getModels() {
    let store = new ModelStore();
    // append stubbed model classes to the store
    store.addModel(new ModelClass('emf:Entity'));
    store.addModel(new ModelClass('emf:Media'));
    store.addModel(new ModelClass('emf:Project'));
    // append stubbed model definitions to the store
    store.addModel(new ModelDefinition('entity'));
    store.addModel(new ModelDefinition('media'));
    return store;
  }

  function getData() {
    return {
      'classes': [{
        'id': 'emf:Entity'
      }, {
        'id': 'emf:Media'
      }, {
        'id': 'emf:Project'
      }],
      'definitions': [{
        'id': 'entity'
      }, {
        'id': 'media'
      }]
    };
  }
});