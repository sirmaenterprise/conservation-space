import {ObjectTypeSelect} from 'components/select/object/object-type-select';
import {SelectMocks} from './select-mocks';
import {ModelsService} from 'services/rest/models-service';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseAdapterMock} from '../adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';
import _ from 'lodash';

describe('ObjectTypeSelect', () => {
  var modelsService;
  var objectTypeSelect;
  var scopeMock;

  beforeEach(() => {
    ObjectTypeSelect.prototype.config = undefined;

    scopeMock = mock$scope();

    var getModelsStub = sinon.stub();
    getModelsStub.returns(PromiseStub.resolve(createModel()));

    modelsService = {
      getModels: getModelsStub
    };
    objectTypeSelect = new ObjectTypeSelect(SelectMocks.mockElement(), scopeMock, PromiseAdapterMock.mockAdapter(), modelsService, mockTranslateService());
  });

  afterEach(() => {
    ObjectTypeSelect.prototype.config = undefined;
  });

  it('should fetch data from ModelsService', () => {
    objectTypeSelect.config.classFilter = 'some-filter';

    objectTypeSelect.loadData();

    expect(modelsService.getModels.callCount).to.equal(1);
    expect(modelsService.getModels.getCall(0).args[0]).to.equal(ModelsService.PURPOSE_SEARCH);
    expect(modelsService.getModels.getCall(0).args[4]).to.equal('some-filter');
  });

  it('should use the provided data loader if configured', () => {
    objectTypeSelect.config.dataLoader = sinon.spy();
    objectTypeSelect.loadData();
    expect(modelsService.getModels.called).to.be.false;
    expect(objectTypeSelect.config.dataLoader.calledOnce).to.be.true;
  });

  it('should use external configuration with higher priority than the default', () => {
    objectTypeSelect.config.multiple = false;
    objectTypeSelect.createSelectConfig();
    expect(objectTypeSelect.config.multiple).to.be.false;
  });

  it('should expand object types if there are predefined data', () => {
    let data = {
      text: 'translated',
      id: 'anyObject'
    };
    objectTypeSelect.config.predefinedData = [data];
    let results = createModel();
    objectTypeSelect.expandSelectableTypes(results.models);
    expect(results.models[0]).to.deep.equal(data);
  });

  it('should correctly transform object types and subtypes', () => {
    var result = objectTypeSelect.convertData(createModel().models);

    expect(result.length).to.equal(5);

    // Documents
    expect(result[0].text).to.equal('Document');
    expect(result[0].level).to.equal(1);
    expect(result[0].type).to.equal('class');

    expect(result[1].level).to.equal(2);
    expect(result[1].type).to.equal('definition');
    expect(result[1].definitionId).to.equal(result[1].id);
    expect(result[2].level).to.equal(2);
    expect(result[2].type).to.equal('definition');
    expect(result[2].definitionId).to.equal(result[2].id);

    expect(result[3].text).to.equal('Definitionless Type');
    expect(result[3].type).to.equal('class');

    expect(result[4].id).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag');
    expect(result[4].definitionId).to.equal('tag');
  });

  it('should correctly transform object types model with broken hierarchy', () => {
    var result = objectTypeSelect.convertData(createModelWithBrokenHierarchy());

    expect(result.length).to.equal(1);

    // Documents
    expect(result[0].id).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image');
    expect(result[0].level).to.equal(1);
    expect(result[0].type).to.equal('class');
    expect(result[0].definitionId).to.equal('image');
  });

  it('should prefer definition type for types with single definition if configured', () => {
    objectTypeSelect.config.preferDefinitionType = true;
    var result = objectTypeSelect.convertData(createModel().models);
    expect(result[4].id).to.equal('tag');
  });

  it('should preserve the class URI after the definition type is preferred', () => {
    objectTypeSelect.config.preferDefinitionType = true;
    var result = objectTypeSelect.convertData(createModel().models);
    expect(result[4].uri).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag');
  });

  it('should construct select config after types are resolved', (done) => {
    objectTypeSelect.createSelectConfig().then(() => {
      expect(objectTypeSelect.config.selectConfig).to.exist;
      done();
    }).catch(done);
  });

  it('should publish data with a callback function when it is fetched and converted', (done) => {
    var objectTypes;
    objectTypeSelect.config.publishCallback = (types) => {
      objectTypes = types;
    };

    objectTypeSelect.createSelectConfig().then(() => {
      expect(objectTypes.length).to.equal(5);
      done();
    }).catch(done);
  });

  it('should not configure the select to limit the minimum selections if there are not predefined items or class filters', (done) => {
    objectTypeSelect.createSelectConfig().then(() => {
      expect(objectTypeSelect.config.selectConfig.minimumSelectionLength).to.not.exist;
      done();
    }).catch(done);
  });

  it('should not configure the select to limit the minimum selections if there are predefined items', (done) => {
    objectTypeSelect.config.predefinedData = ['emf:Document'];
    objectTypeSelect.createSelectConfig().then(() => {
      expect(objectTypeSelect.config.selectConfig.minimumSelectionLength).to.not.exist;
      done();
    }).catch(done);
  });

  it('should configure the select to limit the minimum selections if there is class filters', (done) => {
    objectTypeSelect.config.classFilter = ['emf:Document'];
    objectTypeSelect.createSelectConfig().then(() => {
      expect(objectTypeSelect.config.selectConfig.minimumSelectionLength).to.equal(1);
      done();
    }).catch(done);
  });

  it('should create two way binding if ngModel is present', ()=> {
    scopeMock.$watch = sinon.spy();
    objectTypeSelect.bindToModel();
    expect(scopeMock.$watch.callCount).to.equal(2);
  });

  it('should not create two way binding if ngModel is missing', ()=> {
    scopeMock.$watch = sinon.spy();
    objectTypeSelect.ngModel = undefined;
    objectTypeSelect.bindToModel();
    expect(scopeMock.$watch.callCount).to.equal(0);
  });

  it('should update view value if a different one is watched', ()=> {
    objectTypeSelect.config.objectTypes = 'new type';
    objectTypeSelect.ngModel.$setViewValue = sinon.spy(() => {

    });

    objectTypeSelect.ngOnInit();

    objectTypeSelect.$scope.$digest();
    expect(objectTypeSelect.ngModel.$setViewValue.getCall(0).args[0]).to.equal('new type');
  });

  it('should not update view value if the same one is watched', ()=> {
    objectTypeSelect.config.objectTypes = 'old type';
    objectTypeSelect.ngModel.$setViewValue = () => {
    };

    objectTypeSelect.ngOnInit();

    objectTypeSelect.$scope.$digest();
    objectTypeSelect.config.objectTypes = 'old type';
    objectTypeSelect.ngModel.$setViewValue = sinon.spy(() => {

    });
    objectTypeSelect.$scope.$digest();
    expect(objectTypeSelect.ngModel.$setViewValue.callCount).to.equal(0);
  });

  it('should update model value if a different one is watched', ()=> {
    objectTypeSelect.config.objectTypes = 'old type';
    objectTypeSelect.ngModel.$viewValue = 'new type';
    objectTypeSelect.ngModel.$setViewValue = () => {
    };

    objectTypeSelect.ngOnInit();

    objectTypeSelect.$scope.$digest();
    expect(objectTypeSelect.config.objectTypes).to.equal('new type');
  });

  it('should not update model value if the same one is watched', ()=> {
    objectTypeSelect.config.objectTypes = 'old type';
    objectTypeSelect.ngModel.$viewValue = 'old type';
    objectTypeSelect.ngModel.$setViewValue = () => {
    };

    objectTypeSelect.ngOnInit();

    objectTypeSelect.$scope.$digest();
    expect(objectTypeSelect.config.objectTypes).to.equal('old type');
  });

});

function createModel() {
  return {
    "models": [
      {
        "id": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document",
        "label": "Document",
        "type": "class"
      },
      {
        "id": "MS210001",
        "label": "Approval document",
        "type": "definition",
        "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"
      },
      {
        "id": "OT210027",
        "label": "Common document",
        "type": "definition",
        "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"
      },
      {
        "id": "http://www.sirma.com/ontologies/2013/10/definitionLess",
        "label": "Definitionless Type",
        "type": "class"
      },
      {
        "id": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag",
        "label": "Таг",
        "type": "class"
      },
      {
        "id": "tag",
        "label": "Таг",
        "type": "definition",
        "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag"
      }]
  };
}

// Broken hierarchy means that there are parent specified but entries for it are not provided
function createModelWithBrokenHierarchy() {
  return [{
    "id": "image",
    "label": "image",
    "type": "definition",
    "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image"
  }, {
    "id": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image",
    "label": "Image",
    "type": "class",
    "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Media"
  }];
}

function mockTranslateService() {
  return {
    translate: () => {
      return Promise.resolve();
    }
  }
}