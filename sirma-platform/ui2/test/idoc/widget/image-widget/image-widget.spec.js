import {ImageWidget, MiradorViewer, NO_IMAGES_SELECTED} from 'idoc/widget/image-widget/image-widget';
import miradorStaticConfigZenMode from 'idoc/widget/image-widget/mirador-integration/mirador-static-config-zen-mode.json!'
import miradorStaticConfig from 'idoc/widget/image-widget/mirador-integration/mirador-static-config.json!'
import miradorEndpointConfig from 'idoc/widget/image-widget/mirador-integration/annotation-endpoint-config.json!'
import {Eventbus} from 'services/eventbus/eventbus';
import {EventEmitter} from 'common/event-emitter';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {AnnotationListLoadedEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {InstanceObject} from 'models/instance-object';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {ResizeDetectorAdapterMock} from 'test/adapters/resize-detector-adapter-mock';

describe('ImageWidget', () => {

  let objectSelectorHelper = {
    getSelectedObjects: function (config) {
      return new Promise(function (resolve) {
        if (config) {
          resolve([1, 2]);
        }
        else {
          resolve([]);
        }
      })
    },
    isModeling: () => {
      return false;
    }
  };

  let element = {
    find: function () {
      return [{
        addEventListener: function () {
        }
      }];
    }
  };

  let functionsMock = {
    contents: function () {
      return {
        find: function (selector) {
          if (selector === '.layout-slot') {
            return $(LAYOUT_SLOT);
          }
          return functionsMock;
        }
      };
    },
    eq: function () {
      return functionsMock;
    },
    find: function () {
      return functionsMock;
    },
    hide: function () {
      return {};
    },
    width: function () {
      return {};
    },
    height: function () {
      return {};
    },
    append: function () {
      return {};
    },
    detach: function () {
      return functionsMock;
    },
    css: function () {
    },
    finish: function () {
    },
    on: function (eventName, func) {
      func();
    },
    finish: function () {
    },
    each: function () {
    },
    length: 0,
    0: {
      contentWindow: {
        $: function (sel) {
          return functionsMock;
        }
      }
    }
  };

  let control = {
    saveConfig: sinon.spy(),
    getId: function () {
      return 'test_id';
    },
    element: {
      find: function () {
        return functionsMock;
      }
    },
    storeDataInAttribute: sinon.spy(),
    getDataFromAttribute: function () {
      return {
        manifest: 'test',
        config: {}
      }
    }
  };

  let widget;
  let config = {
    miradorCurrentConfig: {
      windowObjects: [1]
    }
  };

  ImageWidget.prototype.control = control;
  ImageWidget.prototype.config = config;

  beforeEach(() => {
    createImageWidget();
  });

  it('annotationListLoadedHandler should handle printing when annotation list loaded event is fired for all slots', () => {
    widget.element.find = () => ({
      contents: () => ({
        find: () => ({
          length: 1
        })
      })
    });
    let handlePrintModeSpy = sinon.stub(widget, 'handlePrintMode');
    widget.numberOfSlots = 1;
    widget.annotationListLoadedHandler();
    expect(handlePrintModeSpy.callCount).to.equal(1);
  });

  it('annotationListLoadedHandler should publish widget ready event if mode is not print', () => {
    widget.element.find = () => ({
      contents: () => ({
        find: () => ({
          length: 1
        })
      })
    });
    widget.locationAdapter = IdocMocks.mockLocationAdapter('/#/idoc/emf:123456');
    let handlePrintModeSpy = sinon.stub(widget, 'handlePrintMode');
    let publishWidgetReadyEventSpy = sinon.stub(widget, 'publishWidgetReadyEvent');
    widget.numberOfSlots = 1;
    widget.annotationListLoadedHandler();
    expect(handlePrintModeSpy.callCount).to.equal(0);
    expect(publishWidgetReadyEventSpy.callCount).to.equal(1);
  });

  it('buildPrintLayout should create proper DOM structure based on Mirador\'s config', () => {
    let parentElement = $('<div />');
    let config = {
      type: 'row',
      id: 'row-0',
      children: [
        {
          type: 'column',
          id: 'column-0-0',
          children: [
            {
              type: 'row',
              id: 'row-0-0-0',
              dx: 100,
              dy: 20
            },
            {
              type: 'row',
              id: 'row-0-0-1',
              dx: 100,
              dy: 25
            },
            {
              type: 'row',
              id: 'row-0-0-2',
              dx: 100,
              dy: 50
            }
          ]
        },
        {
          type: 'column',
          id: 'column-0-1',
          dx: 100,
          dy: 20
        }
      ]
    };
    widget.buildPrintLayout(parentElement, config, 1);
    expect(parentElement.html()).to.equal('<div class="image-widget-layout-row row" data-layout-slot-id="row-0"><div class="image-widget-layout-column col-xs-1" data-layout-slot-id="column-0-0" style="width: 50%;"><div class="image-widget-layout-row row" data-layout-slot-id="row-0-0-0" style="position: relative; padding-bottom: 20%;"></div><div class="image-widget-layout-row row" data-layout-slot-id="row-0-0-1" style="position: relative; padding-bottom: 25%;"></div><div class="image-widget-layout-row row" data-layout-slot-id="row-0-0-2" style="position: relative; padding-bottom: 50%;"></div></div><div class="image-widget-layout-column col-xs-1" data-layout-slot-id="column-0-1" style="width: 50%; position: relative; padding-bottom: 10%;"></div></div>');
  });

  it('handlePrintMode mode should fire WidgetReadyEvent when widget is loaded and ready for print', () => {
    let eventbus = new Eventbus();
    let spyPublish = sinon.spy(eventbus, 'publish');
    createImageWidget(eventbus);

    widget.miradorViewer = {
      getCurrentConfig: () => {
        return {
          layout: '{ "type": "row", "id": "row-0" }'
        }
      }
    };
    widget.handlePrintMode();

    expect(spyPublish.callCount).to.equal(1);
    expect(spyPublish.getCall(0).args[0]).to.eql(new WidgetReadyEvent({
      widgetId: 'test_id'
    }));
  });

  it('should poll twice when widget is in `image-view` mode', () => {
    createImageWidget();
    widget.config.lockWidget = true;
    widget.miradorViewer = {
      getCurrentConfig: () => {
        return {
          layout: '{ "type": "column", "id": "row-0" }'
        }
      }
    };

    sinon.spy(widget, 'handleCanvasViewPrintMode');
    sinon.spy(widget, '$interval');
    widget.handlePrintMode();
    // interval should be used twice,
    // to poll for canvas and annotation button
    expect(widget.$interval.calledTwice).to.be.true;
    expect(widget.handleCanvasViewPrintMode.calledOnce).to.be.true;
    expect(widget.handleCanvasViewPrintMode.getCall(0).args[1]).to.equal('test-slot-id');
  });

  it('shouldnt poll twice when widget is in `image-view` mode if image widget is not locked', () => {
    createImageWidget();
    widget.config.lockWidget = false;
    widget.miradorViewer = {
      getCurrentConfig: () => {
        return {
          layout: '{ "type": "column", "id": "row-0" }'
        }
      }
    };

    sinon.spy(widget, 'handleCanvasViewPrintMode');
    sinon.spy(widget, '$interval');
    widget.handlePrintMode();
    // interval should be used twice,
    // to poll for canvas and annotation button
    expect(widget.$interval.called).to.be.false;
    expect(widget.handleCanvasViewPrintMode.calledOnce).to.be.true;
    expect(widget.handleCanvasViewPrintMode.getCall(0).args[1]).to.equal('test-slot-id');
  });

  it('should create the config', () => {
    expect(widget.createConfig('manifestId').data[0].manifestUri).to.equal('manifestId');
  });

  it('should set saved to true in data-value', () => {
    widget.control.getDataValue = () => {
      return undefined;
    };
    widget.control.setDataValue = sinon.spy();
    widget.setSaved(true);
    expect(widget.control.setDataValue.args[0][0].saved).to.equal(true);
  });

  it('should destroy miradorViewer when external event comes', () => {
    let control = new EventEmitter();
    control.getId = () => {
    };
    let viewer = createMiradorViewer(undefined, undefined, undefined, control);
    viewer.events = [{unsubscribe: sinon.spy()}];
    viewer.eventsAdapter = null;

    control.publish('widgetExpanded', false);
    expect(viewer.events[0].unsubscribe.called).to.be.true;
    expect(viewer.miradorInstance).to.be.null;
  });

  it('should not destroy miradorViewer when widget is expanded', () => {
    let control = new EventEmitter();
    control.getId = () => {
    };
    let viewer = createMiradorViewer(undefined, undefined, undefined, control);
    viewer.destroy = sinon.spy();

    control.publish('widgetExpanded', true);
    expect(viewer.destroy.called).to.be.false;
  });

  it('should test the unsubscribe of the events', () => {
    let eventbus = IdocMocks.mockEventBus();
    sinon.stub(eventbus, 'subscribe', () => {
      return {
        unsubscribe: sinon.spy()
      };
    });
    createImageWidget(eventbus);
    let miradorViewerDestroy = sinon.spy();
    widget.miradorViewer = {
      destroy: miradorViewerDestroy
    };
    widget.control = {
      getBaseWidget: () => {
        return {
          ngOnDestroy: sinon.stub()
        };
      }
    };
    widget.ngOnDestroy();
    expect(miradorViewerDestroy.called).to.be.true;
    for (let event of widget.events) {
      expect(event.unsubscribe.callCount).to.equal(1);
    }
    expect(widget.miradorViewer).to.be.null;
  });

  it('should save the mirador config when there is mirador instance', () => {
    let miradorViewer = {
      getCurrentConfig: sinon.spy(() => {
        return {'data': 'data', windowObjects: []};
      }),
      miradorInstance: sinon.spy()
    };
    widget.control.getDataValue = () => {
      return {saved: false};
    };
    widget.control.setDataValue = sinon.spy();
    widget.miradorViewer = miradorViewer;
    widget.saveMiradorConfig();
    expect(widget.miradorViewer.getCurrentConfig.callCount).to.equal(1);
    expect(widget.config.miradorCurrentConfig.data).to.equal('data');
    expect(widget.control.saveConfig.callCount).to.equal(1);
    expect(widget.control.setDataValue.callCount).to.equal(1);
  });

  it('should not save the mirador config when there is no mirador instance', () => {
    widget.control.setDataValue = sinon.spy();
    widget.control.saveConfig = sinon.spy();
    widget.miradorViewer = {};
    widget.saveMiradorConfig();
    expect(widget.control.saveConfig.callCount).to.equal(0);
    expect(widget.control.setDataValue.callCount).to.equal(0);
  });

  it('should set the errorMessage if object selector cannot resolve selected objects', function (done) {
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.reject('error-msg');
    };
    var config = {};
    var context = {};
    var searchArguments = {};
    widget.loadObjects(config, context, searchArguments).catch((errorMessage) => {
      expect(errorMessage).to.equal('error-msg');
      done();
    }).then(done);
  });

  it('should set the errorMessage if there are no selected objects', function (done) {
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve({results: []});
    };
    var config = {};
    var context = {};
    var searchArguments = {};
    widget.loadObjects(config, context, searchArguments).catch((errorMessage) => {
      expect(errorMessage).to.equal(NO_IMAGES_SELECTED);
      done();
    });
  });

  it('should fire widgetReadyEvent if no object is selected', function (done) {
    let spyEventbusPublish = sinon.spy(widget.eventbus, 'publish');
    widget.context = {
      isModeling: () => {
        return false;
      }
    };
    widget.loadMirador().then((obtainedId) => {
      expect(spyEventbusPublish.calledOnce).to.be.true;
      expect(spyEventbusPublish.getCall(0).args[0]).to.eql(new WidgetReadyEvent({
        widgetId: 'test_id'
      }));
      done();
    }).catch(done);
  });

  it('should show empty widget in modeling view', () => {
    widget.context = {
      isModeling: () => {
        return true;
      }
    };
    widget.config.selectObjectMode = "automatically";
    widget.element.find = () => ({
      contents: () => ({
        find: () => ({
          hide: () => {
          }
        })
      }),
      hide: () => {
      }
    });
    widget.loadMirador();
    expect(widget.errorMessage).to.equal(undefined);
  });

  it('should not set the errorMessage if there are selected objects', function (done) {
    var selectedObjects = {results: ['emf:123']};
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve(selectedObjects);
    };
    var config = {selectObjectMode: SELECT_OBJECT_AUTOMATICALLY};
    var context = {};
    var searchArguments = {};
    widget.loadObjects(config, context, searchArguments).then(function (selected) {
      expect(widget.errorMessage).to.be.undefined;
      expect(selected).to.deep.equal(selectedObjects.results);
      done();
    }).catch(done);
  });

  it('should remove deleted objects from the widget configuration', (done) => {
    var selectedObjects = {results: ['emf:123']};
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve(selectedObjects);
    };
    widget.context = {
      getSharedObjects: () => {
        return Promise.resolve({
          notFound: ['emf:456'],
          data: [new InstanceObject('emf:123')]
        });
      }
    };
    widget.objectSelectorHelper.removeSelectedObjects = sinon.spy();
    var config = {selectObjectMode: SELECT_OBJECT_MANUALLY};
    var context = {};
    var searchArguments = {};
    widget.loadObjects(config, context, searchArguments).then((selected) => {
      expect(widget.objectSelectorHelper.removeSelectedObjects.called).to.be.true;
      expect(widget.objectSelectorHelper.removeSelectedObjects.getCall(0).args[1]).to.deep.equal(['emf:456']);

      expect(widget.control.saveConfig.called).to.be.true;
      expect(selected).to.deep.equal(selectedObjects.results);
      done();
    }).catch(done);
  });

  it('should set the errorMessage if no selected objects remain after removing deleted objects', function (done) {
    var selectedObjects = {results: ['emf:123']};
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve(selectedObjects);
    };
    widget.context = {
      getSharedObjects: () => {
        return Promise.resolve({
          notFound: [],
          data: []
        });
      }
    };
    var config = {selectObjectMode: SELECT_OBJECT_MANUALLY};
    var context = {};
    var searchArguments = {};
    widget.loadObjects(config, context, searchArguments).catch((errorMessage) => {
      expect(errorMessage).to.equal(NO_IMAGES_SELECTED);
      done();
    });
  });

  it('should provide search arguments when loading objects', (done) => {
    var selectedObjects = {results: [{id: 'emf:123'}]};
    widget.objectSelectorHelper.getSelectedObjects = sinon.spy(() => {
      return Promise.resolve(selectedObjects);
    });
    var config = {};
    var searchArg = {
      pageSize: 123
    };
    widget.loadObjects(config, undefined, searchArg).then(() => {
      expect(widget.objectSelectorHelper.getSelectedObjects.called).to.be.true;
      expect(widget.objectSelectorHelper.getSelectedObjects.getCall(0).args[2]).deep.equal(searchArg);
      done();
    }).catch(done);
  });

  it('should test the proper process of the manifest', function () {
    widget.miradorViewer = {
      getManifestBlobUri: sinon.spy()
    };
    let generatedConfig = widget.processManifest(1);
    expect(widget.miradorViewer.getManifestBlobUri.callCount).to.equal(1);
    expect(generatedConfig.windowObjects[0].viewType).to.equal('ImageView');
  });

  it('should create the manifest', function (done) {
    widget.imageService = {};
    widget.imageService.createManifest = function () {
      return new Promise((resolve, reject) => {
        resolve({data: 'manifestId'});
      });
    };
    widget.imageService.updateManifest = function () {
      return new Promise((resolve, reject) => {
        resolve({data: 'manifestId2'});
      });
    };
    widget.control = {
      getId: function () {
        return 'widgetId';
      }
    };
    widget.getManifestId(1).then((manifestId) => {
      expect(manifestId).to.equal('manifestId');
      done();
    }).catch(done);
  });

  it('should update the manifest', function (done) {
    widget.imageService = {};
    widget.imageService.updateManifest = function () {
      return new Promise((resolve, reject) => {
        resolve({data: 'manifestId2'});
      });
    };
    widget.control = {
      getId: function () {
        return 'widgetId';
      }
    };
    let config = {
      manifestId: 'manifestId'
    };
    widget.config = config;
    widget.getManifestId(1).then((manifestId) => {
      expect(manifestId).to.equal('manifestId2');
      done();
    }).catch(done);
  });

  it('should get the manifest by its id', function (done) {
    widget.imageService = {};
    widget.imageService.getManifest = function () {
      return new Promise((resolve, reject) => {
        resolve({data: 'manifestId'});
      });
    };

    widget.getManifestById('manifestId').then((obtainedId) => {
      expect(obtainedId).to.equal('manifestId');
      done();
    }).catch(done);
  });

  it('should get the data stored inside the temp recover attribute', function () {
    let miradorViwer = {
      setManifest: sinon.spy(),
      setupConfig: sinon.spy(),
      getMiradorSaveController: function () {
      },
      reload: function () {
      }
    };
    widget.miradorViewer = miradorViwer;
    widget.recoverMirador();
    let recoveredData = control.getDataFromAttribute();

    expect(widget.miradorViewer.setManifest.calledWith(recoveredData.manifest)).to.be.true;
    expect(widget.miradorViewer.setupConfig.calledWith(recoveredData.config)).to.be.true;
  });

  describe('handleCanvasViewPrintMode', () => {
    it('should resolve after resize', (done) => {
      widget.promiseAdapter = PromiseAdapterMock.mockAdapter();
      let callback;
      widget.resizeDetectorAdapter.addResizeListener = (element, resizeListenerCallback) => {
        callback = resizeListenerCallback;
      };
      widget.removeResizeListener = sinon.stub();
      widget.$timeout = () => {
      };
      widget.$timeout.cancel = sinon.spy();

      let miradorIframe = functionsMock;
      widget.handleCanvasViewPrintMode(miradorIframe, '').then(() => {
        expect(widget.$timeout.cancel.callCount).to.equal(1);
        done();
      });
      // simulate resize
      callback();
    });

    it('should resolve after a timeout if resize is not called', (done) => {
      widget.promiseAdapter = PromiseAdapterMock.mockAdapter();
      let removeResizeListener = sinon.spy();
      widget.resizeDetectorAdapter.addResizeListener = () => {
        return removeResizeListener;
      };
      widget.$timeout = (callback) => {
        // Resolve immediately
        callback();
      };
      let miradorIframe = functionsMock;
      widget.handleCanvasViewPrintMode(miradorIframe, '').then(() => {
        expect(removeResizeListener.callCount).to.equal(1);
        done();
      });
    });
  });

  describe('handleImageViewPrintMode', () => {
    let miradorIframe = functionsMock;
    sinon.spy(functionsMock, 'finish');

    beforeEach(() => {
      widget.promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
      widget.$timeout = (callback) => {
        // Resolve immediately
        callback();
      };
      functionsMock.length = 0;
      functionsMock.finish.reset();
    });

    it('should resolve after images are loaded', () => {
      functionsMock.length = 1;
      widget.handleImageViewPrintMode(miradorIframe, '',).then(() => {
        expect(functionsMock.finish.called).to.be.true;
      });
    });

    it('should resolve if there are no images to load', () => {
      widget.handleImageViewPrintMode(miradorIframe, '', '').then(() => {
        expect(functionsMock.finish.called).to.be.false;
      });
    });
  });

  describe('MiradorViewer', function () {

    it('should create manifestBlob when new manifest is set', function () {
      let viewer = createMiradorViewer();
      viewer.createManifestBlob = sinon.spy();
      viewer.setManifest('test');
      expect(viewer.createManifestBlob.called).to.be.true;
    });

    it('should return config if there there are no window objects', () => {
      let viewer = createMiradorViewer();
      let config = {
        data: [{
          manifestUri: 'mock'
        }]
      };
      expect(viewer.fixCanvasId(config)).to.eql(config);
    });

    it('should update the passed config with the new manifest blob uri', function () {
      let viewer = createMiradorViewer();
      viewer.manifestBlobUri = 'test';
      let config = {
        data: [{
          manifestUri: 'mock'
        }],
        windowObjects: [{
          loadedManifest: 'test'
        }]
      };
      viewer.fixConfig(config);
      expect(config.data[0].manifestUri).to.equal('test');
      expect(config.windowObjects[0].loadedManifest).to.equal('test');
    });

    it('should update the passed config if setupConfig is called with parameter withRefactor set to true', function () {
      let viewer = createMiradorViewer();
      viewer.fixConfig = sinon.spy();
      viewer.setupConfig({}, true);
      expect(viewer.fixConfig.called).to.be.true;
    });

    it('should update stale objects inside the config', function () {
      let viewer = createMiradorViewer();

      let testConfig = {
        windowObjects: [
          {
            canvasID: '2',
            viewType: 'ThumbnailsView',
            windowOptions: {
              osdBounds: {
                height: 1
              }
            }
          }
        ]
      };

      viewer.manifest = {
        sequences: [{
          canvases: [{
            '@id': '1'
          }]
        }]
      };
      viewer.getManifestBlobUri = sinon.stub().returns({});

      testConfig = viewer.fixConfig(testConfig);

      expect(testConfig.windowObjects[0].viewType).to.equal('ThumbnailsView');
      expect(testConfig.windowObjects[0].windowOptions.osdBounds).to.deep.equal({height: 1});
      expect(testConfig.windowObjects[0].loadedManifest).to.be.not.undefined;
      expect(viewer.getManifestBlobUri.called).to.be.true;
    });

    it('should not update window object if canvas id is inside the manifest', function () {
      let viewer = createMiradorViewer();

      let testConfig = {
        windowObjects: [
          {
            canvasID: '1',
            viewType: 'ThumbnailsView'
          }
        ]
      };

      viewer.manifest = {
        sequences: [{
          canvases: [{
            '@id': '1'
          }]
        }]
      };
      viewer.getManifestBlobUri = sinon.spy();

      testConfig = viewer.fixConfig(testConfig);

      expect(testConfig.windowObjects[0].viewType).to.equal('ThumbnailsView');
      expect(viewer.getManifestBlobUri.called).to.be.false;
    });

    it('should use static config for locked preview mode', function () {
      let viewer = createMiradorViewer();
      viewer.mergeInCurrentConfig = sinon.spy();
      viewer.setupConfig({}, false);
      expect(viewer.mergeInCurrentConfig.getCall(0).args[0]).to.deep.equal(miradorStaticConfigZenMode);
    });

    it('should use normal static config when widget is not locked', function () {
      let viewer = createMiradorViewer(false, false);
      viewer.mergeInCurrentConfig = sinon.spy();
      viewer.setupConfig({}, false);
      expect(viewer.mergeInCurrentConfig.getCall(0).args[0]).to.deep.equal(miradorStaticConfig);
    });

    it('should add annotation endpoint config', function () {
      let viewer = createMiradorViewer(false, true);
      viewer.mergeInCurrentConfig = sinon.spy();
      viewer.setupConfig({}, false);
      expect(viewer.mergeInCurrentConfig.getCall(1).args[0]).to.deep.equal(miradorEndpointConfig);
    });

    it('should remove physicalRuler from config in print mode', function () {
      let locationAdapter = IdocMocks.mockLocationAdapter('/#/idoc/emf:123456?mode=print');
      let viewer = createMiradorViewer(false, true, locationAdapter);
      viewer.setupConfig({'windowSettings': {}});
      expect(viewer.currentConfig.windowSettings.physicalRuler).to.be.false;
    });

    it('should use normal static config when widget is not locked and is not in preview mode', function () {
      let viewer = createMiradorViewer(false, false);
      viewer.mergeInCurrentConfig = sinon.spy();
      viewer.setupConfig({}, false);
      expect(viewer.mergeInCurrentConfig.getCall(0).args[0]).to.deep.equal(miradorStaticConfig);
    });

    it('should handle comments filtered event', function () {
      createImageWidget();
      widget.filterConfig.filters = {
        author: 'author'
      };
      widget.handleCommentsFilteredEvent(['test_id']);
      expect(widget.config.filters.author).to.equal('author');
    });

  });

  function createImageWidget(eventbus) {
    let $timeout = (callback) => {
      callback();
    };
    let evtBus = eventbus || new Eventbus();
    let $compile = {};
    let imageService = {};
    let localStorageService = {};
    let commentRestService = {};
    let commentsFilterService = {};
    let locationAdapter = IdocMocks.mockLocationAdapter('/#/idoc/emf:123456?mode=print');
    widget = new ImageWidget(mock$scope(), element, $compile, imageService, objectSelectorHelper, evtBus, localStorageService,
      PromiseStub, $timeout, IdocMocks.mockInterval(), commentRestService, commentsFilterService, locationAdapter, ResizeDetectorAdapterMock.mockAdapter());
  }

  function createMiradorViewer(isLocked, isPreviewMode, locationAdapter, control) {
    if (!control) {
      control = {
        getId: () => {
        },
        element: functionsMock,
        subscribe: () => {
          return {
            unsubscribe: () => {

            }
          };
        }
      };
    }

    return new MiradorViewer({
      viewerFrame: {
        addEventListener: function () {
        }
      },
      control: control,
      context: {
        isPreviewMode: () => {
          return isPreviewMode !== undefined ? isPreviewMode : true
        }
      },
      config: {
        lockWidget: isLocked !== undefined ? isLocked : true
      },
      locationAdapter: locationAdapter || {
        url: () => {
        }
      }
    });
  }

  var LAYOUT_SLOT =
    `<div class="layout-slot" data-layout-slot-id="test-slot-id"
       style="left: 3px; top: 3px; width: 750px; height: 417px;">
    <div id="" class="slot">
      <div class="slotIconContainer">
        <h1 class="plus" role="presentation" aria-label="Add item using Link"><span>+</span>
          <div class="dropIcon"><i class="fa fa-level-down"></i></div>
        </h1>
        <h1 class="addItemText">Add Item</h1>
        <h1 class="dropMeMessage">Drop to Load Manifest</h1></div>
      <a class="addItemLink" role="button" aria-label="Add item"></a><a class="remove-slot-option" style="display: none;"><i
      class="fa fa-times fa-lg fa-fw"></i> Close</a><a class="dropMask"></a>
      <div class="window">
        <div class="manifest-info">
          <div class="window-manifest-navigation"><a href="javascript:;" class="mirador-btn mirador-icon-view-type"
                                                     role="button" title="Change view type" aria-label="Change view type"><i
            class="fa fa-photo fa-lg fa-fw"></i><i class="fa fa-caret-down"></i>
            <ul class="dropdown image-list">
              <li class="single-image-option"><i class="fa fa-photo fa-lg fa-fw"></i> Image View</li>
              <li class="book-option"><i class="fa fa-columns fa-lg fa-fw"></i> Book View</li>
              <li class="scroll-option"><i class="fa fa-ellipsis-h fa-lg fa-fw"></i> Scroll View</li>
              <li class="thumbnails-option"><i class="fa fa-th fa-lg fa-rotate-90 fa-fw"></i> Gallery View</li>
            </ul>
          </a><a href="javascript:;" class="mirador-btn mirador-icon-metadata-view mirador-tooltip" role="button"
                 aria-label="View information/metadata about this object" data-hasqtip="0"
                 oldtitle="View information/metadata about this object" title=""><i
            class="fa fa-info-circle fa-lg fa-fw"></i></a><a class="mirador-btn mirador-osd-fullscreen mirador-tooltip"
                                                             role="button" aria-label="Toggle this window to full screen"
                                                             data-hasqtip="1" oldtitle="Toggle this window to full screen"
                                                             title=""><i class="fa fa-lg fa-fw fa-expand"></i></a></div>
          <a href="javascript:;" class="mirador-btn mirador-close-window remove-object-option mirador-tooltip"
             aria-label="Close this window" data-hasqtip="2" oldtitle="Close this window" title="" style="display: none;"><i
            class="fa fa-times fa-lg fa-fw"></i></a>
          <h3 class="window-manifest-title" title="" aria-label=""></h3></div>
        <div class="content-container">
          <div class="overlay">
            <div class="sub-title">Details:</div>
            <div class="metadata-listing">
              <div class="metadata-item">
                <div class="metadata-label">Label:</div>
                <div class="metadata-value"><b></b></div>
              </div>
            </div>
            <div class="sub-title">Rights:</div>
            <div class="metadata-listing">
              <div class="metadata-item">
                <div class="metadata-label">Rights Status:</div>
                <div class="metadata-value">Unspecified</div>
              </div>
            </div>
          </div>
          <div class="view-container focus-max-width" style="transition: 0s; margin-left: 0px;">
            <div class="bottomPanel minimized"></div>
            <div class="image-view focus-max-height" style="display: block;">
              <div class="annotation-canvas"></div>
              <div class="mirador-hud">
                <div class="mirador-osd-context-controls hud-container">
                  <div class="mirador-osd-annotation-controls"><a
                    class="mirador-osd-annotations-layer hud-control selected" role="button"
                    aria-label="Toggle annotations" data-hasqtip="4" oldtitle="Toggle annotations" title=""><i
                    class="fa fa-lg fa-comments"></i></a></div>
                </div>
                <div class="mirador-pan-zoom-controls hud-control"><a class="mirador-osd-up hud-control" role="button"
                                                                      aria-label="Move image up"><i
                  class="fa fa-chevron-circle-up"></i></a><a class="mirador-osd-right hud-control" role="button"
                                                             aria-label="Move image right"><i
                  class="fa fa-chevron-circle-right"></i></a><a class="mirador-osd-down hud-control" role="button"
                                                                aria-label="Move image down"><i
                  class="fa fa-chevron-circle-down"></i></a><a class="mirador-osd-left hud-control" role="button"
                                                               aria-label="Move image left"><i
                  class="fa fa-chevron-circle-left"></i></a><a class="mirador-osd-zoom-in hud-control" role="button"
                                                               aria-label="Zoom in"><i
                  class="fa fa-plus-circle"></i></a><a class="mirador-osd-zoom-out hud-control" role="button"
                                                       aria-label="Zoom out"><i class="fa fa-minus-circle"></i></a><a
                  class="mirador-osd-go-home hud-control" role="button" aria-label="Reset image bounds"><i
                  class="fa fa-home"></i></a></div>
              </div>
              <div class="mirador-osd" id="mirador-osd-8d156bb4-7b2d-4312-8345-a7a6b204e29b" data-hasqtip="6">
                <div class="openseadragon-container"
                     style="background: none transparent; border: none; margin: 0px; padding: 0px; position: relative; width: 100%; height: 100%; overflow: hidden; left: 0px; top: 0px; text-align: left;">
                  <div class="openseadragon-canvas" tabindex="0" dir="ltr"
                       style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; width: 100%; height: 100%; overflow: hidden; top: 0px; left: 0px; touch-action: none; text-align: left; cursor: default;">
                    <canvas width="748" height="381"
                            style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; width: 100%; height: 100%;"></canvas>
                    <div
                      style="background: none transparent; border: none; margin: 0px; padding: 0px; position: static;"></div>
                    <canvas id="draw_canvas_352d80ee-2b33-4a95-827e-ae6cfa6f6895" width="748" height="381"
                            keepalive="true"
                            style="transform: translate(0px, 0px); margin-left: 0px; margin-top: 0px; display: block; -webkit-user-drag: none; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0);"></canvas>
                  </div>
                  <div
                    style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; left: 0px; top: 0px;"></div>
                  <div
                    style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; right: 0px; top: 0px;"></div>
                  <div
                    style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; right: 0px; bottom: 0px;"></div>
                  <div
                    style="background: none transparent; border: none; margin: 0px; padding: 0px; position: absolute; left: 0px; bottom: 0px;"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>`
});