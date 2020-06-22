import {PickerCreate} from 'search/picker/picker-create';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {ModelsService} from 'services/rest/models-service';

describe('PickerCreate', () => {

  var pickerCreate;
  var models = [{id: 1}, {id: 2}, {id: 3}, {parent: 4}, {parent: 5}];
  var opts = getDialogOptions(getCurrentObject().id);

  beforeEach(() => {
    PickerCreate.prototype.config = undefined;
    PickerCreate.prototype.context = {
      currentObjectId: getCurrentObject().id,
      getCurrentObjectId: function () {
        return getCurrentObject().id
      }
    }
  });

  it('should have correct default configuration', () => {
    pickerCreate = getPickerCreate();

    let config = {
      controls: {
        showCancel: false,
        showCreateMore: false
      },
      forceCreate: true,
      useContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_CREATE
    };

    expect(pickerCreate.config).to.deep.eq(config);
  });

  it('should the call main initializing methods by calling ngOnInit()', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig = sinon.spy();
    pickerCreate.initInstancePanelCallbacks = sinon.spy();

    pickerCreate.ngOnInit();
    expect(pickerCreate.initInstancePanelConfig.calledOnce).to.be.true;
    expect(pickerCreate.initInstancePanelCallbacks.calledOnce).to.be.true;
  });

  it('should call selectionHandler when an instance is created', () => {
    let instance = {id: 1};

    pickerCreate = getPickerCreate();
    pickerCreate.config.selectionHandler = sinon.spy();
    pickerCreate.onInstanceCreated(instance);

    let args = pickerCreate.config.selectionHandler.getCall(0).args;
    expect(pickerCreate.config.selectionHandler.calledOnce).to.be.true;
    expect(args[0]).to.deep.equal(instance);
  });

  it('should call getInstanceDialogConfig() with proper arguments when context is defined', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    expect(args[0]).to.deep.eq(opts);
  });

  it('should call getInstanceDialogConfig() with proper arguments when context is not defined', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.context = undefined;
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    let currOpts = getDialogOptions(undefined);
    expect(args[0]).to.deep.eq(currOpts);
  });

  it('should call getInstanceDialogConfig() with proper arguments when root context is not used', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.config.useContext = false;
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    let currOpts = getDialogOptions(undefined);
    expect(args[0]).to.deep.eq(currOpts);
  });

  it('should configure instancePanelConfig ', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig();

    let config = mockCreateInstance().getCreatePanelConfig();
    expect(pickerCreate.instancePanelConfig).to.deep.eq(config);
  });

  it('should call instance getCreatePanelConfig()', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let config = mockCreateInstance().getInstanceDialogConfig(opts);
    let args = createInstanceAction.getCreatePanelConfig.getCall(0).args;

    expect(args[0]).to.deep.eq(opts);
    expect(args[1]).to.deep.eq(config);
  });

  it('should call the create instance config methods properly', () => {
    pickerCreate = getPickerCreate();
    let createInstanceAction = pickerCreate.createPanelService;

    pickerCreate.initInstancePanelConfig();
    expect(createInstanceAction.getCreatePanelConfig.calledOnce).to.be.true;
    expect(createInstanceAction.getInstanceDialogConfig.calledOnce).to.be.true;
  });

  function getPickerCreate() {
    return new PickerCreate(mockCreateInstance(), mockWindowAdapter());
  }

  function mockCreateInstance() {
    return {
      getInstanceDialogConfig: sinon.spy(() => {
        return {
          models: 'models',
          dialogConfig: 'dialogConfig',
          suggestedPropertiesMap: {}
        };
      }),
      getCreatePanelConfig: sinon.spy(() => {
        return {
          config: {}
        };
      })
    };
  }

  function mockWindowAdapter(href) {
    return {
      location: {
        href: href || 'initialLocation'
      }
    };
  }

  function getCurrentObject() {
    let id = 123;
    let instance = new InstanceObject(id);
    instance.setContextPath([{id: id}]);
    return instance;
  }

  function getDialogOptions(id) {
    return {
      controls: {
        showCancel: false,
        showCreateMore: false
      },
      parentId: id,
      forceCreate: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_CREATE,
      returnUrl: mockWindowAdapter().location.href
    };
  }

});