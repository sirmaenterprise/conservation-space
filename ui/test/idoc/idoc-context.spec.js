import {IdocContext, InstanceObject, CURRENT_OBJECT_TEMP_ID, CURRENT_OBJECT_LOCK} from 'idoc/idoc-context';
import {InstanceRestService} from 'services/rest/instance-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel, DefinitionModelProperty} from 'models/definition-model';
import {IdocPage} from 'idoc/idoc-page';
import {EmittableObject} from  'common/event-emitter';
import {MODE_PREVIEW, MODE_EDIT} from 'idoc/idoc-constants';
import {PromiseAdapterMock} from '../adapters/angular/promise-adapter-mock';
import {StatusCodes} from 'services/rest/status-codes';

describe('IdocContext', () => {

  describe('isEditMode()', () => {
    it('should return true if the idoc is in edit mode', () => {
      let idocContext = mockIdocContext(null, MODE_EDIT);
      expect(idocContext.isEditMode()).to.be.true;
    });
    it('should return false if the idoc is in preview mode', () => {
      let idocContext = mockIdocContext(null, MODE_PREVIEW);
      expect(idocContext.isEditMode()).to.be.false;
    });
  });

  describe('isPreviewMode()', () => {
    it('should return true if the idoc is in preview mode', () => {
      let idocContext = mockIdocContext(null, MODE_PREVIEW);
      expect(idocContext.isPreviewMode()).to.be.true;
    });
    it('should return false if the idoc is in edit mode', () => {
      let idocContext = mockIdocContext(null, MODE_EDIT);
      expect(idocContext.isPreviewMode()).to.be.false;
    });
  });

  it('skipHttpInterceptor() should return true if there is response with status 404 NOT_FOUND', () => {
    let idocContext = mockIdocContext(null, MODE_PREVIEW);
    expect(idocContext.skipHttpInterceptor({status: StatusCodes.NOT_FOUND})).to.be.true;
    expect(idocContext.skipHttpInterceptor({status: StatusCodes.SERVER_ERROR})).to.be.false;
  });

  describe('applyModelConverters()', () => {
    it('should load and apply registered model converters', () => {
      let converter1 = {
        convert: sinon.stub()
      };
      let converter2 = {
        convert: sinon.stub()
      };
      let idocContext = mockIdocContext(SHARED_OBJECT_ID, MODE_EDIT);
      idocContext.modelConverters = [converter1, converter2];
      let instanceObject = {
        getModels: () => {
          return {
            viewModel: {}
          }
        }
      };
      idocContext.applyModelConverters(instanceObject);
      expect(converter1.convert.calledOnce).to.be.true;
      expect(converter2.convert.calledOnce).to.be.true;
    });
  });

  describe('loadObject()', () => {
    it('should use existing loader if such exists', (done) => {
      let idocContext = mockIdocContext(SHARED_OBJECT_ID, MODE_EDIT);
      idocContext.loaders[SHARED_OBJECT_ID] = new Promise((resolve) => {
        resolve('existing loader result');
      });
      idocContext.loadObject(SHARED_OBJECT_ID).then((result) => {
        expect(idocContext.instanceRestService.load.callCount).to.equals(0);
        expect(result).to.equals('existing loader result');
        done();
      }).catch(done);
    });

    it('should create loader and return current object', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.loadObject(SHARED_OBJECT_ID).then((result) => {
        expect(idocContext.instanceRestService.load.callCount).to.equals(1);
        expect(result).to.be.an.instanceof(InstanceObject);
        expect(result.getId()).to.equals(SHARED_OBJECT_ID);
        expect(result.getThumbnail()).to.equal('thumbnail');
        expect(result.getWriteAllowed()).to.equal(true);
        done();
      }).catch(done);
    });

    it('should load content if loadView is true', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.loadObject(SHARED_OBJECT_ID, true).then((result) => {
        expect(idocContext.instanceRestService.loadView.callCount).to.equals(1);
        expect(idocContext.instanceRestService.loadView.getCall(0).args[0]).to.equals(SHARED_OBJECT_ID);
        expect(result.content).to.equals('idoc content');
        done();
      }).catch(done);
    });

    it('should not load content if loadView is false', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.loadObject(SHARED_OBJECT_ID, false).then((result) => {
        expect(idocContext.instanceRestService.loadView.callCount).to.equals(0);
        expect(result.content).to.be.undefined;
        done();
      }).catch(done);
    });

    it('should load the context path', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.loadObject(SHARED_OBJECT_ID, false).then((object) => {
        expect(idocContext.instanceRestService.loadContextPath.calledOnce).to.be.true;
        expect(object.getContextPath()).to.deep.equal(['emf:123', 'emf:456']);
        done();
      }).catch(done);
    });
  });

  describe('loadObjects()', () => {
    it('should resolve with an array with InstanceObjects', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      sinon.stub(idocContext.instanceRestService, 'loadBatch', (ids) => {
        let data = ids.map((id) => {
          return {
            id: id,
            headers: {'header': 'header value'},
            content: `content${id}`,
            properties: {
              p1: 'newValue1',
              p2: 'newValue2'
            }
          };
        });
        return Promise.resolve({data: data});
      });
      sinon.stub(idocContext.instanceRestService, 'loadModels', (ids) => {
        let data = {};
        ids.forEach((id) => {
          data[id] = {
            validationModel: {
              p1: {
                value: 'value1'
              },
              p2: {
                value: 'value2'
              },
              header: {}
            },
            viewModel: {
              fields: []
            },
            definitionId: `definitionId_${id}`,
            definitionLabel: `definitionLabel_${id}`,
            instanceType: `instanceType_${id}`
          };
        });
        return Promise.resolve({data: data});
      });
      idocContext.loadObjects(['emf:123456', 'emf:999888']).then((result) => {
        expect(result).to.have.length(2);
        expect(result[0]).to.be.instanceof(InstanceObject);
        expect(result[0].id).to.equal('emf:123456');
        // check that instance properties are merged into the model
        expect(result[0].models.validationModel.p1.value).to.equal('newValue1');
        expect(result[0].models.validationModel.p1.defaultValue).to.equal('newValue1');
        expect(result[0].models.validationModel.p2.value).to.equal('newValue2');
        expect(result[0].models.validationModel.p2.defaultValue).to.equal('newValue2');
        expect(result[1]).to.be.instanceof(InstanceObject);
        expect(result[1].id).to.equal('emf:999888');
        done();
      }).catch(done);
    });
  });

  describe('getCurrentObject()', () => {
    it('should load current object if id is provided', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      idocContext.getCurrentObject().then((currentObject) => {
        expect(currentObject).to.be.an.instanceof(InstanceObject);
        expect(currentObject.getId()).to.equals(IDOC_ID);
        expect(sharedObjectsRegistrySpy.callCount).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(IDOC_ID).size).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(IDOC_ID).has(CURRENT_OBJECT_LOCK)).to.be.true;
        done();
      }).catch(done);
    });

    it('should create current object if id is not provided', (done) => {
      let idocContext = mockIdocContext(undefined, MODE_EDIT);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      idocContext.getCurrentObject().then((currentObject) => {
        expect(currentObject).to.be.an.instanceof(InstanceObject);
        expect(currentObject.getId()).to.equal(CURRENT_OBJECT_TEMP_ID);
        expect(sharedObjectsRegistrySpy.callCount).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(CURRENT_OBJECT_TEMP_ID).size).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(CURRENT_OBJECT_TEMP_ID).has(CURRENT_OBJECT_LOCK)).to.be.true;
        done();
      }).catch(done);
    });

    it('should load object\'s view', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.getCurrentObject().then((currentObject) => {
        expect(idocContext.instanceRestService.loadView.callCount).to.equal(1);
        expect(currentObject.content).to.equal('idoc content');
        done();
      }).catch(done);
    });

    it('should navigate to error page if there is an error during the loading', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      sinon.stub(idocContext, 'loadObject', () => {
        return Promise.reject({
          status: StatusCodes.NOT_FOUND
        });
      });
      idocContext.getCurrentObject().then((result) => {
        expect(0).to.equal(1, 'This promise should be rejected.');
      }).catch((error) => {
        expect(idocContext.router.navigate.calledOnce).to.be.true;
        expect(idocContext.router.navigate.getCall(0).args).to.eql(['error', {'key': IdocContext.ERROR_NOT_FOUND_KEY}]);
        done();
      }).then(done, done);
    });

    it('should navigate to error page if the user has no permissions', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      sinon.stub(idocContext, 'loadObject', () => {
        return Promise.reject({
          status: StatusCodes.FORBIDDEN
        });
      });
      idocContext.getCurrentObject().then((result) => {
        expect(0).to.equal(1, 'This promise should be rejected.');
      }).catch((error) => {
        expect(idocContext.router.navigate.calledOnce).to.be.true;
        expect(idocContext.router.navigate.getCall(0).args).to.eql(['error', {'key': IdocContext.FORBIDDEN_KEY}]);
        done();
      }).then(done, done);
    });
  });

  describe('getSharedObject()', () => {
    it('should load shared object', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.getSharedObject(SHARED_OBJECT_ID, null).then((result) => {
        expect(result).to.be.an.instanceof(InstanceObject);
        expect(result.getId()).to.equals(SHARED_OBJECT_ID);
        done();
      }).catch(done);
    });

    it('should throw error if widgetId parameter is undefined', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      expect(() => idocContext.getSharedObject(SHARED_OBJECT_ID)).to.throw(Error);
    });

    it('should not throw error if widgetId parameter is null', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      expect(() => idocContext.getSharedObject(SHARED_OBJECT_ID, null)).to.not.throw(Error);
      expect(sharedObjectsRegistrySpy.callCount).to.equal(0);
    });

    it('should register widget to retrieved object', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      idocContext.getSharedObject(SHARED_OBJECT_ID, 'testWidgetId').then(() => {
        expect(sharedObjectsRegistrySpy.callCount).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(SHARED_OBJECT_ID).size).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get(SHARED_OBJECT_ID).has('testWidgetId')).to.be.true;
        done();
      }).catch(done);
    });
  });

  describe('getSharedObjects()', () => {
    it('should return a promise which resolves with requested objects', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      idocContext.getSharedObjects(['emf:999888', DEFINITION_ID], null).then((result) => {
        expect(result.data).to.have.length(2);
        expect(result.data[0]).to.deep.equal(sharedObjects['emf:999888']);
        expect(result.data[1]).to.deep.equal(sharedObjects[DEFINITION_ID]);
        done();
      }).catch(done);
    });

    it('should throw error if widgetId parameter is undefined', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      expect(() => idocContext.getSharedObjects([SHARED_OBJECT_ID])).to.throw(Error);
    });

    it('should throw error if instance is partially loaded in edit mode', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let config = {params: {properties: ['createdBy']}};
      expect(() => idocContext.getSharedObjects([SHARED_OBJECT_ID], null, false, config)).to.throw(Error);
    });

    it('should not throw error if widgetId parameter is null', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.sharedObjects[SHARED_OBJECT_ID] = {};
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      expect(() => idocContext.getSharedObjects([SHARED_OBJECT_ID], null)).to.not.throw(Error);
      expect(sharedObjectsRegistrySpy.callCount).to.equal(0);
    });

    it('should register widget to retrieved objects', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      idocContext.getSharedObjects(['emf:999888', 'emf:123456'], 'testWidgetId').then(() => {
        expect(sharedObjectsRegistrySpy.callCount).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.size).to.equal(2);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:999888').size).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:999888').has('testWidgetId')).to.be.true;
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:123456').size).to.equal(1);
        expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:123456').has('testWidgetId')).to.be.true;
        done();
      }).catch(done);
    });

    it('should call loadObjects with the ids of not loaded objects (which does not exist in sharedObjects map)', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      let loadObjectsStub = sinon.stub(idocContext, 'loadObjects', () => {
        return Promise.resolve([]);
      });
      idocContext.getSharedObjects(['emf:123456', 'emf:222222', 'emf:333333'], 'testWidgetId').then(() => {
        expect(loadObjectsStub.callCount).to.equal(1);
        expect(loadObjectsStub.getCall(0).args[0]).to.eql(['emf:222222', 'emf:333333']);
        loadObjectsStub.restore();
        done();
      }).catch(done);
    });

    it('should load not loaded objects, to add them to sharedObjects map and to return all requested objects', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      let loadObjectsStub = sinon.stub(idocContext, 'loadObjects', (notLoadedIds) => {
        let result = notLoadedIds.map((notLoadedId) => {
          return createInstanceObject(notLoadedId);
        });
        return Promise.resolve(result);
      });
      idocContext.getSharedObjects(['emf:222222', 'emf:123456', 'emf:333333'], 'testWidgetId').then((result) => {
        expect(result.data).to.have.length(3);
        expect(result.data[0].id).to.equal('emf:222222');
        expect(result.data[1].id).to.equal('emf:123456');
        expect(result.data[2].id).to.equal('emf:333333');
        done();
      }).catch(done);
    });

    it('should handle NOT_FOUND error response when loading objects', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let loadObjectsStub = sinon.stub(idocContext, 'loadObjects', () => {
        return Promise.reject({
          status: StatusCodes.NOT_FOUND
        });
      });
      idocContext.getSharedObjects(['emf:222222', 'emf:123456', 'emf:333333'], 'testWidgetId').then((result) => {
        expect(result.data).to.be.empty;
        expect(result.notFound).to.eql(['emf:222222', 'emf:123456', 'emf:333333']);
        done();
      }).catch(done);
    });

    it('should return proper results if some of the requested objects are not found', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let loadObjectsStub = sinon.stub(idocContext, 'loadObjects', () => {
        return Promise.resolve([createInstanceObject('emf:222222'), createInstanceObject('emf:123456')]);
      });
      idocContext.getSharedObjects(['emf:222222', 'emf:123456', 'emf:333333'], 'testWidgetId').then((result) => {
        expect(result.data).to.have.length(2);
        expect(result.data[0].id).to.equal('emf:222222');
        expect(result.data[1].id).to.equal('emf:123456');
        expect(result.notFound).to.eql(['emf:333333']);
        done();
      }).catch(done);
    });
  });

  it('buildSharedObjectsResult() should produce correct result for both found and not found objects', () => {
    let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
    let sharedObjects = generateSharedObjects();
    idocContext.setSharedObjects(sharedObjects);
    let result = idocContext.buildSharedObjectsResult(['emf:123456', 'emf:999888', 'emf:222222', 'emf:not_found']);
    expect(result.data).to.have.length(2);
    expect(result.data[0].id).to.equal('emf:123456');
    expect(result.data[1].id).to.equal('emf:999888');
    expect(result.notFound).to.eql(['emf:222222', 'emf:not_found']);
  });

  describe('#getAllSharedObjects', () => {
    it('should return all shared objects synchronously if called without parameter', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      sinon.stub(idocContext.sharedObjectsRegistry, 'isRegisteredToAnyWidget', () => {
        return true;
      });
      let allSharedObjects = idocContext.getAllSharedObjects();
      expect(allSharedObjects).to.have.length(3);
      expect(allSharedObjects).to.include(sharedObjects[DEFINITION_ID]);
      expect(allSharedObjects).to.include(sharedObjects['emf:999888']);
    });

    it('should return all cached objects which are registered to any widget', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);
      idocContext.sharedObjectsRegistry.registerWidget('widget:123456', ['emf:123456']);
      let allSharedObjects = idocContext.getAllSharedObjects();
      expect(allSharedObjects).to.have.length(1);
      expect(allSharedObjects[0].getId()).to.equal('emf:123456');
    });

    it('should return only modified objects (plus current object) if onlyModified is true', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.setCurrentObjectId(IDOC_ID);
      let sharedObjects = generateSharedObjects();
      idocContext.setSharedObjects(sharedObjects);

      //validation model is an instance of instance model class.
      let nonModifiedSharedObject = sharedObjects['emf:999888'].models.validationModel.serialize();
      Object.keys(nonModifiedSharedObject).forEach((property) => {
        nonModifiedSharedObject[property].value = nonModifiedSharedObject[property].defaultValue;
      });
      idocContext.sharedObjectsRegistry.registerWidget(IDOC_ID, [IDOC_ID]);
      idocContext.sharedObjectsRegistry.registerWidget('widget:999888', ['emf:999888']);
      idocContext.sharedObjectsRegistry.registerWidget(DEFINITION_ID, [DEFINITION_ID]);

      let allSharedObjects = idocContext.getAllSharedObjects(true);
      expect(allSharedObjects).to.have.length(2);
      expect(allSharedObjects[0].id).to.equal(DEFINITION_ID);
      expect(allSharedObjects[1].id).to.equal(IDOC_ID);
    });
  });

  describe('#updateTempCurrentObjectId', () => {
    it('should remove the object mapped with temp id and store it with real id', () => {
      let idocContext = mockIdocContext(undefined, MODE_EDIT);
      let sharedObjects = {};
      let current = createInstanceObject(CURRENT_OBJECT_TEMP_ID);
      sharedObjects[CURRENT_OBJECT_TEMP_ID] = current;
      sharedObjects['emf:999888'] = createInstanceObject('emf:999888');
      idocContext.setSharedObjects(sharedObjects);
      idocContext.updateTempCurrentObjectId('emf:123456');
      expect(idocContext.sharedObjects[CURRENT_OBJECT_TEMP_ID] === undefined).to.be.true;
      let updated = idocContext.sharedObjects['emf:123456'];
      expect(updated.getId()).to.equal('emf:123456');
    });

    it('should update shared objects registry', () => {
      let idocContext = mockIdocContext(undefined, MODE_EDIT);
      let sharedObjectsRegistrySpy = sinon.spy(idocContext.sharedObjectsRegistry, 'registerWidget');
      let sharedObjects = {};
      let current = createInstanceObject(CURRENT_OBJECT_TEMP_ID);
      sharedObjects[CURRENT_OBJECT_TEMP_ID] = current;
      sharedObjects['emf:999888'] = createInstanceObject('emf:999888');
      idocContext.setSharedObjects(sharedObjects);
      idocContext.updateTempCurrentObjectId('emf:123456');
      expect(sharedObjectsRegistrySpy.callCount).to.equal(1);
      expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:123456').size).to.equal(1);
      expect(idocContext.sharedObjectsRegistry.sharedObjectsRegistry.get('emf:123456').has(CURRENT_OBJECT_LOCK)).to.be.true;
    });
  });

  describe('#mergeObjectsModels()', () => {
    it('should not change the shared object models when no changes are found', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.setSharedObjects({
        'OT210027': createInstanceObject(DEFINITION_ID),
        'emf:999888': createInstanceObject('emf:999888')
      });

      let expected = {
        id: DEFINITION_ID,
        definitionId: null,
        parentId: 'parentId',
        returnUrl: 'returnUrl',
        viewModel: new DefinitionModel({
          fields: [{
            identifier: 'emfObjectProperty',
            uri: 'emf:objectProperty',
            control: {
              identifier: 'PICKER'
            }
          }]
        }),
        validationModel: new InstanceModel({
          'field1': {
            defaultValue: 'value1',
            value: 'value1',
            defaultValueLabel: 'label1',
            valueLabel: 'label2'
          },
          'field2': {
            defaultValue: 'value2',
            value: 'value2'
          },
          'title': {
            defaultValue: 'title',
            value: 'Title'
          },
          'emfObjectProperty': {
            value: [{id: 'emf:123456'}],
            defaultValue: [{id: 'emf:123456'}]
          }
        }),
        path: [
          {
            "compactHeader": "\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>",
            "id": "emf:123456",
            "type": "projectinstance"
          }, {
            "compactHeader": "\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>",
            "id": "emf:234567",
            "type": "documentinstance"
          }
        ]
      };
      idocContext.mergeObjectsModels({});
      expected.definitionId = 'OT210027';
      idocContext.sharedObjects['OT210027'].getModels();
      expected.id = DEFINITION_ID;
      expect(idocContext.sharedObjects['OT210027'].getModels()).to.deep.equal(expected);
      expected.definitionId = 'emf:999888';
      expected.id = 'emf:999888';
      expect(idocContext.sharedObjects['emf:999888'].getModels()).to.deep.equal(expected);
    });

    it('should update shared objects model with changed properties', () => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      idocContext.setSharedObjects({
        'OT210027': createInstanceObject(DEFINITION_ID),
        'emf:999888': createInstanceObject('emf:999888')
      });
      idocContext.mergeObjectsModels({
        'OT210027': {
          models: {
            definitionId: 'OT210027',
            parentId: 'parentId',
            returnUrl: 'returnUrl',
            viewModel: {},
            validationModel: new InstanceModel({
              'field1': {
                defaultValue: 'value1',
                value: '123'
              },
              'field2': {
                defaultValue: 'value2',
                value: 'value2'
              },
              'title': {
                defaultValue: 'title',
                value: 'new Title'
              }
            })
          }
        }
      });
      let expected = {
        id: DEFINITION_ID,
        definitionId: 'OT210027',
        parentId: 'parentId',
        returnUrl: 'returnUrl',
        viewModel:new DefinitionModel({
          fields: [{
            identifier: 'emfObjectProperty',
            uri: 'emf:objectProperty',
            control: {
              identifier: 'PICKER'
            }
          }]
        }),
        validationModel: new InstanceModel({
          'field1': {
            defaultValue: 'value1',
            value: '123',
            defaultValueLabel: 'label1',
            valueLabel: 'label2'
          },
          'field2': {
            defaultValue: 'value2',
            value: 'value2'
          },
          'title': {
            defaultValue: 'title',
            value: 'new Title'
          },
          'emfObjectProperty': {
            value: [{id: 'emf:123456'}],
            defaultValue: [{id: 'emf:123456'}]
          }
        }),
        path: [
          {
            "compactHeader": "\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>",
            "id": "emf:123456",
            "type": "projectinstance"
          }, {
            "compactHeader": "\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>",
            "id": "emf:234567",
            "type": "documentinstance"
          }
        ]
      };
      expect(idocContext.sharedObjects['OT210027'].getModels()).to.deep.equal(expected);
    });
  });

  it('reloadObjectDetails() should update current object details and fire object updated event', (done) => {
    let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
    let id = 'emf:999888';
    let instanceObject = createInstanceObject(id);
    instanceObject.models.validationModel['default_header'] = {};
    instanceObject.models.validationModel['compact_header'] = {};
    instanceObject.models.validationModel['breadcrumb_header'] = {};

    idocContext.setSharedObjects({
      'OT210027': createInstanceObject(DEFINITION_ID),
      'emf:999888': instanceObject
    });
    let headers = {
      'default_header': 'default_header',
      'compact_header': 'compact_header',
      'breadcrumb_header': 'breadcrumb_header'
    };
    let contextPath = ['parent', 'child'];
    idocContext.instanceRestService = {
      load: () => {
        return Promise.resolve({
          data: {
            headers: headers
          }
        });
      },
      loadModel: () => {
        return Promise.resolve({
          data: {
            validationModel: generateModels(id).validationModel
          }
        });
      },
      loadContextPath: () => {
        return Promise.resolve({data: contextPath});
      }
    };
    idocContext.eventbus = new Eventbus();
    let spyPublish = sinon.spy(idocContext.eventbus, 'publish');

    idocContext.reloadObjectDetails('emf:999888').then((sharedObject) => {
      idocContext.getSharedObject(['emf:999888'], null).then((updatedObject) => {
        expect(updatedObject.getHeaders()).to.deep.equal(headers);
        expect(updatedObject.getContextPath()).to.eql(contextPath);
        expect(spyPublish.callCount).to.equal(1);
        done();
      }).catch(done);
    });
  });

  it('revertAllChanges should call revertChanges of all shared objects', () => {
    let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
    let id = 'emf:999888';
    idocContext.setSharedObjects({
      'OT210027': createInstanceObject(DEFINITION_ID),
      'emf:999888': createInstanceObject(id)
    });
    idocContext.sharedObjectsRegistry.registerWidget('widget:123456', ['OT210027', 'emf:999888']);
    let revertChangesSpies = idocContext.getAllSharedObjects().map((sharedObject) => {
      return sinon.spy(sharedObject, 'revertChanges');
    });
    idocContext.revertAllChanges();
    expect(revertChangesSpies).to.have.length(2);
    revertChangesSpies.forEach((revertChangesSpy) => {
      expect(revertChangesSpy.callCount).to.equal(1);
    });
  });

  it('isPartiallyLoaded should return true if config contains properties', () => {
    let idocContext = mockIdocContext(IDOC_ID, MODE_PREVIEW);
    let config = {params: {properties: ['createdBy']}};
    expect(idocContext.isPartiallyLoaded(config)).to.be.true;
  });

  it('should merge instance models correct', () => {
    let idocContext = mockIdocContext(IDOC_ID, MODE_PREVIEW);
    let sharedInstance = {models: {validationModel: {}, viewModel: {fields:[]}}};
    let loadedInstance = {models: {validationModel: {}, viewModel: {fields:[]}}};
    sharedInstance.models.validationModel['createdBy'] = {
      value: 'test user',
      defaultValue: 'test user'
    };
    sharedInstance.models.viewModel.fields.push({
      'identifier': 'createdBy',
      'dataType': 'text',
      'displayType': 'READ_ONLY',
      'isDataProperty': true
    });
    sharedInstance.models.validationModel = new InstanceModel(sharedInstance.models.validationModel);
    sharedInstance.models.viewModel = new DefinitionModel(sharedInstance.models.viewModel);

    loadedInstance.models.validationModel['modifiedBy'] = {
      value: 'test user',
      defaultValue: 'test user'
    };
    loadedInstance.models.viewModel.fields.push({
      'identifier': 'modifiedBy',
      'dataType': 'text',
      'displayType': 'READ_ONLY',
      'isDataProperty': true
    });
    loadedInstance.models.validationModel = new InstanceModel(loadedInstance.models.validationModel);
    loadedInstance.models.viewModel = new DefinitionModel(loadedInstance.models.viewModel);
    idocContext.mergeInstanceModels(sharedInstance, loadedInstance);

    let expectedViewModel = [
      {
        'identifier': 'createdBy',
        'dataType': 'text',
        'displayType': 'READ_ONLY',
        'isDataProperty': true
      },
      {
        'identifier': 'modifiedBy',
        'dataType': 'text',
        'displayType': 'READ_ONLY',
        'isDataProperty': true
      }
    ];

    let expectedValidationModel = {
      createdBy: {value: 'test user', defaultValue: 'test user'},
      modifiedBy: {value: 'test user', defaultValue: 'test user'}
    };
    expect(sharedInstance.models.viewModel.serialize().fields).to.eql(expectedViewModel);
    expect(sharedInstance.models.validationModel.serialize()).to.eql(expectedValidationModel);
  });

  function mockIdocContext(id, mode) {
    let instanceRestService = new InstanceRestService();
    sinon.stub(instanceRestService, 'load', () => {
      if (!id) {
        return Promise.reject('Error during loading!');
      }
      return new Promise((resolve) => {
        resolve({
          data: {
            definitionId: DEFINITION_ID,
            properties: {},
            thumbnailImage: 'thumbnail',
            writeAllowed: true
          }
        });
      });
    });

    let loadModel = () => {
      return new Promise((resolve) => {
        let models = generateModels(DEFINITION_ID);
        resolve({
          data: {
            validationModel: models.validationModel,
            viewModel: models.viewModel
          }
        });
      });
    };

    sinon.stub(instanceRestService, 'loadModel', loadModel);

    sinon.stub(instanceRestService, 'loadView', () => {
      return new Promise((resolve) => {
        resolve({
          data: 'idoc content'
        });
      });
    });

    sinon.stub(instanceRestService, 'loadContextPath', () => {
      return Promise.resolve({data: ['emf:123', 'emf:456']});
    });

    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', JSON.stringify(generateModels(DEFINITION_ID)));

    var router = {
      navigate: sinon.stub()
    };
    var eventbus = {};

    var pluginsService = {
      loadPluginServiceModules: () => {
        return PromiseAdapterMock.mockImmediateAdapter().resolve([]);
      }
    };

    return new IdocContext(id, mode, instanceRestService, sessionStorageService,
      PromiseAdapterMock.mockAdapter(), eventbus, router, pluginsService);
  }
});

