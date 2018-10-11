import { IdocMocks } from '../idoc-mocks';
import { SaveIdocAndContinueAction } from 'idoc/actions/save-idoc-and-continue-action';
import { PromiseStub } from 'test/promise-stub';
import { PromiseAdapterMock } from 'test/adapters/angular/promise-adapter-mock';

describe('Save and continue action', () => {

  describe('#reloadIdoc', () => {
    it('should not reload idoc page', () => {
      let notificationService = {
        success: () => {
        }
      };
      let spySuccess = sinon.spy(notificationService, 'success');
      let config = {
      };
      var validationService2 = {
        validate: () => {
          return config.validObjects;
        },
        init: () => {
          return PromiseStub.resolve();
        }
      };

      var dialogService2 = {
        openDialog: () => {
          return PromiseStub.resolve();
        }
      };

      var searchResolverService = {
        resolve: sinon.spy((criteria) => {
          return PromiseStub.resolve(criteria);
        })
      };
      let saveIdocAndContinue = new SaveIdocAndContinueAction(config.eventbus || IdocMocks.mockEventBus(), config.instanceService || IdocMocks.mockInstanceRestService(),
        IdocMocks.mockLogger(), validationService2, config.notificationService || notificationService, IdocMocks.mockTranslateService(),
        config.stateParamsAdapter || IdocMocks.mockStateParamsAdapter(), config.router || IdocMocks.mockRouter(),
        config.dialogService || dialogService2, IdocMocks.mockActionsService(), searchResolverService, PromiseAdapterMock.mockAdapter(),
        IdocMocks.mockIdocDraftService());

      let currentObjectModels = {
        validationModel: {
          'emf:version': {
            value: '1.0'
          }
        }
      };
      let context = {
        currentObject: {
          models: currentObjectModels,
          mergePropertiesIntoModel: sinon.spy(),
          mergeHeadersIntoModel: sinon.spy(),
          setContent: sinon.spy(),
          getModels: () => currentObjectModels
        },
        idocActionsController: {
          disableSaveButton: sinon.spy()
        }
      };

      let response = {
        data: [{
          properties: {
            'emf:version': {
              value: '1.1'
            },
            type: {
              text: 'instance'
            }
          }
        }],
        config: {
          data: [{
            content: 'content'
          }]
        }
      };
      saveIdocAndContinue.reloadIdoc(response, {}, context);
      expect(spySuccess.callCount, 'Notification success should be risen').to.equal(1);
      expect(context.currentObject.mergePropertiesIntoModel.callCount).to.equal(1);
    });
  });
});