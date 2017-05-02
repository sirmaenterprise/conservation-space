import {ImageWidget, MiradorViewer, NO_IMAGES_SELECTED} from 'idoc/widget/image-widget/image-widget';
import miradorStaticConfigZenMode from 'idoc/widget/image-widget/mirador-integration/mirador-static-config-zen-mode.json!'
import miradorStaticConfig from 'idoc/widget/image-widget/mirador-integration/mirador-static-config.json!'
import miradorEndpointConfig from 'idoc/widget/image-widget/mirador-integration/annotation-endpoint-config.json!'
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {AnnotationListLoadedEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {InstanceObject} from 'idoc/idoc-context';
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
        find: function () {
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
    on: function (eventName, func) {
      func();
    },
    finish: function () {
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
  let config = {};

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

  it('should test the unsubscribe of the events', () => {
    let eventbus = IdocMocks.mockEventBus();
    sinon.stub(eventbus, 'subscribe', () => {
      return {
        unsubscribe: sinon.spy()
      };
    });
    createImageWidget(eventbus);
    widget.miradorViewer = {
      eventsAdapter: {
        destroy: sinon.spy()
      }
    };
    widget.ngOnDestroy();
    for (let event of widget.events) {
      expect(event.unsubscribe.callCount).to.equal(1);
    }
    expect(widget.miradorViewer.eventsAdapter.destroy.called).to.be.true;
  });

  it('should save the mirador config when there is mirador instance', () => {
    let miradorViewer = {
      getCurrentConfig: sinon.spy(() => {
        return {'data': 'data'};
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
    widget.loadObjects({}).catch((errorMessage) => {
      expect(errorMessage).to.equal('error-msg');
      done();
    }).then(done);
  });

  it('should set the errorMessage if there are no selected objects', function (done) {
    widget.objectSelectorHelper.getSelectedObjects = () => {
      return Promise.resolve({results: []});
    };
    widget.loadObjects({}).catch((errorMessage) => {
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
          hide: () => {}
        })
      }),
      hide: () => {}
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
    widget.loadObjects(config).then(function (selected) {
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
    widget.loadObjects(config).then((selected) => {
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
    widget.loadObjects(config).catch((errorMessage) => {
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

  describe('imageViewPrintMode', () => {
    it('should resolve after resize', (done) => {
      widget.promiseAdapter = PromiseAdapterMock.mockAdapter();
      let callback;
      widget.resizeDetectorAdapter.addResizeListener = (element, resizeListenerCallback) => {
        callback = resizeListenerCallback;
      };
      widget.removeResizeListener = sinon.stub();
      widget.$timeout = IdocMocks.mockTimeout();
      let timeoutCancelSpy = sinon.spy(widget.$timeout, 'cancel');
      let miradorIframe = functionsMock;
      widget.imageViewPrintMode(miradorIframe, '').then(() => {
        expect(timeoutCancelSpy.callCount).to.equal(1);
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
      widget.imageViewPrintMode(miradorIframe, '').then(() => {
        expect(removeResizeListener.callCount).to.equal(1);
        done();
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
    let $interval = {};
    let commentRestService = {};
    let commentsFilterService = {};
    let locationAdapter = IdocMocks.mockLocationAdapter('/#/idoc/emf:123456?mode=print');
    widget = new ImageWidget(mock$scope(), element, $compile, imageService, objectSelectorHelper, evtBus, localStorageService,
      PromiseStub, $timeout, $interval, commentRestService, commentsFilterService, locationAdapter, ResizeDetectorAdapterMock.mockAdapter());
  }

  function createMiradorViewer(isLocked, isPreviewMode, locationAdapter) {

    return new MiradorViewer({
      viewerFrame: {
        addEventListener: function () {
        }
      },
      control: {
        getId: () => {
        }
      },
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
});