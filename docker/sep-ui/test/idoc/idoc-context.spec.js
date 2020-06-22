import {IdocContext, CURRENT_OBJECT_LOCK} from 'idoc/idoc-context';
import {InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {InstanceRestService} from 'services/rest/instance-service';
import {SessionStorageService} from 'services/storage/session-storage-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {MODE_PREVIEW, MODE_EDIT} from 'idoc/idoc-constants';
import {PluginsService} from 'services/plugin/plugins-service';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseStub} from 'test/promise-stub';
import {Router} from 'adapters/router/router';
import {stub} from 'test/test-utils';

const IDOC_ID = 'emf:123456';
const SHARED_OBJECT_ID = 'emf:shared';
const DEFINITION_ID = 'OT210027';

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
          };
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
      idocContext.loaders[SHARED_OBJECT_ID] = PromiseStub.promise((resolve) => {
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
            id,
            headers: {'header': 'header value'},
            content: `content${id}`,
            properties: {
              p1: 'newValue1',
              p2: 'newValue2'
            }
          };
        });
        return PromiseStub.resolve({data});
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
        return PromiseStub.resolve({data});
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
        return PromiseStub.reject({
          status: StatusCodes.NOT_FOUND
        });
      });
      idocContext.getCurrentObject().then(() => {
        expect(0).to.equal(1, 'This promise should be rejected.');
      }).catch(() => {
        expect(idocContext.router.navigate.calledOnce).to.be.true;
        expect(idocContext.router.navigate.getCall(0).args).to.eql(['error', {'key': IdocContext.ERROR_NOT_FOUND_KEY}, {location: 'replace'}]);
        done();
      }).then(done, done);
    });

    it('should navigate to error page if the user has no permissions', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_EDIT);
      sinon.stub(idocContext, 'loadObject', () => {
        return PromiseStub.reject({
          status: StatusCodes.FORBIDDEN
        });
      });
      idocContext.getCurrentObject().then(() => {
        expect(0).to.equal(1, 'This promise should be rejected.');
      }).catch(() => {
        expect(idocContext.router.navigate.calledOnce).to.be.true;
        expect(idocContext.router.navigate.getCall(0).args).to.eql(['error', {'key': IdocContext.FORBIDDEN_KEY}, {location: 'replace'}]);
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
      expect(() => idocContext.getSharedObject(SHARED_OBJECT_ID, null)).to.not.throw(Error);
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
      idocContext.sharedObjects[SHARED_OBJECT_ID] = stub(InstanceObject);
      expect(() => idocContext.getSharedObjects([SHARED_OBJECT_ID], null)).to.not.throw(Error);
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
        return PromiseStub.resolve([]);
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
      sinon.stub(idocContext, 'loadObjects', (notLoadedIds) => {
        let result = notLoadedIds.map((notLoadedId) => {
          return createInstanceObject(notLoadedId);
        });
        return PromiseStub.resolve(result);
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
      sinon.stub(idocContext, 'loadObjects', () => {
        return PromiseStub.reject({
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
      sinon.stub(idocContext, 'loadObjects', () => {
        return PromiseStub.resolve([createInstanceObject('emf:222222'), createInstanceObject('emf:123456')]);
      });
      idocContext.getSharedObjects(['emf:222222', 'emf:123456', 'emf:333333'], 'testWidgetId').then((result) => {
        expect(result.data).to.have.length(2);
        expect(result.data[0].id).to.equal('emf:222222');
        expect(result.data[1].id).to.equal('emf:123456');
        expect(result.notFound).to.eql(['emf:333333']);
        done();
      }).catch(done);
    });

    it('should set proper model to changed shared objects when shouldReload is set to true', (done) => {
      let idocContext = mockIdocContext(IDOC_ID, MODE_PREVIEW);
      let sharedObjects = generateSharedObjects();
      sharedObjects[DEFINITION_ID].getModels().validationModel.field1.value = 'old value';

      sharedObjects[DEFINITION_ID].setShouldReload(true);
      sharedObjects['emf:999888'].setShouldReload(true);
      sharedObjects['emf:123456'].setShouldReload(true);

      sinon.stub(idocContext, 'loadObjects', () => {
        return PromiseStub.resolve([createInstanceObject(DEFINITION_ID), createInstanceObject('emf:999888'), createInstanceObject('emf:123456')]);
      });

      idocContext.getSharedObjects([DEFINITION_ID, 'emf:999888', 'emf:123456'], 'testWidgetId').then((result) => {
        expect(result.data).to.have.length(3);
        expect(result.data[0].id).to.equal(DEFINITION_ID);
        expect(result.data[1].id).to.equal('emf:999888');
        expect(result.data[2].id).to.equal('emf:123456');

        expect(result.data[0].models.validationModel.field1).to.have.property('value', 'value1');
        expect(result.data[0].models.validationModel.field1).to.have.property('defaultValue', 'value1');

        expect(result.data[0].getShouldReload()).to.be.false;
        expect(result.data[1].getShouldReload()).to.be.false;
        expect(result.data[2].getShouldReload()).to.be.false;

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
      sharedObjects[CURRENT_OBJECT_TEMP_ID] = createInstanceObject(CURRENT_OBJECT_TEMP_ID);
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
      sharedObjects[CURRENT_OBJECT_TEMP_ID] = createInstanceObject(CURRENT_OBJECT_TEMP_ID);
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
            compactHeader: '\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>',
            id: 'emf:123456',
            type: 'projectinstance'
          }, {
            compactHeader: '\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>',
            id: 'emf:234567',
            type: 'documentinstance'
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
            compactHeader: '\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>',
            id: 'emf:123456',
            type: 'projectinstance'
          }, {
            compactHeader: '\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>',
            id: 'emf:234567',
            type: 'documentinstance'
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
        return PromiseStub.resolve({
          data: {
            headers,
            writeAllowed: true
          }
        });
      },
      loadModel: () => {
        return PromiseStub.resolve({
          data: {
            validationModel: generateModels(id).validationModel
          }
        });
      },
      loadContextPath: () => {
        return PromiseStub.resolve({data: contextPath});
      }
    };
    idocContext.eventbus = new Eventbus();
    let spyPublish = sinon.spy(idocContext.eventbus, 'publish');

    idocContext.reloadObjectDetails('emf:999888').then(() => {
      idocContext.getSharedObject(['emf:999888'], null).then((updatedObject) => {
        expect(updatedObject.getHeaders()).to.deep.equal(headers);
        expect(updatedObject.getContextPath()).to.eql(contextPath);
        expect(updatedObject.writeAllowed).to.be.true;
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
    let sharedInstance = {
      models: {validationModel: {}, viewModel: {fields: []}},
      getPartiallyLoaded: sinon.spy(), setPartiallyLoaded: sinon.spy(), getHeaders: sinon.spy(), setHeaders: sinon.spy()
    };
    let loadedInstance = {
      models: {validationModel: {}, viewModel: {fields: []}},
      getPartiallyLoaded: sinon.spy(), setPartiallyLoaded: sinon.spy(), getHeaders: sinon.spy(), setHeaders: sinon.spy()
    };
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
        'identifier': 'modifiedBy',
        'dataType': 'text',
        'displayType': 'READ_ONLY',
        'isDataProperty': true
      },
      {
        'identifier': 'createdBy',
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

  describe('getRootContextWithReadAccess()', () => {
    it('should not get any root context if there is no path', () => {
      expect(IdocContext.getRootContextWithReadAccess()).to.not.exist;
    });

    it('should return the current context if it is the same', () => {
      let path = [{
        'id': 'child',
        'readAllowed': true
      }];
      let root = IdocContext.getRootContextWithReadAccess(path);
      expect(root).to.exist;
      expect(root.id).to.equal('child');
    });

    it('should return the root context if it has read access', () => {
      let expected = {
        'id': 'root',
        'readAllowed': true
      };
      let path = [expected, {
        'id': 'parent',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }];
      expect(IdocContext.getRootContextWithReadAccess(path)).to.deep.equal(expected);
    });

    it('should return the furthest context that has a read access', () => {
      let expected = {
        'id': 'parent',
        'readAllowed': true
      };
      let path = [{
        'id': 'root',
        'readAllowed': false
      }, expected, {
        'id': 'child',
        'readAllowed': true
      }];
      expect(IdocContext.getRootContextWithReadAccess(path)).to.deep.equal(expected);
    });
  });

  describe('getRootContextWithReadAccessInverted', () => {
    it('should not get any root context if there is no path', () => {
      expect(IdocContext.getRootContextWithReadAccessInverted()).to.not.exist;
    });

    it('should return the current context if it is the same', () => {
      let path = [{
        'id': 'child',
        'readAllowed': true
      }];
      let root = IdocContext.getRootContextWithReadAccessInverted(path);
      expect(root).to.exist;
      expect(root.id).to.equal('child');
    });

    it('should return the root context if it has read access', () => {
      let expected = {
        'id': 'root',
        'readAllowed': true
      };
      let path = [expected, {
        'id': 'parent',
        'readAllowed': true
      }, {
        'id': 'child',
        'readAllowed': true
      }];
      expect(IdocContext.getRootContextWithReadAccessInverted(path)).to.deep.equal(expected);
    });

    it('should return the furthest context that has a read access', () => {
      let expected = {
        'id': 'parent',
        'readAllowed': true
      };
      let path = [{
        'id': 'root',
        'readAllowed': false
      }, expected, {
        'id': 'child',
        'readAllowed': true
      }];
      expect(IdocContext.getRootContextWithReadAccessInverted(path)).to.deep.equal(expected);
    });
  });

  function mockIdocContext(id, mode) {
    let instanceRestService = new InstanceRestService();
    sinon.stub(instanceRestService, 'load', () => {
      if (!id) {
        return PromiseStub.reject('Error during loading!');
      }
      return PromiseStub.promise((resolve) => {
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
      return PromiseStub.promise((resolve) => {
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
      return PromiseStub.promise((resolve) => {
        resolve({
          data: 'idoc content'
        });
      });
    });

    sinon.stub(instanceRestService, 'loadContextPath', () => {
      return PromiseStub.resolve({data: ['emf:123', 'emf:456']});
    });

    let sessionStorageService = new SessionStorageService(new WindowAdapter(window));
    sessionStorageService.set('models', JSON.stringify(generateModels(DEFINITION_ID)));

    let router = stub(Router);
    let eventbus = stub(Eventbus);

    let pluginsService = stub(PluginsService);
    pluginsService.loadPluginServiceModules.returns(PromiseStub.resolve([]));

    return new IdocContext(id, mode, instanceRestService, sessionStorageService,
      PromiseStub, eventbus, router, pluginsService);
  }
});

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
        compactHeader: '\n<span class=\"truncate-element\"><a class=\"SUBMITTED emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149 instance-link has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\" uid=\"10\"><b><span data-property=\"identifier\">10<\/span> (<span data-property=\"type\">Project for testing<\/span>) <span data-property=\"title\">10<\/span> (<span data-property=\"status\">Submitted<\/span>)\n<\/b><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:d1e6b763-cdf9-4f87-83ec-c0e9a6254149\"><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span>',
        id: 'emf:123456',
        type: 'projectinstance'
      }, {
        compactHeader: '\n<span><span class=\"banner label label-warning\"><\/span><span class=\"truncate-element\"><a class=\"instance-link has-tooltip\" href=\"http://10.131.2.243:5000/#/open?type=documentinstance&instanceId=emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\" >\n(<span data-property=\"type\">Common document<\/span>) <span data-property=\"title\">Обикновен документ<\/span><span class=\"document-version version badge\">1.7<\/span><\/a><\/span><span class=\"header-icons\" data-instanceId=\"emf:e4f8b06d-8a26-48a1-94f7-4051342e04cd\"><span class=\"custom-icon download downloads-list\" title=\"Add to downloads\"><\/span><span class=\"custom-icon dislikes favourites\" title=\"Add to favourites\"><\/span><\/span><\/span>',
        id: 'emf:234567',
        type: 'documentinstance'
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
