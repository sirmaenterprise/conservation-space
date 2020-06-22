import {PickerUpload} from 'search/picker/picker-upload';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {ModelsService} from 'services/rest/models-service';

describe('PickerUpload', () => {

  var pickerUpload;
  var models = [{id: 1}, {id: 2}, {id: 3}, {parent: 4}, {parent: 5}];
  var opts = getDialogOptions(getCurrentObject().id);

  beforeEach(() => {
    PickerUpload.prototype.context = {
      getCurrentObjectId: sinon.spy(() => {
        return getCurrentObject().id;
      })
    };
  });

  it('should have correct default configuration', () => {
    pickerUpload = getPickerUpload();

    let config = {
      controls: {
        showCancel: false
      },
      useContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_UPLOAD
    };

    expect(pickerUpload.config).to.deep.eq(config);
  });

  it('should the call main initializing methods by calling ngOnInit()', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.initUploadPanelConfig = sinon.spy();
    pickerUpload.initUploadPanelCallbacks = sinon.spy();

    pickerUpload.ngOnInit();
    expect(pickerUpload.initUploadPanelConfig.calledOnce).to.be.true;
    expect(pickerUpload.initUploadPanelCallbacks.calledOnce).to.be.true;
  });

  it('should call selectionHandler when an instance is uploaded', () => {
    let file = {id: 1};

    pickerUpload = getPickerUpload();
    pickerUpload.config.selectionHandler = sinon.spy();
    pickerUpload.onFileUploaded(file);

    let args = pickerUpload.config.selectionHandler.getCall(0).args;
    expect(pickerUpload.config.selectionHandler.calledOnce).to.be.true;
    expect(args[0]).to.deep.equal(file);
  });

  it('should call getInstanceDialogConfig() with proper arguments when context is defined', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.initUploadPanelConfig([{id: 1}, {parent: 2}]);

    let createInstanceAction = pickerUpload.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    expect(args[0]).to.deep.eq(opts);
  });

  it('should call getInstanceDialogConfig() with proper arguments when context is not defined', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.context = undefined;
    pickerUpload.initUploadPanelConfig();

    let createInstanceAction = pickerUpload.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    let currOpts = getDialogOptions(undefined);
    expect(args[0]).to.deep.eq(currOpts);
  });

  it('should call getInstanceDialogConfig() with proper arguments when root context is not used', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.config.useContext = false;
    pickerUpload.initUploadPanelConfig();

    let createInstanceAction = pickerUpload.createPanelService;
    let args = createInstanceAction.getInstanceDialogConfig.getCall(0).args;

    let currOpts = getDialogOptions(undefined);
    expect(args[0]).to.deep.eq(currOpts);
  });

  it('should configure uploadPanelConfig ', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.initUploadPanelConfig();

    let config = mockCreateInstance().getUploadPanelConfig();
    expect(pickerUpload.uploadPanelConfig).to.deep.eq(config);
  });

  it('should call instance getUploadPanelConfig()', () => {
    pickerUpload = getPickerUpload();
    pickerUpload.initUploadPanelConfig();

    let createInstanceAction = pickerUpload.createPanelService;
    let config = mockCreateInstance().getInstanceDialogConfig(opts);
    let args = createInstanceAction.getUploadPanelConfig.getCall(0).args;

    expect(args[0]).to.deep.eq(opts);
    expect(args[1]).to.deep.eq(config);
  });

  it('should call the create instance config methods properly', () => {
    pickerUpload = getPickerUpload();
    let createInstanceAction = pickerUpload.createPanelService;

    pickerUpload.initUploadPanelConfig();
    expect(createInstanceAction.getUploadPanelConfig.calledOnce).to.be.true;
    expect(createInstanceAction.getInstanceDialogConfig.calledOnce).to.be.true;
  });

  function getPickerUpload() {
    return new PickerUpload(mockCreateInstance(), mockWindowAdapter());
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
      getUploadPanelConfig: sinon.spy(() => {
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
        showCancel: false
      },
      parentId: id,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_UPLOAD,
      returnUrl: mockWindowAdapter().location.href
    };
  }

});