describe('InstanceObject', () => {

  describe('getContextPathIds()', () => {
    it('should convert context path to list of identifiers', () => {
      var models = generateModels(DEFINITION_ID);
      let instanceObject = new InstanceObject(IDOC_ID, models);
      instanceObject.setContextPath(models.path);
      let path = instanceObject.getContextPathIds();
      expect(path).to.eql(['emf:123456', 'emf:234567']);
    });
  });

  describe('setPropertiesValue()', () => {

    var instanceObject;
    beforeEach(() => {
      var validationModel = {};
      var viewModel = {
        fields: []
      };
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      addFieldValidationModel(validationModel, 'status', 'DRAFT', '');
      instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
    });

    it('should report being persisted if its id is not the surrogate temp id', () => {
      expect(instanceObject.isPersisted()).to.be.true;
    });

    it('should not report being persisted if its id is the surrogate temp id', () => {
      instanceObject.id = CURRENT_OBJECT_TEMP_ID;
      expect(instanceObject.isPersisted()).to.be.false;
    });

    it('should not report being persisted if have no id set', () => {
      instanceObject.id = null;
      expect(instanceObject.isPersisted()).to.be.false;
    });

    it('should not update the validation model if no properties are given', () => {
      instanceObject.setPropertiesValue();
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['title'].value).to.equals('title1');
      expect(instanceObject.models.validationModel['identifier'].value).to.equals('');
      expect(instanceObject.models.validationModel['status'].value).to.equals('DRAFT');
    });

    it('should not update the validation model for properties that are missing in it', () => {
      instanceObject.setPropertiesValue({
        'type': 'CASE01'
      });
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['type']).to.not.exist;
    });

    it('should update the validation model with the provided properties', () => {
      instanceObject.setPropertiesValue({
        'title': 'new-title',
        'identifier': '456'
      });
      expect(Object.keys(instanceObject.models.validationModel.serialize()).length).to.equals(3);
      expect(instanceObject.models.validationModel['title'].value).to.equals('new-title');
      expect(instanceObject.models.validationModel['identifier'].value).to.equals('456');
      expect(instanceObject.models.validationModel['status'].value).to.equals('DRAFT');
    });
  });

  describe('getChangeset', () => {
    it('should generate correct change set', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      addFieldValidationModel(validationModel, 'status', 'DRAFT', '');
      addFieldValidationModel(validationModel, 'createdOn', '12.12.2012', '12.12.2012');
      addFieldValidationModel(validationModel, 'emfObjectProperty', [{id: 'emf:123456'}], []);
      addFieldValidationModel(validationModel, 'emfObjectPropertyUnchanged', [{id: 'emf:999888'}, {id: 'emf:123456'}], [{id: 'emf:999888'}, {id: 'emf:123456'}]);
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emfObjectProperty', 'PICKER', 'emf:objectProperty', false);
      addFieldViewModel(viewModel, 'emfObjectPropertyUnchanged', 'PICKER', 'emf:objectPropertyUnchanged', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      instanceObject.models.validationModel.title.value = 'modified value';
      instanceObject.models.viewModel.identifier = {dataType : ''};
      let result = instanceObject.getChangeset();

      // old and new value differs
      expect(result).to.have.property('title', 'modified value');
      // value removed probably from user trough the UI
      expect(result).to.have.property('identifier', null);
      // has new value and no default
      expect(result).to.have.property('status', 'DRAFT');
      // not modified: should be missing from changeset
      expect(result).to.not.have.property('createdOn');
      expect(result).to.have.property('emfObjectProperty');
      expect(result['emfObjectProperty']).to.eql(['emf:123456']);
      expect(result).to.not.have.property('emfObjectPropertyUnchanged');
    });

    it('should return unmodified values if forDraft is true', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'emfObjectProperty', [{id: 'emf:123456'}], []);
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emfObjectProperty', 'PICKER', 'emf:objectProperty', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      let result = instanceObject.getChangeset(true);
      expect(result).to.have.property('emfObjectProperty');
      expect(result['emfObjectProperty']).to.eql([{id: 'emf:123456'}]);
    });
  });

  describe('isChanged', () => {
    it('should return true if there are any changes in the model', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', 'title1', 'title1');
      addFieldValidationModel(validationModel, 'identifier', '', '123');
      let viewModel = {
        fields: []
      };
      addFieldViewModel(viewModel, 'emf:objectProperty', 'PICKER', false);
      addFieldViewModel(viewModel, 'emf:objectPropertyUnchanged', 'PICKER', false);
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      expect(instanceObject.isChanged()).to.be.true;
    });

    it('should return false if value is null and default value is undefined', () => {
      let validationModel = {};
      addFieldValidationModel(validationModel, 'title', null, undefined);
      let viewModel = {
        fields: []
      };
      let instanceObject = new InstanceObject(IDOC_ID, {validationModel, viewModel});
      expect(instanceObject.isChanged()).to.be.false;
    });
  });

  it('isNil should return true for null or undefined value', () => {
    expect(InstanceObject.isNil(null)).to.be.true;
    expect(InstanceObject.isNil(undefined)).to.be.true;
    expect(InstanceObject.isNil('')).to.be.false;
    expect(InstanceObject.isNil(false)).to.be.false;
    expect(InstanceObject.isNil(0)).to.be.false;
    expect(InstanceObject.isNil('test')).to.be.false;
    expect(InstanceObject.isNil([])).to.be.false;
  });

  describe('convertPropertyValues()', () => {
    it('should convert values for object property', () => {
      let propertyViewModel = {
        isDataProperty: false,
        control: {
          identifier: 'PICKER'
        }
      };
      let propertyValidationModel = {
        value: [{
          id: 'emf:123456'
        }],
        defaultValue: [{
          id: 'emf:123456'
        }, {
          id: 'emf:999888'
        }]
      };
      let convertedValues = InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);
      expect(convertedValues.value).to.eql(['emf:123456']);
      expect(convertedValues.defaultValue).to.eql(['emf:123456', 'emf:999888']);
    });

    it('should return values as is for data property', () => {
      let propertyViewModel = {
        isDataProperty: true
      };
      let propertyValidationModel = {
        value: ['value1'],
        defaultValue: ['value1', 'value2']
      };
      let convertedValues = InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);
      expect(convertedValues.value).to.eql(['value1']);
      expect(convertedValues.defaultValue).to.eql(['value1', 'value2']);
    });
  });

  describe('setIncomingPropertyValue', () => {
    it('should convert properly codelist single field value and set it into validation model', () => {
      let viewModel = {
        codelist: 500
      };
      let validationModel = { };
      let newValue = {
        id: 'codeValue',
        text: 'codeLabel'
      };

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.equals('codeValue');
      expect(validationModel.valueLabel).to.equals('codeLabel');
    });

    it('should convert properly codelist multi field value and set it into validation model', () => {
      let viewModel = {
        codelist: 500
      };
      let validationModel = { };
      let newValue = [{
        id: 'codeValue1',
        text: 'codeLabel1'
      }, {
        id: 'codeValue2',
        text: 'codeLabel2'
      }];

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.eql(['codeValue1', 'codeValue2']);
      expect(validationModel.valueLabel).to.equals('codeLabel1, codeLabel2');
    });

    it('should set normal field value into validation model as is', () => {
      let viewModel = { };
      let validationModel = { };
      let newValue = 'simple value';

      InstanceObject.setIncomingPropertyValue(viewModel, validationModel, newValue);
      expect(validationModel.value).to.equals('simple value');
      expect(validationModel.valueLabel).to.be.undefined;
    });
  });

  it('isObjectProperty should return correct result', () => {
    let fieldViewModelForObjectProperty = {
      identifier: 'objectProperty',
      isDataProperty: false
    };
    expect(InstanceObject.isObjectProperty(fieldViewModelForObjectProperty)).to.be.true;
    let fieldViewModelForDataProperty = {
      identifier: 'dataProperty',
      isDataProperty: true
    };
    expect(InstanceObject.isObjectProperty(fieldViewModelForDataProperty)).to.be.false;
  });

  it('isCodelistProperty shoud return correct result', () => {
    let fieldViewModelForCodeListProperty = {
      codelist: 500
    };
    expect(InstanceObject.isCodelistProperty(fieldViewModelForCodeListProperty)).to.be.true;
    expect(InstanceObject.isCodelistProperty({})).to.be.false;
  });

  it('formatObjectPropertyValue should properly convert relation raw values', () => {
    let objectPropertyRawValues = [
      {
        id: 'emf:123456',
        properties: {}
      }, {
        id: 'emf:999888',
        instanceType: 'documentInstance'
      }
    ];

    expect(InstanceObject.formatObjectPropertyValue(objectPropertyRawValues)).to.eql(['emf:123456', 'emf:999888']);
    expect(InstanceObject.formatObjectPropertyValue(undefined)).to.eql([]);
  });

  it('mergePropertiesIntoModel() should properly update validation model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.mergePropertiesIntoModel({'field1': 'value to merge', 'emfObjectProperty': ['emf:123456']});
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'value to merge');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValue', 'value to merge');
    expect(instanceObject.models.validationModel.field1).to.have.property('valueLabel', 'label2');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValueLabel', 'label2');

    expect(instanceObject.models.validationModel['emfObjectProperty'].value).to.eql(['emf:123456']);
    expect(instanceObject.models.validationModel['emfObjectProperty'].defaultValue).to.eql(['emf:123456']);
  });

  it('mergePropertiesIntoModel() should also add properties which does not exist in the model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.mergePropertiesIntoModel({'nonModelField': 'nonModelField value'});
    expect(instanceObject.models.validationModel.nonModelField).to.have.property('value', 'nonModelField value');
    expect(instanceObject.models.validationModel.nonModelField).to.have.property('defaultValue', 'nonModelField value');
  });

  it('mergeHeadersIntoModel() should add all available headers in the model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.models.validationModel['breadcrumb_header'] = {};
    instanceObject.models.validationModel['default_header'] = {};
    instanceObject.mergeHeadersIntoModel({
      'breadcrumb_header': 'breadcrumb_header_string',
      'default_header': 'default_header_string'
    });
    expect(instanceObject.models.validationModel['breadcrumb_header'].value).to.equal('breadcrumb_header_string');
    expect(instanceObject.models.validationModel['breadcrumb_header'].value).to.eql(instanceObject.models.validationModel['breadcrumb_header'].defaultValue);
    expect(instanceObject.models.validationModel['default_header'].value).to.equal('default_header_string');
    expect(instanceObject.models.validationModel['default_header'].value).to.eql(instanceObject.models.validationModel['default_header'].defaultValue);
  });

  it('revertChanges() should properly update validation model', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.models.validationModel.field1.value = 'modified value';
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'modified value');
    instanceObject.revertChanges();
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'value1');
    expect(instanceObject.models.validationModel.field1).to.have.property('valueLabel', 'label1');
  });

  it('revertChanges() should clone defaultValue instead of directly assigning it to value', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.models.validationModel['emfObjectProperty'].value = [{id: 'emf:999888'}, {id: 'emf:999777'}];
    instanceObject.revertChanges();
    expect(instanceObject.models.validationModel['emfObjectProperty'].value).to.have.length(1);
    // value and default value should not reference to the same object
    expect(instanceObject.models.validationModel['emfObjectProperty'].value === instanceObject.models.validationModel['emfObjectProperty'].defaultValue).to.be.false;
    expect(instanceObject.models.validationModel['emfObjectProperty'].value[0]).to.have.property('id', 'emf:123456');
  });

  it('revertChanges() should not add undefined value to property of type array', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    instanceObject.models.validationModel['fieldWithUndefinedDefaultValue'].value = [{id: 'emf:999888'}, {id: 'emf:999777'}];
    instanceObject.revertChanges();
    expect(instanceObject.models.validationModel['fieldWithUndefinedDefaultValue'].value).to.have.length(0);
  });

  it('mergeModelIntoModel() should properly update validation model ', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    let updatedValidationModel = generateModels(DEFINITION_ID).validationModel;
    updatedValidationModel.field1.value = 'updated value 1';
    instanceObject.mergeModelIntoModel(updatedValidationModel);
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'updated value 1');
  });

  it('updateLocalModel() should update model values and default values', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    let updatedValidationModel = generateModels(DEFINITION_ID).validationModel;
    updatedValidationModel.field1.value = 'updated value 1';
    instanceObject.updateLocalModel(updatedValidationModel);
    expect(instanceObject.models.validationModel.field1).to.have.property('value', 'updated value 1');
    expect(instanceObject.models.validationModel.field1).to.have.property('defaultValue', 'updated value 1');
  });

  it('getPropertyValue() should return property value if such property exists and has a value', () => {
    let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
    expect(instanceObject.getPropertyValue('title')).to.equals('Title');
    expect(instanceObject.getPropertyValue('non-existing-property')).to.be.undefined;
  });

  it('isVersion should return true if property isVersion is true', () => {
    let models = {
      validationModel: {
        isVersion: {
          value: true
        }
      }
    };
    let instanceObject = new InstanceObject(IDOC_ID, models);
    expect(instanceObject.isVersion()).to.be.true;
  });

  describe('hasMandatory ', () => {

    it('no mandatory properties', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModels(DEFINITION_ID));
      expect(instanceObject.hasMandatory()).to.be.false;
    });

    it('with mandatory properties', () => {
      let instanceObject = new InstanceObject(IDOC_ID, generateModelsForValidation(DEFINITION_ID));
      expect(instanceObject.hasMandatory()).to.be.true;
    });

    it('checkMandatory ', () => {
      let properties = {
        isMandatory: true
      };
      let instanceObject = new InstanceObject(IDOC_ID, generateModelsForValidation(DEFINITION_ID));
      expect(instanceObject.checkMandatory(properties)).to.be.true;
      let fancyProperties = {
        fields: [{
          isMandatory: false
        }, {
          isMandatory: true
        }]
      };
      expect(instanceObject.checkMandatory(fancyProperties)).to.be.true;
      let fancyPropertiesFalse = {
        fields: [{
          isMandatory: false
        }, {
          isMandatory: false
        }]
      };
      expect(instanceObject.checkMandatory(fancyPropertiesFalse)).to.be.false;
    });
  });
});

