import {PromiseStub} from 'test/promise-stub';

// TODO: track these mocking functions and when all are replaced with proper usage of the stub utility, then eventually delete this class
export class BpmMockUtil {

  static mockTranslateService() {
    return {
      translateInstantWithInterpolation: sinon.spy((value, object) => {
        return 'Test String';
      }),
      translateInstant: sinon.spy((value) => {
        return ' created';
      })
    };
  }

  static mockNotificationService() {
    return {
      success: sinon.spy((value) => {
        return value;
      }),
      warning: sinon.spy((value) => {
        return value;
      }),
      error: sinon.spy((value) => {
        return value;
      })
    };
  }

  static mockSaveDialogService() {
    return {
      openDialog: sinon.spy((value) => {
        return PromiseStub.resolve();
      })
    }
  }

  static mockBpmService() {
    return {
      buildBPMActionPayload: sinon.spy((id, actionDefinition, models, op) => {
        let payload = {};
        return payload;
      }),
      loadModel: sinon.spy((id, action) => {
        let response = {
          data: {
            item1: {
              model: {
                definitionLabel: 'label',
                viewModel: {
                  fields: {
                    isMandatory: true
                  }
                }
              },
              instance: {
                id: "item1",
                properties: []
              }
            },
            'emf:id': {
              model: {
                definitionLabel: 'label',
                viewModel: {
                  fields: {
                    isMandatory: true
                  }
                }
              },
              instance: {
                id: "emf:id",
                properties: []
              }
            }
          }
        };
        return PromiseStub.resolve(response);
      }),
      releaseBpm: sinon.spy((currentObjectId, payload) => {
        let response = {
          config: {
            data: {
              currentInstance: 'id',
              id: 'id'
            }
          }
        };
        return PromiseStub.resolve(response);
      }),
      executeTransition: sinon.spy((currentObjectId, payload) => {
        let response = {
          config:{
            data: {
              currentInstance: 'id',
              id: 'id'
            }
          }, data: [{
            headers: {
              breadcrumb_header: ' BR header'
            }
          }]
        };
        return PromiseStub.resolve(response);
      }),
      claimBpm: sinon.spy((currentObjectId, payload) => {
        return PromiseStub.resolve({
          config: {
            data: {
              currentInstance: 'id',
              id: 'id'
            }
          }
        });
      }),
      startBpm: sinon.spy((currentObjectId, payload) => {
        let response = {
        config:{
          data: {
            currentInstance: 'id',
              id: 'id'
          }
        },data: [{
            headers: {
              breadcrumb_header: ' BR header'
            }
          }]
        };
        return PromiseStub.resolve(response);
      }),
      stopBpm: sinon.spy((currentObjectId, payload) => {
        let response = {
        config:{
          data: {
            currentInstance: 'id',
              id: 'id'
          }
        },
          data: [{
            headers: {
              breadcrumb_header: ' BR header'
            }
          }]
        };
        return PromiseStub.resolve(response);
      })
    }
  }

  static mockValidationService() {
    return {
      validate: sinon.spy((model, viewModel, id) => {
        return PromiseStub.resolve(true);
      }),
      init: () => {
        return PromiseStub.resolve();
      }
    }
  }

  static mockLoggerService() {
    return {
      error: sinon.spy((log) => {
      })
    }
  }

  static mockActionsService() {
    return {  };
  }

  static mockInstanceRestService(){
    return { };
  }

  static  mockEventBus() {
    return {
      publish: sinon.spy((event) => {

      })
    };
  }

  static  mockPromiseAdapter() {
    return { };
  }
}