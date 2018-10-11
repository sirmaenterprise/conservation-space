import {InstanceCreatePanel} from 'create/instance-create-panel';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {EventEmitter} from 'common/event-emitter';
import {ModelUtils} from 'models/model-utils';

describe('InstanceCreatePanel', function () {

  let instanceCreatePanel;
  let translateService;
  let notificationService;
  let eventEmitter;

  beforeEach(() => {
    translateService = mockTranslateService();
    notificationService = stub(NotificationService);
    eventEmitter = stub(EventEmitter);
    InstanceCreatePanel.prototype.config = {
      eventEmitter,
      formConfig: {
        models: {}
      }
    };
    instanceCreatePanel = new InstanceCreatePanel(translateService, notificationService);
  });

  afterEach(() => {
    InstanceCreatePanel.prototype.config = undefined;
  });

  it('should set default configuration', function () {
    expect(instanceCreatePanel.createAnother).to.be.false;
    expect(instanceCreatePanel.config.purpose).to.exist;
    expect(instanceCreatePanel.config.forceCreate).to.be.false;
    expect(instanceCreatePanel.config.controls).to.exist;
    expect(instanceCreatePanel.config.predefinedTypes).to.eq(undefined);
    expect(instanceCreatePanel.config.controls).to.deep.eq({
      showCreate: true,
      showCancel: true,
      showCreateMore: true
    });
  });

  it('should create object if forceCreate is true', () => {
    InstanceCreatePanel.prototype.config = {
      forceCreate: true,
      formConfig: {
        models: {}
      }
    };
    instanceCreatePanel = new InstanceCreatePanel(translateService, notificationService);
    expect(instanceCreatePanel.createAnother).to.be.true;
  });

  describe('isModelValid()', () => {
    it('should return false if no type selected', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: null,
          validationModel: null
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.false;
    });

    it('should return false if form is invalid (some mandatory field is empty)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: false
          }
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.false;
    });

    it('should return true if form is valid (all mandatory fields are filled)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.true;
    });
  });

  describe('hasNoDefinition()', () => {
    it('should return true if no definition', () => {
      instanceCreatePanel.config.formConfig = {
        models: {definitionId: null}
      };
      expect(instanceCreatePanel.hasNoDefinition()).to.be.true;
    });

    it('should return false if has definition and if form is valid (all mandatory fields are filled)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };
      expect(instanceCreatePanel.hasNoDefinition()).to.be.false;
    });

    it('should return true if has definition and if form is invalid (some mandatory field is empty)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {definitionId: 'definition',
          validationModel: {
            isValid: false
          }
        }
      };
      expect(instanceCreatePanel.hasNoDefinition()).to.be.true;
    });
  });

  describe('isCreateDisabled()', () => {
    it('should disable create when creating instance', () => {
      instanceCreatePanel.isCreating = true;

      instanceCreatePanel.config.disableCreate = false;
      instanceCreatePanel.createAnother = true;
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };
      expect(instanceCreatePanel.isCreateDisabled()).to.be.true;
    });

    it('should disable create when disableCreate property is true', () => {
      instanceCreatePanel.config.disableCreate = true;

      instanceCreatePanel.isCreating = false;
      instanceCreatePanel.createAnother = true;
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };
      expect(instanceCreatePanel.isCreateDisabled()).to.be.true;
    });

    it('should disable create when the model is not valid', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: false
          }
        }
      };

      instanceCreatePanel.config.disableCreate = false;
      instanceCreatePanel.isCreating = false;
      instanceCreatePanel.createAnother = true;

      expect(instanceCreatePanel.isCreateDisabled()).to.be.true;
    });

    it('should disable create when has no definition', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: null,
          validationModel: {
            isValid: true
          }
        }
      };

      instanceCreatePanel.config.disableCreate = false;
      instanceCreatePanel.isCreating = false;
      instanceCreatePanel.createAnother = false;

      expect(instanceCreatePanel.isCreateDisabled()).to.be.true;
    });

    it('should enable create when has definition, disableCreate is set to false and is not creating new instance', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };

      instanceCreatePanel.config.disableCreate = false;
      instanceCreatePanel.isCreating = false;
      instanceCreatePanel.createAnother = false;

      expect(instanceCreatePanel.isCreateDisabled()).to.be.false;
    });

    it('should enable create when the model is valid, disableCreate is set to false and is not creating new instance', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };

      instanceCreatePanel.config.disableCreate = false;
      instanceCreatePanel.isCreating = false;
      instanceCreatePanel.createAnother = true;

      expect(instanceCreatePanel.isCreateDisabled()).to.be.false;
    });

  });

  describe('create()', () => {
    it('should invoke the configured function if passed in config', () => {
      instanceCreatePanel.config.onCreate = sinon.spy(() => {
        return PromiseStub.resolve({
          headers: {
            breadcrumb_header: 'header'
          }
        });
      });

      instanceCreatePanel.create();
      expect(notificationService.success.calledOnce).to.be.true;
      expect(instanceCreatePanel.config.onCreate.calledOnce).to.be.true;
      expect(instanceCreatePanel.createdObjectHeader).to.equal('label header');
    });

    it('should not have function if not passed in config', () => {
      expect(instanceCreatePanel.config.onCreate).to.not.exist;
      instanceCreatePanel.create();
      expect(instanceCreatePanel.config.onCreate).to.not.exist;
    });

    it('should call open in new tab', () => {
      instanceCreatePanel.config.onCreate = sinon.spy(() => {
        return PromiseStub.resolve({
          headers: {
            breadcrumb_header: 'header'
          }
        });
      });
      instanceCreatePanel.config.onOpenInNewTab = sinon.spy(() => {
        return PromiseStub.resolve({});
      });
      instanceCreatePanel.createAnother = false;
      instanceCreatePanel.config.openInNewTab = true;
      instanceCreatePanel.create();
      expect(instanceCreatePanel.config.onOpenInNewTab.calledOnce).to.be.true;
    });


    it('should set flag that object creation is in progress', () => {
      instanceCreatePanel.config.onCreate = sinon.spy(() => {
        return PromiseStub.resolve({
          headers: {
            breadcrumb_header: 'header'
          }
        });
      });

      expect(instanceCreatePanel.isCreating).to.not.exist;
      instanceCreatePanel.create();
      expect(instanceCreatePanel.isCreating).to.be.false;
    });

    it('should populate last template used when creating more instances', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            'emf:hasTemplate': {
              value: {
                results: ['emf:123456']
              }
            }
          }
        }
      };

      instanceCreatePanel.createAnother = true;
      instanceCreatePanel.config.onCreate = sinon.spy(() => {
        return PromiseStub.resolve({
          headers: {
            breadcrumb_header: 'header'
          },
          properties: {
            'emf:hasTemplate': {
              results: ['emf:123456']
            }
          }
        });
      });

      let modelUtilSpy = sinon.spy(ModelUtils, 'updateObjectProperty');
      expect(instanceCreatePanel.createTeplateUsed).to.not.exist;
      instanceCreatePanel.create();
      expect(modelUtilSpy.calledOnce).to.be.false;
      expect(instanceCreatePanel.createTeplateUsed).to.eql('emf:123456');

      instanceCreatePanel.config.formConfig.models.validationModel['emf:hasTemplate'].value.results = [];
      instanceCreatePanel.create();
      expect(modelUtilSpy.calledOnce).to.be.true;
      expect(instanceCreatePanel.createTeplateUsed).to.eql('emf:123456');
      modelUtilSpy.restore();
    });
  });

  describe('open()', () => {
    it('should invoke the configured function', () => {
      instanceCreatePanel.config.onOpen = sinon.spy(() => {
      });
      instanceCreatePanel.open();
      expect(instanceCreatePanel.config.onOpen.calledOnce).to.be.true;
    });

    it('should not have function if not passed in config', () => {
      expect(instanceCreatePanel.config.onOpen).to.not.exist;
      instanceCreatePanel.open();
      expect(instanceCreatePanel.config.onOpen).to.not.exist;
    });
  });

  describe('cancel()', () => {
    it('should invoke the configured function', () => {
      instanceCreatePanel.config.onCancel = sinon.spy(() => {
      });
      instanceCreatePanel.cancel();
      expect(instanceCreatePanel.config.onCancel.calledOnce).to.be.true;
    });
  });

  describe('instanceCreatedCallback', () => {
    it('should correctly call on instance created callback', () => {
      let instance = {id: 1};
      instanceCreatePanel.config.instanceCreatedCallback = sinon.spy();
      instanceCreatePanel.callOnInstanceCreatedCallback(instance);

      expect(instanceCreatePanel.config.instanceCreatedCallback.calledOnce);
      //check if the arguments to the callback are passed correctly
      let args = instanceCreatePanel.config.instanceCreatedCallback.getCall(0).args;
      expect(args[0]).to.deep.equal(instance);
    });
  });

  function mockTranslateService() {
    let translateService = stub(TranslateService);
    translateService.translateInstant.returns('label ');
    return translateService;
  }
});