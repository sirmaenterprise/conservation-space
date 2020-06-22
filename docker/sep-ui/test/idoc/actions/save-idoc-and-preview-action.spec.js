import {IdocMocks} from '../idoc-mocks';
import {SaveIdocAndPreviewAction} from 'idoc/actions/save-idoc-and-preview-action';
import {InstanceObject} from 'models/instance-object';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {STATE_PARAM_MODE, STATE_PARAM_ID, MODE_PREVIEW, IDOC_STATE} from 'idoc/idoc-constants';

describe('Save idoc and preview action', () => {

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
          {id: 'emf:123456',
            properties:{
              'emf:version': '1.1'
            }},
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

  beforeEach(() => {
    spyUpdate.reset();
    spyCreate.reset();
    eventbus.publish.reset();
    // backup the original function and restore it after each test because it is overriden for som eof them which
    // causes the test for that function to not work properly
    getSaveObjectPayloadFunction = SaveIdocAndPreviewAction.prototype.getSaveObjectPayload;
  });

  afterEach(() => {
    SaveIdocAndPreviewAction.prototype.getSaveObjectPayload = getSaveObjectPayloadFunction;
    spyUpdateAll.reset();
    spyUpdate.reset();
    spyCreate.reset();
  });

  describe('#save', () => {
    it('should stop draft interval and delete draft if object already exists', (done) => {
      SaveIdocAndPreviewAction.prototype.getSaveObjectPayload = () => {
        return PromiseStub.resolve([
          {definitionId: 'idocfortest'}
        ]);
      };
      SaveIdocAndPreviewAction.prototype.currentObject = mockCurrentObject(id, true);
      var handler = createIdocSaveHandler({
        validObjects: true,
        eventbus: eventbus,
        instanceService: instanceService
      });
      handler.idocDraftService.deleteDraft = () => PromiseStub.resolve();
      let deleteDraftSpy = sinon.spy(handler.idocDraftService, 'deleteDraft');
      let context = {
        idocContext: handler.context,
        currentObject: SaveIdocAndPreviewAction.prototype.currentObject,
        idocPageController : { stopDraftInterval: sinon.spy() }
      };

      handler.afterUpdate = () => {};

      handler.save(context, {}).then(() => {
        expect(context.idocPageController.stopDraftInterval.callCount).to.equal(1);
        expect(deleteDraftSpy.callCount).to.equal(1);
        done();
      }).catch(done);
    });
  });

  it('#reloadIdoc should unlock document, notify for save and navigate to idoc in preview mode', () => {
    let handler = createIdocSaveHandler({});

    handler.buildActionPayload = () => {};

    let context = {
      idocContext: handler.context,
      currentObject: handler.currentObject,
      idocPageController : { stopDraftInterval: sinon.spy() }
    };

    let unlockSpy = sinon.spy(handler.actionsService, 'unlock');
    let notificationSuccessSpy = sinon.spy(handler.notificationService, 'success');
    let setStateParamSpy = sinon.spy(handler.stateParamsAdapter, 'setStateParam');
    let navigateSpy = sinon.spy(handler.router, 'navigate');

    let response = {
      data: [{
        properties: {
          type: {
            text: 'instance'
          }
        }
      }]
    };

    handler.reloadIdoc(response, 'emf:123456', context);
    expect(unlockSpy.callCount).to.equals(1);
    expect(unlockSpy.getCall(0).args[0]).to.equals('emf:123456');
    expect(notificationSuccessSpy.callCount).to.equals(1);
    expect(setStateParamSpy.callCount).to.equals(2);
    expect(setStateParamSpy.getCall(0).args).to.eql([STATE_PARAM_ID, 'emf:123456']);
    expect(setStateParamSpy.getCall(1).args).to.eql([STATE_PARAM_MODE, MODE_PREVIEW]);
    expect(navigateSpy.callCount).to.equals(1);
    expect(navigateSpy.getCall(0).args[0]).to.equals(IDOC_STATE);
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
      models:{
        validationModel:{
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
    var objectId1 = sharedObjectId === null && sharedObjectId !== undefined ? sharedObjectId : 'emf:123456';
    SaveIdocAndPreviewAction.prototype.context = {
      getSharedObjects: function () {
        return [
          new InstanceObject(objectId1, createModels(objectId1), 'template.content'),
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
    var searchResolverService = {
      resolve: sinon.spy((criteria) => {
        return PromiseStub.resolve(criteria);
      })
    };
    var userServiceMock = {
      getCurrentUserId: () => { return 'emf:123456' }
    };
    return new SaveIdocAndPreviewAction(config.eventbus || IdocMocks.mockEventBus(), config.instanceService || IdocMocks.mockInstanceRestService(),
      IdocMocks.mockLogger(), validationService2, config.notificationService || notificationService, IdocMocks.mockTranslateService(),
      config.stateParamsAdapter || IdocMocks.mockStateParamsAdapter(), config.router || IdocMocks.mockRouter(),
      config.dialogService || dialogService2, IdocMocks.mockActionsService(), searchResolverService, PromiseAdapterMock.mockAdapter(),
      IdocMocks.mockIdocDraftService(), userServiceMock);
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
});