const IDOC_ID = 'emf:123456';
const SHARED_OBJECT_ID = 'emf:shared';
const DEFINITION_ID = 'OT210027';

function addFieldValidationModel(model, name, value, defaultValue) {
  model[name] = {
    value: value,
    defaultValue: defaultValue
  }
}

function addFieldViewModel(model, identifier, controlIdentifier, uri, isDataProperty) {
  let field = {
    identifier: identifier,
    uri: uri,
    isDataProperty
  };
  if (controlIdentifier) {
    field.control = {
      identifier: controlIdentifier
    };
  }
  model.fields.push(field);
}

function generateModels(id) {
  return {
    definitionId: id,
    parentId: 'parentId',
    returnUrl: 'returnUrl',
    viewModel: {
      fields: [{
        identifier: 'emfObjectProperty',
        uri: 'emf:objectProperty',
        control: {
          identifier: 'PICKER'
        }
      }]
    },
    validationModel: {
      'field1': {
        defaultValue: 'value1',
        value: 'value1',
        defaultValueLabel: 'label1',
        valueLabel: 'label2'
      },
      'field2': {
        defaultValue: 'value2',
        value: 'value2'
      },
      'title': {
        defaultValue: 'title',
        value: 'Title'
      },
      'emfObjectProperty': {
        value: [{id: 'emf:123456'}],
        defaultValue: [{id: 'emf:123456'}]
      },
      'fieldWithUndefinedDefaultValue': {
        value: [{id: 'emf:123456'}],
        defaultValue: undefined
      }
    },
    path: [
      {
        "compactHeader": "\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>",
        "id": "emf:123456",
        "type": "projectinstance"
      }, {
        "compactHeader": "\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>",
        "id": "emf:234567",
        "type": "documentinstance"
      }
    ]
  };
}


