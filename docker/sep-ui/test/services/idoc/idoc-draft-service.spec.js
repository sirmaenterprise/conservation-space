import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {MomentAdapter} from 'adapters/moment-adapter';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {StatusCodes} from 'services/rest/status-codes';
import base64 from 'common/lib/base64';

describe('IdocDraftService', () => {
  it('saveDraft should create draft with proper content', (done) => {
    let instanceRestService = {
      createDraft: () => {
        return Promise.resolve({
          data: {}
        });
      }
    };
    let idocDraftService = new IdocDraftService(instanceRestService, {}, IdocMocks.mockEventBus(), new MomentAdapter(), IdocMocks.mockConfiguration(), PromiseStub, undefined, IdocMocks.mockTranslateService());
    let createDraftSpy = sinon.spy(instanceRestService, 'createDraft');
    let publishSpy = sinon.spy(idocDraftService.eventbus, 'publish');
    let idocContext = {
      getAllSharedObjects: () => {
        let anObject = {
          isChanged: () => true,
          getChangeset: () => {
            propertyName: 'value'
          }
        };
        return [anObject];
      },
      getCurrentObjectId: function() {
        return this.currentObjectId;
      },
      getUUID: () => 'uuid'
    };
    idocDraftService.saveDraft(idocContext, '<div></div>').then(() => {
      expect(createDraftSpy.callCount).to.equal(1);
      expect(publishSpy.callCount).to.equal(1);
      done();
    }).catch(done);
  });

  it('loadDraft should resolve with loaded false if there is no draft', (done) => {
    let instanceRestService = {
      loadDraft: () => {
        return Promise.resolve({
          status: StatusCodes.SERVER_ERROR,
          data: {}
        });
      }
    };
    let idocDraftService = new IdocDraftService(instanceRestService, {}, IdocMocks.mockEventBus(), new MomentAdapter(), IdocMocks.mockConfiguration(), PromiseAdapterMock.mockAdapter(), undefined, IdocMocks.mockTranslateService());
    let idocContext = {
      currentObjectId: 'emf:123456',
      getCurrentObjectId: function() {
        return this.currentObjectId;
      }
    };
    idocDraftService.loadDraft(idocContext).then((result) => {
      expect(result.loaded).to.be.false;
      done();
    });
  });

  describe('onConfirmationButtonClick', () => {
    it('should resolve with loaded false if Discard button is clicked', (done) => {
      let instanceRestService = {
        deleteDraft: () => {
          return Promise.resolve({
            data: {}
          });
        }
      };
      let idocDraftService = new IdocDraftService(instanceRestService, {},IdocMocks.mockEventBus(), new MomentAdapter(), IdocMocks.mockConfiguration(), PromiseAdapterMock.mockAdapter(), undefined, IdocMocks.mockTranslateService());

      let deleteDraftSpy = sinon.spy(instanceRestService, 'deleteDraft');
      let publishSpy = sinon.spy(idocDraftService.eventbus, 'publish');
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      let idocContext = {
        currentObjectId: 'emf:123456',
        getCurrentObjectId: function() {
          return this.currentObjectId;
        },
        getUUID: () => 'uuid'
      };
      idocDraftService.onConfirmationButtonClick('DISCARD', undefined, dialogConfig, idocContext).then((result) => {
        expect(deleteDraftSpy.callCount).to.equals(1);
        expect(publishSpy.callCount).to.equals(1);
        expect(dialogConfig.dismiss.callCount).to.equals(1);
        expect(result.loaded).to.be.false;
        done();
      });
    });

    it('should resolve with loaded true if Resume button is clicked', (done) => {
      let draftData = {
        changesets: {
          'emf:123456': {
            property1: 'value1',
            property2: ['value1', 'value2']
          }
        }
      };
      let contentRestService = {
        getContent: () => {
          return Promise.resolve({
            data: `<div data-draft-data='${base64.encode(JSON.stringify(draftData))}'></div>`
          });
        }
      };
      let idocDraftService = new IdocDraftService({}, contentRestService, IdocMocks.mockEventBus(), new MomentAdapter(), IdocMocks.mockConfiguration(), PromiseAdapterMock.mockAdapter(), undefined, IdocMocks.mockTranslateService());
      let sharedObjects = {
        data: [{
          id: 'emf:123456',
          models: {
            validationModel: {
              property1: {
                value: 'old value'
              },
              property2: {
                value: []
              }
            },
            viewModel: {
              fields: []
            }
          }
        }]
      };
      let idocContext = {
        getSharedObjects: () => {
          return Promise.resolve(sharedObjects);
        }
      };
      let dialogConfig = {
        dismiss: sinon.spy()
      };
      idocDraftService.onConfirmationButtonClick('RESUME', undefined, dialogConfig, idocContext).then((result) => {
        expect(dialogConfig.dismiss.callCount).to.equals(1);
        expect(result.loaded).to.be.true;
        expect(result.content).to.equals('<div></div>');
        expect(sharedObjects.data[0].models.validationModel.property1.value).to.equals('value1');
        expect(sharedObjects.data[0].models.validationModel.property2.value).to.eql(['value1', 'value2']);
        done();
      });
    });
  });

  it('deleteDraft should call service to delete draft', (done) => {
    let instanceRestService = {
      deleteDraft: () => {
        return Promise.resolve({
          data: {}
        });
      }
    };
    let idocDraftService = new IdocDraftService(instanceRestService, {}, IdocMocks.mockEventBus(), new MomentAdapter(), IdocMocks.mockConfiguration(), PromiseStub, undefined, IdocMocks.mockTranslateService());
    let deleteDraftSpy = sinon.spy(instanceRestService, 'deleteDraft');
    let publishSpy = sinon.spy(idocDraftService.eventbus, 'publish');
    let idocContext = {
      currentObjectId: 'emf:123456',
      getCurrentObjectId: function() {
        return this.currentObjectId;
      },
      getUUID: () => 'uuid'
    };
    idocDraftService.deleteDraft(idocContext).then(() => {
      expect(deleteDraftSpy.callCount).to.equal(1);
      expect(deleteDraftSpy.getCall(0).args[0]).to.equal('emf:123456');
      expect(publishSpy.callCount).to.equal(1);
      done();
    }).catch(done);
  });
});
