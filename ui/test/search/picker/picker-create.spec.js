import {PickerCreate, NOTHING_TO_CREATE_MESSAGE} from 'search/picker/picker-create';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'idoc/idoc-context';
import {ModelsService} from 'services/rest/models-service';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {InstanceCreateConfigurationEvent} from 'create/instance-create-configuration-event';

describe('PickerCreate', () => {

  var pickerCreate;
  var models = [{id: 1}, {id: 2}, {id: 3}, {parent: 4}, {parent: 5}];
  var opts = getDialogOptions(getCurrentObject().id);

  beforeEach(() => {
    PickerCreate.prototype.config = undefined;
    PickerCreate.prototype.context = {
      currentObjectId: getCurrentObject().id,
      getCurrentObjectId: function() {
        return getCurrentObject().id
      }
    }
  });

  afterEach(() => {
    PickerCreate.prototype.config = undefined;
    PickerCreate.prototype.context = undefined;
  });

  it('should have correct default configuration', () => {
    pickerCreate = getPickerCreate();

    let config = {
      controls: {
        showCancel: false,
      },
      forceCreate: true,
      useRootContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_CREATE,
      nothingToCreateMessage: NOTHING_TO_CREATE_MESSAGE
    };

    expect(pickerCreate.config).to.deep.eq(config);
  });

  it('should the call main initializing methods by calling ngOnInit()', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.subscribeToInstanceConfiguration = sinon.spy();
    pickerCreate.subscribeToInstanceCreated = sinon.spy();
    pickerCreate.initInstancePanelConfig = sinon.spy();

    pickerCreate.ngOnInit();
    expect(pickerCreate.initInstancePanelConfig.calledOnce).to.be.true;
    expect(pickerCreate.subscribeToInstanceCreated.calledOnce).to.be.true;
    expect(pickerCreate.subscribeToInstanceConfiguration.calledOnce).to.be.true;
  });

  it('should unsubscribe from the event bus', () => {
    pickerCreate = getPickerCreate();

    let unsubscribeSpy = {
      unsubscribe: sinon.spy()
    };
    pickerCreate.eventbus = {
      subscribe: () => {
        return unsubscribeSpy
      }
    };

    pickerCreate.ngOnInit();
    pickerCreate.ngOnDestroy();

    expect(unsubscribeSpy.unsubscribe.calledTwice).to.be.true;
  });

  it('should call the selectionHandler when subscribing to InstanceCreatedEvent', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.config.selectionHandler = sinon.spy();
    pickerCreate.subscribeToInstanceCreated();

    let method = pickerCreate.eventbus.subscribe.getCall(0).args[1];
    method([{currentObject: {}}]);

    let args = pickerCreate.config.selectionHandler.getCall(0).args;
    expect(pickerCreate.config.selectionHandler.calledOnce).to.be.true;
    expect(args[0]).to.deep.eq({});
  });

  it('should subscribe to InstanceCreatedEvent', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.subscribeToInstanceCreated();

    let args = pickerCreate.eventbus.subscribe.getCall(0).args;
    expect(pickerCreate.eventbus.subscribe.calledOnce).to.be.true;
    expect(args[0]).to.deep.eq(InstanceCreatedEvent);
  });

  it('should not show instance panel when subscribing to InstanceCreateConfigurationEvent with empty models array', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.subscribeToInstanceConfiguration();

    let method = pickerCreate.eventbus.subscribe.getCall(0).args[1];
    method([{models: []}]);

    expect(pickerCreate.showPanel).to.be.false;
  });

  it('should show instance panel when subscribing to InstanceCreateConfigurationEvent existing models array', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.subscribeToInstanceConfiguration();

    let method = pickerCreate.eventbus.subscribe.getCall(0).args[1];
    method([{models: [1,2,3]}]);

    expect(pickerCreate.showPanel).to.be.true;
  });

  it('should subscribe to InstanceCreateConfigurationEvent', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.subscribeToInstanceConfiguration();

    let args = pickerCreate.eventbus.subscribe.getCall(0).args;
    expect(pickerCreate.eventbus.subscribe.calledOnce).to.be.true;
    expect(args[0]).to.deep.eq(InstanceCreateConfigurationEvent);
  });

  it('should call getInstanceDialogConfig() with proper arguments when context is defined', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig([{id: 1}, {parent: 2}]);

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
    pickerCreate.config.useRootContext = false;
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    let currOpts = getDialogOptions(undefined);
    expect(args[0]).to.deep.eq(currOpts);
  });

  it('should configure instancePanelConfig ', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.instancePanelConfig = {};
    pickerCreate.initInstancePanelConfig();

    let config = mockCreateInstance().getPropertiesConfig();
    expect(pickerCreate.instancePanelConfig).to.deep.eq(config);
  });

  it('should call instance getPropertiesConfig()', () => {
    pickerCreate = getPickerCreate();
    pickerCreate.initInstancePanelConfig();

    let createInstanceAction = pickerCreate.createPanelService;
    let config = mockCreateInstance().getInstanceDialogConfig(opts);
    let args = createInstanceAction.getPropertiesConfig.getCall(0).args;

    expect(args[0]).to.deep.eq(opts);
    expect(args[1]).to.deep.eq(config.models);
    expect(args[2]).to.deep.eq(config.dialogConfig);
    expect(args[3]).to.deep.eq(config.suggestedPropertiesMap);
  });

  it('should call the create instance config methods properly', () => {
    pickerCreate = getPickerCreate();
    let createInstanceAction = pickerCreate.createPanelService;

    pickerCreate.initInstancePanelConfig();
    expect(createInstanceAction.getPropertiesConfig.calledOnce).to.be.true;
    expect(createInstanceAction.getInstanceDialogConfig.calledOnce).to.be.true;
  });

  function getPickerCreate() {
    return new PickerCreate(mockEventBus({}), mockCreateInstance(), mockWindowAdapter());
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
      getPropertiesConfig: sinon.spy(() => {
        return {
          config: 'config'
        };
      })
    };
  }

  function mockEventBus(ev) {
    return {
      subscribe: sinon.spy((ev) => {
        return ev;
      }),
      unsubscribe: sinon.spy()
    };
  };

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
        showCancel: false
      },
      parentId: id,
      forceCreate: true,
      predefinedTypes: [],
      returnUrl: mockWindowAdapter().location.href
    };
  }

});