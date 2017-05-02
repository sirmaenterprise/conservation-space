import {InstanceCreatePanel} from 'create/instance-create-panel';
import {PromiseStub} from 'test/promise-stub';

describe('InstanceCreatePanel', function () {

  var instanceCreatePanel;
  var translateService;
  var notificationService;

  beforeEach(() => {
    translateService = mockTranslateService();
    notificationService = mockNotificationService();
    instanceCreatePanel = new InstanceCreatePanel(translateService, notificationService);
  });

  afterEach(() => {
    InstanceCreatePanel.prototype.config = undefined;
  });

  it('should set default configuration', function () {
    expect(instanceCreatePanel.createAnother).to.be.false;
    expect(instanceCreatePanel.config.purpose).to.exist;
    expect(instanceCreatePanel.config.forceCreate).to.be.false
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
      forceCreate: true
    };
    instanceCreatePanel = new InstanceCreatePanel(translateService, notificationService);
    expect(instanceCreatePanel.createAnother).to.be.true;
  });

  describe('isModelValid()', () => {
    it('should return true if no type selected', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: null,
          validationModel: null
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.true;
    });

    it('should return true if form is invalid (some mandatory field is empty)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: false
          }
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.true;
    });

    it('should return false if form is valid (all mandatory fields are filled)', () => {
      instanceCreatePanel.config.formConfig = {
        models: {
          definitionId: 'definition',
          validationModel: {
            isValid: true
          }
        }
      };
      expect(instanceCreatePanel.isModelValid()).to.be.false;
    });
  });

  describe('hasNoDefinition()', () => {
    it('should return true if no definition', () => {
      instanceCreatePanel.config.formConfig = {
        models: {definitionId: null}
      };
      expect(instanceCreatePanel.hasNoDefinition()).to.be.true;
    });

    it('should return false if has definition', () => {
      instanceCreatePanel.config.formConfig = {
        models: {definitionId: 'definition'}
      };
      expect(instanceCreatePanel.hasNoDefinition()).to.be.false;
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

  describe('execute()', () => {
    it('should invoke create if flag is true', () => {
      instanceCreatePanel.config.onCreate = sinon.spy(() => {
        return PromiseStub.resolve({
          headers: {
            breadcrumb_header: 'header'
          }
        });
      });
      instanceCreatePanel.createAnother = true;
      instanceCreatePanel.execute();
      expect(instanceCreatePanel.config.onCreate.calledOnce).to.be.true;
    });

    it('should invoke open if flag is false', () => {
      instanceCreatePanel.config.onOpen = sinon.spy(() => {
      });
      instanceCreatePanel.execute();
      expect(instanceCreatePanel.config.onOpen.calledOnce).to.be.true;
    });
  });
});

function mockTranslateService() {
  return {
    translateInstant: () => {
      return 'label ';
    }
  };
}

function mockNotificationService() {
  return {
    success: sinon.spy(() => {
    })
  };
}