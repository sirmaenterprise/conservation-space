import {stub} from 'test/test-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceRestService} from 'services/rest/instance-service';
import {Logger} from 'services/logging/logger';
import {ValidationService} from 'form-builder/validation/validation-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Router} from 'adapters/router/router';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceObject, CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {PromiseStub} from 'test/promise-stub';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {UserService} from 'security/user-service';
import {Configuration} from 'common/application-config';
import {InstanceContextService, ERROR_EXISTING_WITHOUT_CONTEXT} from 'services/idoc/instance-context-service';
import {SaveIdocAction} from 'idoc/actions/save-idoc-action';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';
import {ValidationModelBuilder} from 'test/form-builder/validation-model-builder';
import {AfterIdocSaveEvent} from 'idoc/actions/events/after-idoc-save-event';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {
  SELECTION_MODE_WITHOUT_CONTEXT
} from 'components/contextselector/context-selector';

describe('Save idoc action', () => {

  const INSTANCE_ONE_DEFINITION_ID = 'SIA0001';

  let eventbus;
  let instanceRestService;
  let logger;
  let validationService;
  let notificationService;
  let translateService;
  let stateParamsAdapter;
  let router;
  let saveDialogService;
  let actionsService;
  let searchResolverService;
  let idocDraftService;
  let userService;
  let configuration;
  let instanceContextService;

  let saveIdocAction;

  beforeEach(() => {
    eventbus = stub(Eventbus);
    instanceRestService = stub(InstanceRestService);
    logger = stub(Logger);
    validationService = stub(ValidationService);
    notificationService = stub(NotificationService);
    translateService = stub(TranslateService);
    stateParamsAdapter = stub(StateParamsAdapter);
    router = stub(Router);
    saveDialogService = stub(SaveDialogService);
    actionsService = stub(ActionsService);
    searchResolverService = createSearchResolverService();
    idocDraftService = mockIdocDraftService();
    userService = stub(UserService);
    configuration = stub(Configuration);
    instanceContextService = stub(InstanceContextService);

    saveIdocAction = new SaveIdocAction(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
      stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, PromiseStub,
      idocDraftService, userService, configuration, instanceContextService);
  });

  function mockIdocDraftService() {
    let idocDraftService = stub(IdocDraftService);
    idocDraftService.saveDraft.returns(PromiseStub.resolve());
    idocDraftService.loadDraft.returns(PromiseStub.resolve());
    idocDraftService.deleteDraft.returns(PromiseStub.resolve());
    return idocDraftService;
  }

  describe('#execute', () => {
    beforeEach(() => {
      validationService.init.returns(PromiseStub.resolve());
    });


    it('should open a dialog and then save', () => {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocActionsController: {
          animateSaveButton: () => {
          },
          disableSaveButton: () => {
          }
        }
      };
      sinon.stub(saveIdocAction, 'getInvalidObjectsModels').returns(PromiseStub.resolve([{}, {}]));
      saveDialogService.openDialog.returns(PromiseStub.resolve());
      let saveStub = sinon.stub(saveIdocAction, 'save').returns(PromiseStub.resolve());
      let animateSpy = sinon.spy(context.idocActionsController, 'animateSaveButton');

      saveIdocAction.execute('testAction', context);
      expect(saveStub.called).to.be.true;
      expect(saveDialogService.openDialog.called).to.be.true;
      expect(animateSpy.called).to.be.true;
    });

    it('should cancel animation if save is canceled in the dialog', () => {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocActionsController: {
          animateSaveButton: () => {
          },
          disableSaveButton: () => {
          }
        }
      };
      sinon.stub(saveIdocAction, 'getInvalidObjectsModels').returns(PromiseStub.resolve([{}, {}]));
      saveDialogService.openDialog.returns(PromiseStub.reject());
      let saveSpy = sinon.stub(saveIdocAction, 'save');
      let disableSpy = sinon.spy(context.idocActionsController, 'disableSaveButton');
      let animateSpy = sinon.spy(context.idocActionsController, 'animateSaveButton');

      saveIdocAction.execute('testAction', context);
      expect(saveSpy.called, 'should save').to.be.false;
      expect(saveDialogService.openDialog.called, 'should open dialog').to.be.true;
      expect(animateSpy.called, 'animation should be called').to.be.true;
      expect(disableSpy.called, 'animation should be stopped').to.be.true;
    });

    it('should cancel animation when a problem occurs with the save', () => {
      let context = {
        idocContext: 'test',
        currentObject: 'test',
        idocActionsController: {
          animateSaveButton: () => {
          },
          disableSaveButton: () => {
          }
        }
      };
      sinon.stub(saveIdocAction, 'getInvalidObjectsModels').returns(PromiseStub.resolve([{}, {}]));
      let saveStub = sinon.stub(saveIdocAction, 'save').returns(PromiseStub.reject());
      let disableSpy = sinon.spy(context.idocActionsController, 'disableSaveButton');
      let animateSpy = sinon.spy(context.idocActionsController, 'animateSaveButton');

      saveIdocAction.execute('testAction', context);
      expect(saveStub.called).to.be.true;
      expect(saveDialogService.openDialog.called).to.be.true;
      expect(animateSpy.called).to.be.true;
      expect(disableSpy.called, 'animation should be stopped').to.be.true;
    });
  });

  describe('#save', () => {

    let spyGetSaveObjectPayload;
    let id = 'emf:123456';
    beforeEach(() => {
      // spyGetSaveObjectPayload = sinon.stub(saveIdocAction, 'getSaveObjectPayload', (context) => {
      //   return PromiseStub.resolve([{definitionId: 'idocfortest'}]);
      // });
      spyGetSaveObjectPayload = sinon.stub(saveIdocAction, 'getSaveObjectPayload');
      spyGetSaveObjectPayload.returns(PromiseStub.resolve([{definitionId: 'idocfortest'}]));
      instanceRestService.update.returns(Promise.resolve({data: {id: 'emf:123456'}}));
      instanceRestService.updateAll.returns(Promise.resolve({
        data: [{
          id: 'emf:123456',
          properties: {'emf:version': '1.1'}
        }, {id: 'emf:234567'}]
      }));
      instanceRestService.create.returns(Promise.resolve({data: {id: 'emf:123456'}}));
      userService.getCurrentUserId.returns('emf:123456');
      userService.getCurrentUser.returns(PromiseStub.resolve({
        id: 'john@domain',
        name: 'John',
        username: 'john',
        isAdmin: true,
        language: 'en'
      }));
    });

    afterEach(() => {
      spyGetSaveObjectPayload.restore();
    });

    it('should publish BeforeIdocSaveEvent', () => {
      let context = {
        currentObject: mockCurrentObject(id, true),
        idocContext: createIdocContext(id),
        idocPageController: {
          stopDraftInterval: sinon.spy()
        }
      };

      saveIdocAction.save(context, {});

      expect(eventbus.publish.args[0][0] instanceof BeforeIdocSaveEvent).to.be.true;
      expect(eventbus.publish.callCount).to.equal(1);

    });

    it('should call instance rest service to update the object if it has id', (done) => {
      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(id, true),
        idocPageController: {
          stopDraftInterval: sinon.stub()
        }
      };

      saveIdocAction.save(context, {}).then(() => {
        expect(instanceRestService.updateAll.callCount).to.equal(1);
        let updateAllCall = instanceRestService.updateAll.getCall(0);
        expect(updateAllCall.args[0]).to.deep.equal([{
          definitionId: 'idocfortest'
        }]);
        done();
      }).catch(done);
    });

    it('should call instance rest service to create a new object if it has no id', (done) => {
      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(CURRENT_OBJECT_TEMP_ID, false, {
          parentId: 'emf:987654',
          purpose: 'iDoc'
        })
      };

      saveIdocAction.save(context, {}).then(() => {
        expect(instanceRestService.updateAll.callCount).to.equal(1);
        let updateAllCall = instanceRestService.updateAll.getCall(0);

        expect(updateAllCall.args[0]).to.deep.equal([{
          definitionId: 'idocfortest',
          parentId: 'emf:987654'
        }]);
        done();
      }).catch(done);
    });

    it('should stop draft interval and delete draft if object already exists', (done) => {

      let context = {
        idocContext: createIdocContext(),
        currentObject: mockCurrentObject(id, true),
        idocPageController: {
          stopDraftInterval: sinon.spy()
        }
      };
      let stopDraftIntervalSpy = sinon.spy(saveIdocAction, 'stopDraftInterval');
      saveIdocAction.save(context, {}).then(() => {
        expect(stopDraftIntervalSpy.callCount).to.equal(1);
        expect(idocDraftService.deleteDraft.callCount).to.equal(1);
        done();
      }).catch(done);
    });
  });

  describe('containsCurrentUserModel', () => {
    it('should return true if current user model is present in the list', () => {
      expect(SaveIdocAction.containsCurrentUserModel([
        {'id': 'emf:123456'},
        {'id': 'emf:234567'},
        {'id': 'admin@user1.bg'}
      ], 'admin@user1.bg')).to.be.true;
    });

    it('should return false if current user model is present in the list', () => {
      expect(SaveIdocAction.containsCurrentUserModel([
        {'id': 'emf:123456'},
        {'id': 'emf:234567'},
        {'id': 'emf:345678'}
      ], 'admin@user1.bg')).to.be.false;
    });
  });

  // Test the afterUpdate workflow.
  describe('afterUpdate', () => {

    beforeEach(() => {
      userService.getCurrentUserId.returns('emf:123456');
      userService.getCurrentUser.returns(PromiseStub.resolve({
        id: 'john@domain',
        name: 'John',
        username: 'john',
        isAdmin: true,
        language: 'en'
      }));
    });

    it('should fire AfterIdocSaveEvent', () => {
      // let spyPublish = sinon.spy(saveIdocAction.eventbus, 'publish');
      saveIdocAction.afterUpdate({}, 'emf:123456', {}, true);
      expect(eventbus.publish.called).to.be.true;
      expect(eventbus.publish.getCall(0).args[0]).to.be.instanceof(AfterIdocSaveEvent);
    });

    it('should change the language if current user has been updated', () => {
      saveIdocAction.afterUpdate({}, 'emf:123456', {}, true);
      expect(translateService.changeLanguage.called).to.be.true;
      expect(translateService.changeLanguage.getCall(0).args).to.eql(['en']);
    });

    it('should not change the language if current user hasn`t been updated', () => {
      saveIdocAction.afterUpdate({}, 'emf:123456', {}, false);
      expect(translateService.changeLanguage.called).to.be.false;
    });

    it('should reload idoc', () => {
      saveIdocAction.afterUpdate({}, 'emf:123456', {}, true);
      expect(translateService.changeLanguage.called).to.be.true;
    });
  });

  describe('getPersistedObjectsIds()', () => {
    it('should extract and return ids only of the objects that have real one', () => {
      let objects = [
        {id: 'emf:123456'},
        {id: CURRENT_OBJECT_TEMP_ID},
        {id: 'emf:234567'}
      ];
      let objectRealIds = SaveIdocAction.getPersistedObjectsIds(objects);
      expect(objectRealIds.size === 2).to.be.true;
      expect(objectRealIds.has('emf:123456')).to.be.true;
      expect(objectRealIds.has('emf:234567')).to.be.true;
    });

    it('should return empty array if there is no object with real id', () => {
      let objects = [
        {id: CURRENT_OBJECT_TEMP_ID}
      ];
      let objectRealIds = SaveIdocAction.getPersistedObjectsIds(objects);
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
      expect(found).to.eql({id: 'emf:999999'});
    });
  });

  describe('#getSaveObjectPayload', () => {
    let id = 'emf:123456';
    let content = 'content';
    it('should return configured payload object for existing object update', (done) => {
      let models = {
        validationModel: {}
      };
      let context = {
        currentObject: mockCurrentObject(id, false, models),
        idocContext: createIdocContext()
      };

      context.idocPageController = {
        getIdocContent: () => {
          return content;
        }
      };

      saveIdocAction.getSaveObjectPayload(context).then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id,
          $versionMode$: 'UPDATE',
          definitionId: id,
          content,
          properties: {
            textareaEdit: 'textareaEdit',
            textareaPreview: 'textareaPreview'
          }
        });
        done();
      }).catch(done);
    });

    it('should return configured payload object for new object create', (done) => {
      let context = {
        currentObject: mockCurrentObject(CURRENT_OBJECT_TEMP_ID, false, {}),
        idocContext: createIdocContext(),
        idocPageController: {
          getIdocContent: () => {
            return content;
          }
        }
      };

      saveIdocAction.getSaveObjectPayload(context).then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id,
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
      let models = {
        validationModel: {},
        purpose: 'idoc'
      };
      let context = {
        currentObject: mockCurrentObject(id, false, models),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content;
          }
        }
      };

      saveIdocAction.getSaveObjectPayload(context).then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.deep.equal({
          id,
          $versionMode$: 'UPDATE',
          definitionId: id,
          content,
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
      let models = {
        validationModel: {}
      };
      let context = {
        currentObject: mockCurrentObject(id, false, models),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content;
          }
        }
      };
      let getAllSharedObjectsSpy = sinon.spy(context.idocContext, 'getAllSharedObjects');

      saveIdocAction.getSaveObjectPayload(context).then(() => {
        expect(getAllSharedObjectsSpy.callCount).to.equal(1);
        expect(getAllSharedObjectsSpy.args[0][0]).to.be.true;
        done();
      }).catch(done);
    });

    it('should return current object even if it is not modified', (done) => {
      let models = {
        validationModel: {}
      };
      let context = {
        currentObject: mockCurrentObject(id, false, models),
        idocContext: createIdocContext(id),
        idocPageController: {
          getIdocContent: () => {
            return content;
          }
        }
      };

      // make defaultValue same as value so getChangeset will return empty object and skip this object for
      let sharedObjects = context.idocContext.getSharedObjects();
      let notModifiedObjectValidationModel = sharedObjects[0].models.validationModel.serialize();
      Object.keys(notModifiedObjectValidationModel).forEach((property) => {
        notModifiedObjectValidationModel[property].defaultValue = notModifiedObjectValidationModel[property].value;
      });

      saveIdocAction.getSaveObjectPayload(context).then((payload) => {
        expect(payload).to.have.length(2);
        expect(payload[0]).to.eql({
          id,
          $versionMode$: 'UPDATE',
          definitionId: id,
          content,
          properties: {
            textareaEdit: 'textareaEdit',
            textareaPreview: 'textareaPreview'
          }
        });
        done();
      }).then(done);
    });
  });

  describe('getInvalidObjectsModels', () => {
    it('should return all invalid objects', () => {
      let existingInContextResultConfiguration = [];
      // Given:
      // 1. Create instanceObject which is with all fields are valid.
      let validObjectId = 'emf:validObject';
      let validObject = creteInstanceObject(validObjectId);
      existingInContextResultConfiguration.push({
        id: validObjectId,
        isValid: true,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT
      });


      // 2. Create instanceObject which is with not all fields valid.
      let invalidObjectId = 'emf:invalidObject';
      let invalidObject = creteInstanceObject(invalidObjectId);
      existingInContextResultConfiguration.push({
        id: invalidObjectId,
        isValid: true,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT
      });

      // 3. Create instanceObject which is with not valid existence in context validation.
      let invalidExistingInContextObjectId = 'emf:invalidExistingInContextObjectId';
      let invalidExistingInContextObject = creteInstanceObject(invalidExistingInContextObjectId);
      existingInContextResultConfiguration.push({
        id: invalidExistingInContextObjectId,
        isValid: false,
        existingInContext: SELECTION_MODE_WITHOUT_CONTEXT,
        errorMessage: ERROR_EXISTING_WITHOUT_CONTEXT
      });

      setupExistingInContextValidation(existingInContextResultConfiguration);


      let validationResults = {};
      validationResults[validObjectId] = {isValid: true};
      validationResults[invalidObjectId] = {isValid: false};
      validationResults[invalidExistingInContextObjectId] = {isValid: true};
      validationService.validateAll.returns(PromiseStub.resolve(validationResults));


      let context = {
        idocContext: {
          getAllSharedObjects: (onlyModified) => {
            return [validObject, invalidObject, invalidExistingInContextObject];
          }
        }
      };

      // When:
      // ask for invalid objects.
      saveIdocAction.getInvalidObjectsModels(context).then((invalidObjects) => {
        // Then:
        // 1. we expect resulted invalidObjects to not contains the validObject.
        expect(invalidObjects[validObjectId] === undefined).to.be.true;

        // 2. we expect resulted invalidObjects to contains the invalidObject.
        expect(invalidObjects[invalidObjectId] === undefined).to.be.false;

        // 3. we expect resulted invalidObjects to contains the invalidExistingInContextObjectId.
        expect(invalidObjects[invalidExistingInContextObjectId] === undefined).to.be.false;
      });
    });

  });

  describe('#getInvalidObject', () => {
    it('should return properly object for "SaveDialogService" from instanceObject', () => {

      let validationModel = new ValidationModelBuilder().getModel();
      let viewModel = new ViewModelBuilder().addField('someField').getModel();
      let headers = {
        breadcrumb_header: '<span>Breadcrumb header</span>',
        compact_header: '<span>Compact header</span>',
        default_header: '<span>Default header</span>'
      };
      let models = {
        definitionId: INSTANCE_ONE_DEFINITION_ID,
        viewModel,
        validationModel,
        headers
      };
      let id = 'emf:0001';
      let modifiedObject = new InstanceObject(id, models);

      let result = SaveIdocAction.getInvalidObject(modifiedObject);

      expect(result.models.id).to.equal(id);
      expect(result.models.definitionId).to.equal(INSTANCE_ONE_DEFINITION_ID);
      expect(result.models.viewModel).to.equal(viewModel);
      expect(result.models.validationModel).to.deep.equal(validationModel);
      expect(result.models.headers).to.deep.equal(headers);
    });
  });

  describe('#setOperationSpecificProperties', () => {

    it('should set version mode to UPDATE by default if not passed through instance object models', () => {
      let objectToModify = {};
      saveIdocAction.setOperationSpecificProperties(objectToModify, new InstanceObject('emf:0001', {}));
      expect(objectToModify).to.deep.equal({$versionMode$: 'UPDATE'});
    });

    it('should set version mode if it is passed through instance object models', () => {
      let objectToModify = {};
      saveIdocAction.setOperationSpecificProperties(objectToModify, new InstanceObject('emf:0001', {$versionMode$: 'MINOR'}));
      expect(objectToModify).to.deep.equal({$versionMode$: 'MINOR'});
    });
  });

  function setupExistingInContextValidation(validationConfiguration = []) {

    let validationResult = {};
    validationConfiguration.forEach((configuration) => {
      validationResult[configuration.id] = configuration;
    });

    instanceContextService.validateExistingInContextAll.returns(PromiseStub.resolve(validationResult));
  }

  function createSearchResolverService() {
    return {
      resolve: sinon.spy((criteria) => {
        return PromiseStub.resolve(criteria);
      })
    };
  }

  function creteInstanceObject(id) {
    let validationModel = new ValidationModelBuilder().getModel();
    let viewModel = new ViewModelBuilder().addField('someField').getModel();
    let headers = {
      breadcrumb_header: '<span>Breadcrumb header</span>',
      compact_header: '<span>Compact header</span>',
      default_header: '<span>Default header</span>'
    };
    let models = {
      definitionId: INSTANCE_ONE_DEFINITION_ID,
      viewModel,
      validationModel,
      headers
    };

    return new InstanceObject(id, models);
  }

  function createIdocContext(objectId) {
    var objectId = objectId === null && objectId !== undefined ? objectId : 'emf:123456';
    return {
      getSharedObjects() {
        return [
          new InstanceObject(objectId, createModels(objectId), 'template.content'),
          new InstanceObject('emf:999888', createModels('emf:999888'), 'template.content')
        ];
      },
      getAllSharedObjects() {
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
        'default_header': 'emf:123456',
        'compact_header': 'emf:123456',
        'breadcrumb_header': 'emf:123456'
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
        }, {
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
    };
  }

  function mockCurrentObject(id, isPersisted, models) {
    return {
      getId: () => {
        return id;
      },
      isPersisted: () => {
        return isPersisted;
      },
      getModels: () => {
        return models;
      },
      getChangeset: () => {
        return {
          property1: 123
        };
      },
      models: {
        validationModel: {
          'emf:version': {
            value: '1.1'
          }
        }
      }
    };
  }
});