function generateModelsForValidation(id) {
  return {
    definitionId: id,
    parentId: 'parentId',
    returnUrl: 'returnUrl',
    viewModel: {
      fields: [{
        identifier: 'emfObjectProperty',
        uri: 'emf:objectProperty',
        isMandatory : true,
        control: {
          identifier: 'PICKER'
        }
      }]
    },
    validationModel: {
      'field1': {
        defaultValue: 'value1',
        value: 'value1',
        defaultValueLabel: 'label1',
        valueLabel: 'label2'
      },
      'field2': {
        defaultValue: 'value2',
        value: 'value2'
      },
      'title': {
        defaultValue: 'title',
        value: 'Title'
      },
      'emfObjectProperty': {
        value: [{id: 'emf:123456'}],
        defaultValue: [{id: 'emf:123456'}]
      },
      'fieldWithUndefinedDefaultValue': {
        value: [{id: 'emf:123456'}],
        defaultValue: undefined
      }
    },
    path: [
      {
        "compactHeader": "\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>",
        "id": "emf:123456",
        "type": "projectinstance"
      }, {
        "compactHeader": "\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>",
        "id": "emf:234567",
        "type": "documentinstance"
      }
    ]
  };
}

function createInstanceObject(id) {
  return new InstanceObject(id, generateModels(id), 'content');
}

function generateSharedObjects() {
  let sharedObjects = {};
  sharedObjects[DEFINITION_ID] = createInstanceObject(DEFINITION_ID);
  sharedObjects['emf:999888'] = createInstanceObject('emf:999888');
  sharedObjects['emf:123456'] = createInstanceObject('emf:123456');

  return sharedObjects;
}
