import {IdocMocks} from '../idoc-mocks';
import {SaveIdocAction} from 'idoc/actions/save-idoc-action';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {AfterIdocSaveEvent} from 'idoc/actions/events/after-idoc-save-event';
import {InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'idoc/idoc-context';
import {PromiseStub} from 'test/promise-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('Save idoc action', () => {

  var instanceService = {
    update: () => {
      return Promise.resolve({
        data: {
          id: 'emf:123456'
        }
      });
    },
    updateAll: () => {
      return Promise.resolve({
        data: [
          {
            id: 'emf:123456',
            properties: {
              'emf:version': '1.1'
            }
          },
          {id: 'emf:234567'}
        ]
      });
    },
    create: () => {
      return Promise.resolve({
        data: {
          id: 'emf:123456'
        }
      });
    }
  };
  var spyUpdate = sinon.spy(instanceService, 'update');
  var spyUpdateAll = sinon.spy(instanceService, 'updateAll');
  var spyCreate = sinon.spy(instanceService, 'create');
  var eventbus = {
    publish: sinon.spy(),
    subscribe: sinon.spy()
  };
  var id = 'emf:123456';
  var content = 'content';
  var getSaveObjectPayloadFunction;
  var reloadIDocSilentlyFunction;

  beforeEach(() => {
    spyUpdate.reset();
    spyCreate.reset();
    eventbus.publish.reset();
    // backup the original function and restore it after each test because it is overriden for som eof them which
    // causes the test for that function to not work properly
    getSaveObjectPayloadFunction = SaveIdocAction.prototype.getSaveObjectPayload;
    reloadIDocSilentlyFunction = SaveIdocAction.prototype.reloadIDocSilently;
  });

  afterEach(() => {
    SaveIdocAction.prototype.getSaveObjectPayload = getSaveObjectPayloadFunction;
    SaveIdocAction.prototype.reloadIDocSilently = reloadIDocSilentlyFunction;
    spyUpdateAll.reset();
    spyUpdate.reset();
    spyCreate.reset();
  });

  describe('#execute', ()=> {
    it('should open a dialog and then save', ()=> {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocPageController: {
          animateSaveButton: ()=> {
          },
          disableSaveButton: ()=> {
          }
        }
      };
      var handler = createIdocSaveHandler({});
      sinon.stub(handler, 'getInvalidObjectsModels').returns([{}, {}]);
      let dialogSpy = sinon.spy(handler.saveDialogService, 'openDialog');
      let saveStub = sinon.stub(handler, 'save').returns(PromiseStub.resolve());
      let animateSpy = sinon.spy(context.idocPageController, 'animateSaveButton');

      handler.execute('testAction', context);
      expect(saveStub.called).to.be.true;
      expect(dialogSpy.called).to.be.true;
      expect(animateSpy.called).to.be.true;
    });

    it('should cancel animation if save is canceled in the dialog', ()=> {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocPageController: {
          animateSaveButton: ()=> {
          },
          disableSaveButton: ()=> {
          }
        }
      };
      var handler = createIdocSaveHandler({});
      sinon.stub(handler, 'getInvalidObjectsModels').returns([{}, {}]);
      let dialogStub = sinon.stub(handler.saveDialogService, 'openDialog');
      dialogStub.returns(PromiseStub.reject());
      let saveSpy = sinon.stub(handler, 'save');
      let disableSpy = sinon.spy(context.idocPageController, 'disableSaveButton');
      let animateSpy = sinon.spy(context.idocPageController, 'animateSaveButton');

      handler.execute('testAction', context);
      expect(saveSpy.called, 'should save').to.be.false;
      expect(dialogStub.called, 'should open dialog').to.be.true;
      expect(animateSpy.called, 'animation should be called').to.be.true;
      expect(disableSpy.called, 'animation should be stopped').to.be.true;
    });

    it('should cancel animation when a problem occurs with the save', ()=> {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocPageController: {
          animateSaveButton: ()=> {
          },
          disableSaveButton: ()=> {
          }
        }
      };
      var handler = createIdocSaveHandler({});
      sinon.stub(handler, 'getInvalidObjectsModels').returns([{}, {}]);
      let dialogSpy = sinon.spy(handler.saveDialogService, 'openDialog');
      let saveStub = sinon.stub(handler, 'save').returns(PromiseStub.reject());
      let disableSpy = sinon.spy(context.idocPageController, 'disableSaveButton');
      let animateSpy = sinon.spy(context.idocPageController, 'animateSaveButton');

      handler.execute('testAction', context);
      expect(saveStub.called).to.be.true;
      expect(dialogSpy.called).to.be.true;
      expect(animateSpy.called).to.be.true;
      expect(disableSpy.called, 'animation should be stopped').to.be.true;
    });
  });

  describe('getPersistedObjectsIds()', () => {
    it('should extract and return ids only of the objects that have real one', () => {
      let objects = [
        {id: 'emf:123456'},
        {id: CURRENT_OBJECT_TEMP_ID},
        {id: 'emf:234567'}
      ];
      var objectRealIds = SaveIdocAction.getPersistedObjectsIds(objects);
      expect(objectRealIds.size === 2).to.be.true;
      expect(objectRealIds.has('emf:123456')).to.be.true;
      expect(objectRealIds.has('emf:234567')).to.be.true;
    });

    it('should return empty array if there is no object with real id', () => {
      let objects = [
        {id: CURRENT_OBJECT_TEMP_ID}
      ];
      var objectRealIds = SaveIdocAction.getPersistedObjectsIds(objects);
      expect(objectRealIds.size === 0).to.be.true;
    });
  });

  describe('findTheCurrentObject()', () => {
    it('should return the object which id is not present in provided set of ids', () => {
      let ids = new Set(['emf:123456', 'emf:234567']);
      let responseData = [
        {id: 'emf:123456'},
        {id: 'emf:234567'},
        {id: 'emf:999999'}
      ];
      let found = SaveIdocAction.findTheCurrentObject(responseData, ids);
      expect(found).to.eql({id: 'emf:999999'})
    });
  });

  describe('#allowSave', () => {
    it('should allow save action when no invalid object data exists in context', () => {
      var handler = createIdocSaveHandler({});
      SaveIdocAction.prototype.invalidObjectsMap = {
        'emf:123456': false
      };
      var button = {};
      var data = [{
        id: id,
        isValid: true
      }];
      handler.allowSave()(button, data);
      expect(button.disabled).to.be.false;
    });

    it('should not allow save action when invalid object data exists in context', () => {
      var handler = createIdocSaveHandler({});
      // given all objects in context are invalid
      SaveIdocAction.prototype.invalidObjectsMap = {
        'emf:123456': false,
        'emf:987654': false
      };
      var button = {};
      // when validation returns that one of the objects is already valid
      var data = [{
        id: 'emf:987654',
        isValid: true
      }];
      handler.allowSave()(button, data);
      // then save should be disabled because there is still one invalid object
      expect(button.disabled).to.be.true;
      // when validation returns that the second object became valid
      data = [{
        id: 'emf:123456',
        isValid: true
      }];
      handler.allowSave()(button, data);
      // then save should be enabled because all objects are valid now
      expect(button.disabled).to.be.false;
    });
  });

  describe('#save', () => {
    it('should publish BeforeIdocSaveEvent', () => {
      SaveIdocAction.prototype.getSaveObjectPayload = () => {
        return PromiseStub.resolve([
          {definitionId: 'idocfortest'}
        ]);
      };

      var handler = createIdocSaveHandler({
        validObjects: true,
        eventbus: eventbus,
        instanceService: instanceService
      });

      var context = {
        currentObject: mockCurrentObject(id, true),
        idocContext: createIdocContext(id),
        idocPageController: {
          stopDraftInterval: sinon.spy()
        }
      };

      handler.save(context, {});

      expect(eventbus.publish.args[0][0] instanceof BeforeIdocSaveEvent).to.be.true;
      expect(eventbus.publish.callCount).to.equal(1);
    });

    it('should call instance rest service to update the object if it has id', (done) => {
      SaveIdocAction.prototype.getSaveObjectPayload = () => {
        return PromiseStub.resolve([
          {definitionId: 'idocfortest'}
        ]);
      };
      SaveIdocAction.prototype.reloadIDocSilently = () => {
      };

      var handler = createIdocSaveHandler({
        validObjects: true,
        eventbus: eventbus,
        instanceService: instanceService
      });

      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(id, true),
        idocPageController: {
          stopDraftInterval: sinon.stub()
        }
      };

      handler.save(context, {}).then(() => {
        expect(spyUpdateAll.callCount).to.equal(1);
        var updateAllCall = spyUpdateAll.getCall(0);
        expect(updateAllCall.args[0]).to.deep.equal([{
          definitionId: 'idocfortest'
        }]);
        done();
      }).catch(done);
    });

    it('should call instance rest service to create a new object if it has no id', (done) => {
      SaveIdocAction.prototype.getSaveObjectPayload = () => {
        return PromiseStub.resolve([{
          definitionId: 'idocfortest',
          properties: {}
        }]);
      };

      SaveIdocAction.prototype.reloadIDocSilently = () => {
      };

      var handler = createIdocSaveHandler({
        validObjects: true,
        eventbus: eventbus,
        instanceService: instanceService
      });

      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(CURRENT_OBJECT_TEMP_ID, false, {
          parentId: 'emf:987654',
          purpose: 'iDoc'
        })
      };

      handler.save(context, {}).then(() => {
        expect(spyUpdateAll.callCount).to.equal(1);
        var updateAllCall = spyUpdateAll.getCall(0);
        expect(updateAllCall.args[0]).to.deep.equal([{
          definitionId: 'idocfortest',
          properties: {},
          parentId: 'emf:987654'
        }]);
        done();
      }).catch(done);
    });

    it('should stop draft interval and delete draft if object already exists', (done) => {
      SaveIdocAction.prototype.getSaveObjectPayload = () => {
        return PromiseStub.resolve([
          {definitionId: 'idocfortest'}
        ]);
      };
      SaveIdocAction.prototype.reloadIDocSilently = () => {
      };

      var handler = createIdocSaveHandler({
        validObjects: true,
        eventbus: eventbus,
        instanceService: instanceService
      });
      handler.idocDraftService.deleteDraft = () => PromiseStub.resolve();
      let deleteDraftSpy = sinon.spy(handler.idocDraftService, 'deleteDraft');

      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(id, true),
        idocPageController: {
          stopDraftInterval: sinon.spy()
        }
      };
      let stopDraftIntervalSpy = sinon.spy(handler, 'stopDraftInterval');
      handler.save(context, {}).then(() => {
        expect(stopDraftIntervalSpy.callCount).to.equal(1);
        expect(deleteDraftSpy.callCount).to.equal(1);
        done();
      }).catch(done);
    });
  });

  describe('#getSaveObjectPayload', () => {
    it('should return configured payload object for existing object update', (done) => {
      var context = {
        currentObject: mockCurrentObject(id, false, {}),
        idocContext: createIdocContext()
      };

      context.idocPageController = {
        getIdocContent: () => {
          return content;
        }
      };

      var handler = createIdocSaveHandler({});
      var payloadResolver = handler.getSaveObjectPayload(context);
      payloadResolver.then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id: id,
          definitionId: id,
          content: content,
          dynamicQueries: {},
          properties: {
            textareaEdit: 'textareaEdit',
            textareaPreview: 'textareaPreview'
          }
        });
        done();
      }).catch(done);
    });

    it('should return configured payload object for new object create', (done) => {
      var context = {
        currentObject: mockCurrentObject(CURRENT_OBJECT_TEMP_ID, false, {}),
        idocContext: createIdocContext(),
        idocPageController: {
          getIdocContent: () => {
            return content
          }
        }
      };

      var handler = createIdocSaveHandler({});
      var payloadResolver = handler.getSaveObjectPayload(context);
      payloadResolver.then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id: id,
          definitionId: id,
          properties: {
            textareaEdit: 'textareaEdit',
            textareaPreview: 'textareaPreview'
          }
        });
        done();
      }).catch(done);
    });

    it('should return configured payload object with purpose and mimetype if current object has purpose', (done) => {
      var context = {
        currentObject: mockCurrentObject(id, false, {
          purpose: 'idoc'
        }),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content
          }
        }
      };

      var handler = createIdocSaveHandler({});
      var payloadResolver = handler.getSaveObjectPayload(context);
      payloadResolver.then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id: id,
          definitionId: id,
          content: content,
          dynamicQueries: {},
          properties: {
            textareaEdit: 'textareaEdit',
            textareaPreview: 'textareaPreview',
            'emf:purpose': 'idoc',
            mimetype: 'text/html'
          }
        });
        done();
      }).catch(done);
    });

    it('should call getAllSharedObjects with true as first argument', (done) => {
      var context = {
        currentObject: mockCurrentObject(id, false, {}),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content
          }
        }
      };
      var getAllSharedObjectsSpy = sinon.spy(context.idocContext, 'getAllSharedObjects');

      var handler = createIdocSaveHandler({});
      handler.getSaveObjectPayload(context).then(() => {
        expect(getAllSharedObjectsSpy.callCount).to.equal(1);
        expect(getAllSharedObjectsSpy.args[0][0]).to.be.true;
        done();
      }).catch(done);
    });

    it('should return current object even if it is not modified', (done) => {
      var context = {
        currentObject: mockCurrentObject(id, false, {}),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content
          }
        }
      };

      // make defaultValue same as value so getChangeset will return empty object and skip this object for
      let sharedObjects = context.idocContext.getSharedObjects();
      let notModifiedObjectValidationModel = sharedObjects[0].models.validationModel.serialize();
      Object.keys(notModifiedObjectValidationModel).forEach((property) => {
        notModifiedObjectValidationModel[property].defaultValue = notModifiedObjectValidationModel[property].value;
      });

      var handler = createIdocSaveHandler({});
      var payloadResolver = handler.getSaveObjectPayload(context);

      payloadResolver.then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.eql({
          id: id,
          definitionId: id,
          content: content,
          dynamicQueries: {},
          properties: {
            textareaEdit: "textareaEdit",
            textareaPreview: "textareaPreview"
          }
        });
        done();
      }).then(done);
    });
  });

  describe('#getInvalidObjectsModels', () => {

    it('should return no data if all objects in context are valid', () => {
      var context = {
        idocContext: createIdocContext(id)
      };

      var handler = createIdocSaveHandler({
        validObjects: true
      });

      expect(handler.getInvalidObjectsModels(context)).to.eql({});
    });

    it('should return only data for shared objects that have id', () => {
      var handler = createIdocSaveHandler({
        validObjects: false
      }, null);

      var context = {
        idocContext: createIdocContext(id)
      };

      expect(Object.keys(handler.getInvalidObjectsModels(context))).to.eql([id, 'emf:999888']);
    });

    it('should return 2 objects with invalid data', () => {
      var handler = createIdocSaveHandler({
        validObjects: false
      });

      var context = {
        idocContext: createIdocContext(id)
      };

      var invalidObjectsModels = handler.getInvalidObjectsModels(context);
      let expected = {
        renderMandatory: true,
        formViewMode: 'EDIT',
        models: {
          definitionId: "emf:123456",
          headers: {
            'default_header': "emf:123456",
            'compact_header': "emf:123456",
            'breadcrumb_header': "emf:123456"
          },
          id: "emf:123456",
          validationModel: new InstanceModel({
            'textareaEdit': {
              'dataType': 'text',
              'messages': [],
              'value': 'textareaEdit'
            },
            'textareaPreview': {
              'dataType': 'text',
              'messages': [],
              'value': 'textareaPreview'
            }
          }),
          viewModel: new DefinitionModel({
            fields: [{
              'previewEmpty': true,
              'identifier': 'textareaEdit',
              'disabled': false,
              'displayType': 'EDITABLE',
              'validators': [],
              'dataType': 'text',
              'label': 'Editable textarea',
              'regexValidator': '[\\s\\S]{1,500}',
              'regexValidatorError': 'This field should be max 60 characters length',
              'isMandatory': true,
              'mandatoryValidatorError': 'The field is mandatory',
              'maxLength': 60,
              'isDataProperty': true
            },
            {
              'previewEmpty': true,
              'identifier': 'textareaPreview',
              'disabled': false,
              'displayType': 'READ_ONLY',
              'validators': [],
              'dataType': 'text',
              'label': 'Preview textarea',
              'regexValidator': '[\\s\\S]{1,60}',
              'regexValidatorError': 'This field should be max 60 characters length',
              'isMandatory': false,
              'maxLength': 60,
              'isDataProperty': true
            },
            {
              'identifier': 'default_header',
              'dataType': 'text',
              'displayType': 'SYSTEM',
              'isDataProperty': true,
              'control': {
                'identifier': 'INSTANCE_HEADER'
              }
            },
            {
              'identifier': 'compact_header',
              'dataType': 'text',
              'displayType': 'SYSTEM',
              'isDataProperty': true,
              'control': {
                'identifier': 'INSTANCE_HEADER'
              }
            },
            {
              'identifier': 'breadcrumb_header',
              'dataType': 'text',
              'displayType': 'SYSTEM',
              'isDataProperty': true,
              'control': {
                'identifier': 'INSTANCE_HEADER'
              }
            }]
          })
        }
      };
      expect(Object.keys(invalidObjectsModels).length).to.equal(2);
      expect(invalidObjectsModels[Object.keys(invalidObjectsModels)[0]]).to.deep.equal(expected);
    });

    describe('#reloadIdocSilently', () => {
      it('should update currentObject, notify for success and call method for update models', () => {
        var notificationService = {
          success: () => {
          }
        };
        var spySuccess = sinon.spy(notificationService, 'success');
        var stateParamsAdapter = {
          setStateParam: sinon.spy(),
          getStateParams: () => {
            return [1, 2]
          }
        };

        var context = {
          idocPageController: {
            setViewMode: (mode) => {
            }
          },
          idocContext: createIdocContext(id),
          currentObject: {
            id: id,
            models: {
              definitionId: id,
              parentId: "emf:234567"
            },
            setId: sinon.spy(),
            setContent: sinon.spy(),
            getContextPathIds: ()=> Promise.resolve({}),
            getChangeset: ()=> Promise.resolve({}),
            getModels: ()=> Promise.resolve({
              definitionId: id
            })
          }
        };

        var spyReloadObjectDetails = sinon.spy(context.idocContext, 'reloadObjectDetails');

        var handler = createIdocSaveHandler({
          validObjects: false,
          notificationService: notificationService,
          stateParamsAdapter: stateParamsAdapter,
          router: {}
        });

        handler.reloadIDocSilently({
          data: [{
            id: id,
            properties: {
              property1: 123,
              content: content
            }
          }]
        }, id, context);

        expect(spySuccess.callCount, 'Notification Success should be risen').to.equal(1);
        expect(context.currentObject.setId.getCall(0).args[0], 'Should set id to current object').to.equal(id);
        expect(spyReloadObjectDetails.callCount, 'Should call method to reload object details').to.equal(1);
        expect(stateParamsAdapter.setStateParam.getCall(0).args).to.deep.equal(['id', id]);
      });
    });

    describe('updateDetails(object, currentObjectId)', () => {

      it('should not update the current objects content if it is a shared object', () => {
        var currentObject = new InstanceObject('id');
        currentObject.content = 'content'

        var context = {
          currentObject: currentObject,
          idocContext: createIdocContext(id)
        };

        var handler = createIdocSaveHandler({});
        var object = {
          id: 'shared',
          properties: {},
          content: 'shared-content'
        };
        handler.updateDetails(object, 'id', context);
        expect(context.currentObject.content).to.equal('content');
      });
    });

    describe('afterObjectReloadHandler()', () => {
      it('should set form view mode to preview and perform navigation', () => {
        var stateParamsAdapter = {
          setStateParam: sinon.spy(),
          getStateParams: () => {
            return [1, 2]
          }
        };

        var context = {
          idocPageController: {
            setViewMode: (mode) => {
            },
            checkPermissionsForEditAction: () => {
            }
          },
          idocContext: createIdocContext(id)
        };

        var spySetViewMode = sinon.spy(context.idocPageController, 'setViewMode');
        var spyCheckPermissionsForEditAction = sinon.spy(context.idocPageController, 'checkPermissionsForEditAction');

        var router = {
          navigate: sinon.spy()
        };
        var handler = createIdocSaveHandler({
          validObjects: false,
          notificationService: () => {
          },
          stateParamsAdapter: stateParamsAdapter,
          router: router
        });

        handler.afterInstanceRefreshHandler(context);

        expect(spySetViewMode.getCall(0).args[0], 'The form view model should be changed to PREVIEW').to.equal('preview');
        expect(spyCheckPermissionsForEditAction.called).to.be.true;
        expect(router.navigate.getCall(0).args, 'Should navigate to idoc with proper request parameters').to.deep.equal(['idoc', [1, 2], {
          notify: true,
          skipRouteInterrupt: true
        }]);
      });
    });

    it('#extractDynamicCriteriaMap should return a map with all criteria for all automatic searches in widgets', (done) => {
      fixture.setBase('test/idoc/actions');
      let idocContent = fixture.load('save-idoc-content.html');

      var context = {
        idocPageController: {
          setViewMode: (mode) => {
          },
          checkPermissionsForEditAction: () => {
          }
        },
        idocContext: createIdocContext(id)
      };

      let handler = createIdocSaveHandler({});
      let dynamicCriteriaResolver = handler.extractDynamicCriteriaMap(idocContent, context);
      dynamicCriteriaResolver.then((result) => {
        expect(Object.keys(result)).to.have.length(2);
        expect(result['e3d612ea-b87a-4dc5-e16a-fdb50f0529a0'].rules[0].rules).to.have.length(2);
        fixture.cleanup();
        done();
      }).catch(done);
    });

  });

  describe('#getDynamicCriteriaResolver', () => {
    it('should resolve with proper criteria for advanced search type', (done) => {
      let criteria = {
        'condition': 'OR',
        'rules': [{
          'condition': 'AND',
          'rules': [{
            'id': '0f5b1f31-72b4-4e11-eee6-f14dba7e5a19',
            'field': 'rdf:type',
            'type': '',
            'operator': 'equals',
            'value': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document'
          },
            {
              'id': 'd46d875b-245c-48f1-d2d2-b97c590eb1a6',
              'condition': 'AND',
              'rules': [{
                'id': '672f6a9a-01c9-4bc4-c0eb-f656ee70ae57',
                'field': 'dcterms:title',
                'type': 'rdfs:Literal',
                'operator': 'contains',
                'value': ['Обикновен']
              }]
            }],
          'id': 'd61bd37d-9a59-449a-be64-b2329c35205a'
        }],
        'id': 'e3d612ea-b87a-4dc5-e16a-fdb50f0529a0'
      };
      let handler = createIdocSaveHandler({});
      let dynamicCriteriaResolver = handler.getDynamicCriteriaResolver(criteria, 'advanced');
      dynamicCriteriaResolver.then((result) => {
        expect(result.rules).to.eql(criteria.rules);
        done();
      }).catch(done);
    });
  });

  describe('containsCurrentUserModel', () => {
    it('should return true if current user model is present in the list', () => {
      expect(SaveIdocAction.containsCurrentUserModel([
        { 'id': 'emf:123456' },
        { 'id': 'emf:234567' },
        { 'id': 'admin@user1.bg' }
      ], 'admin@user1.bg')).to.be.true;
    });

    it('should return false if current user model is present in the list', () => {
      expect(SaveIdocAction.containsCurrentUserModel([
        { 'id': 'emf:123456' },
        { 'id': 'emf:234567' },
        { 'id': 'emf:345678' }
      ], 'admin@user1.bg')).to.be.false;
    });
  });

  // Test the afterUpdate workflow.
  describe('afterUpdate', () => {
    it('should fire AfterIdocSaveEvent', () => {
      let handler = createIdocSaveHandler({});
      let spyPublish = sinon.spy(handler.eventbus, 'publish');
      handler.afterUpdate({}, 'emf:123456', {}, true);
      expect(spyPublish.called).to.be.true;
      expect(spyPublish.getCall(0).args[0]).to.be.instanceof(AfterIdocSaveEvent);
    });

    it('should change the language if current user has been updated', () => {
      let handler = createIdocSaveHandler({});
      let spyChangeLanguage = sinon.spy(handler.translateService, 'changeLanguage');
      handler.afterUpdate({}, 'emf:123456', {}, true);
      expect(spyChangeLanguage.called).to.be.true;
      expect(spyChangeLanguage.getCall(0).args).to.eql(['en']);
    });

    it('should not change the language if current user hasn`t been updated', () => {
      let handler = createIdocSaveHandler({});
      let spyChangeLanguage = sinon.spy(handler.translateService, 'changeLanguage');
      handler.afterUpdate({}, 'emf:123456', {}, false);
      expect(spyChangeLanguage.called).to.be.false;
    });

    it('should reload idoc', () => {
      let handler = createIdocSaveHandler({});
      let spyReloadIdoc = sinon.spy(SaveIdocAction.prototype, 'reloadIdoc');
      handler.afterUpdate({}, 'emf:123456', {}, true);
      expect(spyReloadIdoc.called).to.be.true;
    });
  });

  function mockCurrentObject(id, isPersisted, models) {
    return {
      getId: () => {
        return id
      },
      isPersisted: () => {
        return isPersisted
      },
      getModels: () => {
        return models
      },
      getChangeset: () => {
        return {
          property1: 123
        }
      },
      models: {
        validationModel: {
          'emf:version': {
            value: '1.1'
          }
        }
      }
    }
  }

  /**
   * @param config { validObjects, eventbus, instanceService }
   * @param sharedObjectId an id to be used for the shared object and it will be used unless undefined is passed or not being provided
   * @returns {*}
   */
  function createIdocSaveHandler(config, sharedObjectId) {
    var validationService2 = {
      validate: () => {
        return config.validObjects;
      },
      init: ()=> {
        return PromiseStub.resolve();
      }
    };
    var notificationService = {
      success: () => {
      }
    };
    var dialogService2 = {
      openDialog: ()=> {
        return PromiseStub.resolve();
      }
    };

    var searchResolverService = {
      resolve: sinon.spy((criteria) => {
        return PromiseStub.resolve(criteria);
      })
    };

    var userServiceMock = {
      getCurrentUserId: () => { return 'emf:123456' },
      getCurrentUser: () => {
        return PromiseStub.resolve({
          id: 'john@domain',
          name: 'John',
          username: 'john',
          isAdmin: true,
          language: 'en'
        });
      }
    };

    return new SaveIdocAction(config.eventbus || IdocMocks.mockEventBus(), config.instanceService || IdocMocks.mockInstanceRestService(),
      IdocMocks.mockLogger(), validationService2, config.notificationService || notificationService, IdocMocks.mockTranslateService(),
      config.stateParamsAdapter || IdocMocks.mockStateParamsAdapter(), config.router || IdocMocks.mockRouter(),
      config.dialogService || dialogService2, IdocMocks.mockActionsService(), searchResolverService, PromiseAdapterMock.mockAdapter(),
      IdocMocks.mockIdocDraftService(), userServiceMock);
  }

  function createIdocContext(objectId) {
    var objectId = objectId === null && objectId !== undefined ? objectId : 'emf:123456';
    return {
      getSharedObjects: function () {
        return [
          new InstanceObject(objectId, createModels(objectId), 'template.content'),
          new InstanceObject('emf:999888', createModels('emf:999888'), 'template.content')
        ]
      },
      getAllSharedObjects: function () {
        return this.getSharedObjects();
      },
      reloadObjectDetails: () => {
        return Promise.resolve([]);
      },
      setCurrentObjectId: () => {
      },
      updateTempCurrentObjectId: () => {
      },
      mergeObjectsModels: () => {
      }
    };
  }

  function createModels(id) {
    return {
      definitionId: id,
      headers: {
        'default_header': "emf:123456",
        'compact_header': "emf:123456",
        'breadcrumb_header': "emf:123456"
      },
      validationModel: {
        'textareaEdit': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaEdit'
        },
        'textareaPreview': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaPreview'
        }
      },
      viewModel: {
        fields: [{
          'previewEmpty': true,
          'identifier': 'textareaEdit',
          'disabled': false,
          'displayType': 'EDITABLE',
          'validators': [],
          'dataType': 'text',
          'label': 'Editable textarea',
          'regexValidator': '[\\s\\S]{1,500}',
          'regexValidatorError': 'This field should be max 60 characters length',
          'isMandatory': true,
          'mandatoryValidatorError': 'The field is mandatory',
          'maxLength': 60,
          'isDataProperty': true
        },
          {
            'previewEmpty': true,
            'identifier': 'textareaPreview',
            'disabled': false,
            'displayType': 'READ_ONLY',
            'validators': [],
            'dataType': 'text',
            'label': 'Preview textarea',
            'regexValidator': '[\\s\\S]{1,60}',
            'regexValidatorError': 'This field should be max 60 characters length',
            'isMandatory': false,
            'maxLength': 60,
            'isDataProperty': true
          }]
      }
    }
  }
})
;
