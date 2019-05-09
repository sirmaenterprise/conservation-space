import {Widget} from 'idoc/widget/widget';
import Paths from 'common/paths';
import {UrlUtils} from 'common/url-utils';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {View, Inject, NgScope, NgElement, NgCompile, NgTimeout, NgInterval} from 'app/app';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {ImageService} from 'services/rest/image-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {BeforeIdocContentModelUpdateEvent} from 'idoc/events/before-idoc-content-model-update-event';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {MiradorEventsAdapter} from 'idoc/widget/image-widget/mirador-integration/mirador-events-adapter';
import {MiradorLoadedEvent, MiradorSaveControllerUpdated, UpdateMiradorAnnotationListEvent, AnnotationListLoadedEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {MODE_PRINT} from 'idoc/idoc-constants';
import 'idoc/widget/image-widget/image-comments/image-comments';
import {AnnotationEndpoint} from 'idoc/widget/image-widget/mirador-integration/annotation-endpoint';
import {CommentsRestService} from 'services/rest/comments-service';
import {CommentsHelper} from 'idoc/comments/comments-helper';
import {CommentsFilterService} from 'idoc/comments/comments-filter/comments-filter-service';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import {CommentsFilteredEvent} from 'idoc/comments/comments-filter/comments-filtered-event';
import _ from 'lodash';
import {ResizeDetectorAdapter} from 'adapters/resize-detector-adapter';
import miradorStaticConfig from './mirador-integration/mirador-static-config.json!';
import miradorStaticConfigZenMode from './mirador-integration/mirador-static-config-zen-mode.json!';
import miradorAnnotationEndpointConfig from './mirador-integration/annotation-endpoint-config.json!';
import imageWidgetTemplate from './image-widget.html!text';
import './image-widget.css!css';

export const NO_IMAGES_SELECTED = 'imagewidget.no.images.selected';
const CANNOT_LOAD_IMAGES = 'imagewidget.cannot.load.images';

@Widget
@View({
  template: imageWidgetTemplate
})
@Inject(NgScope, NgElement, NgCompile, ImageService, ObjectSelectorHelper, Eventbus, LocalStorageService, PromiseAdapter, NgTimeout,
  NgInterval, CommentsRestService, CommentsFilterService, LocationAdapter, ResizeDetectorAdapter)
export class ImageWidget {

  constructor($scope, element, $compile, imageService, objectSelectorHelper, eventbus, localStorageService, promiseAdapter, $timeout,
              $interval, commentRestService, commentsFilterService, locationAdapter, resizeDetectorAdapter) {
    this.locationAdapter = locationAdapter;
    this.commentRestService = commentRestService;
    this.objectSelectorHelper = objectSelectorHelper;
    this.imageService = imageService;
    this.commentsFilterService = commentsFilterService;
    this.element = element;
    this.eventbus = eventbus;
    this.$compile = $compile;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$interval = $interval;
    this.resizeDetectorAdapter = resizeDetectorAdapter;
    this.filterConfig = {
      filters: this.config.filters || _.merge({}, EMPTY_FILTERS),
      comments: () => {
        return this.commentRestService.loadAllComments(this.miradorViewer.eventsAdapter.getCurrentImageId()).then((response) => {
          if (response.data && response.data.length) {
            return this.allComments = CommentsHelper.convertToCommentInstances(response.data);
          }
          else {
            this.allComments = [];
          }
        });
      },
      widgetId: this.control.getId()
    };

    this.events = [
      this.eventbus.subscribe(BeforeIdocContentModelUpdateEvent, this.saveMiradorConfig.bind(this)),
      this.eventbus.subscribe(MiradorLoadedEvent, (event) => {
        if (event[0] === this.control.getId()) {
          this.numberOfSlots = this.miradorViewer.getCurrentConfig().windowObjects.length;
        }
        this.appendImageComments(event);
      }),
      this.eventbus.subscribe(AnnotationListLoadedEvent, this.annotationListLoadedHandler.bind(this)),
      this.eventbus.subscribe(MiradorSaveControllerUpdated, (event) => {
        if (event[0] === this.control.getId()) {
          this.control.storeDataInAttribute(event[1], ImageWidget.TEMP_SAVE_ATTRIBUTE);
        }
      }),

      this.eventbus.subscribe(CommentsFilteredEvent, this.handleCommentsFilteredEvent.bind(this))
    ];
    this.storage = localStorageService;
    this.config = this.config || {};
    this.control.onConfigConfirmed = () => {
      delete this.secondQueryAutoSelect;
      this.control.storeDataInAttribute({}, ImageWidget.TEMP_SAVE_ATTRIBUTE);
    };
    this.promiseAdapter = promiseAdapter;
    this.searchArguments = {
      pageSize: 250
    };

    this.numberOfLoadedSlots = 0;

    this.watcherFn = this.$scope.$watch(() => {
      return this.config.hideAnnotationLayer;
    }, (newVal, oldVal) => {
      if (newVal !== oldVal) {
        this.appendImageComments([this.control.getId()], true);
      }
    });
  }

  annotationListLoadedHandler() {
    if (this.numberOfSlots) {
      // initialize first time when AnnotationListLoadedEvent is fired
      this.numberOfLoadedSlots++;
      // wait until AnnotationListLoadedEvent is fired for all slots
      if (this.numberOfLoadedSlots === this.numberOfSlots) {
        // wait initially some time because its not reliable depend only from the mirador events even from the
        // AnnotationsRenderedEvent
        this.$timeout(() => {
          let isPrintMode = UrlUtils.getParameter(this.locationAdapter.url(), 'mode') === MODE_PRINT;
          if (isPrintMode) {
            this.waitForSlotsToBeAddedToDOM().then(() => {
              this.handlePrintMode();
            });
          } else {
            this.publishWidgetReadyEvent(this.control.getId());
          }
        }, 3000);
      }
    }
  }

  /**
   * Ensure that all layout slots are added to DOM before proceeding with the print
   * @returns {*} promise which is resolved when all layout slots are in DOM
   */
  waitForSlotsToBeAddedToDOM() {
    let iFrameContents = this.element.find('.image-widget-viewer iframe').contents();
    return this.promiseAdapter.promise((resolve) => {
      this.numberOfSlotsAddedToDOMPoller = this.$interval(() => {
        if (iFrameContents.find('.layout-slot').length === this.numberOfSlots) {
          this.$interval.cancel(this.numberOfSlotsAddedToDOMPoller);
          resolve();
        }
      }, 100);
    });
  }

  /**
   * Creates layout to position content of different slots of Mirador viewer.
   * Mirador uses absolute positioning
   * @param parentElement to be divided into rows and columns
   * @param layoutSlot as described in Mirador's config
   * @param numColumns to be nested into parent slot (i.e. number of column siblings) used to calculated columns widths
   */
  buildPrintLayout(parentElement, layoutSlot, numColumns) {
    let cssClass = layoutSlot.type === 'row' ? 'row' : 'col-xs-1';
    let layoutElement = $(`<div class="image-widget-layout-${layoutSlot.type} ${cssClass}" data-layout-slot-id="${layoutSlot.id}"></div>`);

    let width = 100;
    if (layoutSlot.type === 'column') {
      width = 100 / numColumns;
      // create evenly spaced columns
      layoutElement.css({
        width: width + '%'
      });
    }

    // only for leafs
    if (!layoutSlot.children) {
      let aspectRatioPercent = width / layoutSlot.dx * layoutSlot.dy;
      // keep aspect ratio
      layoutElement.css({
        position: 'relative',
        'padding-bottom': aspectRatioPercent + '%'
      });
    }

    parentElement.append(layoutElement);
    if (layoutSlot.children) {
      layoutSlot.children.forEach((child) => {
        this.buildPrintLayout(layoutElement, child, layoutSlot.children.length);
      });
    }
  }

  handlePrintMode() {
    let imageWidgetViewer = this.control.element.find('.image-widget-viewer').eq(0);

    let layoutConfig = JSON.parse(this.miradorViewer.getCurrentConfig().layout);
    // column without a row as top parent? Probably a bug in Mirador
    if (layoutConfig.type === 'column') {
      layoutConfig.type = 'row';
    }
    this.buildPrintLayout(imageWidgetViewer, layoutConfig, 1);

    let miradorIframe = imageWidgetViewer.find('iframe');
    miradorIframe.hide();
    // in print mode the annotations toolbar must be manually opened because sometimes it is not opened by default
    let slotsReadyToPrintPromises = $.map(miradorIframe.contents().find('.layout-slot'), (slotEl) => {
      let slot = $(slotEl);
      let slotId = slot.data('layout-slot-id');
      // first child is always the bottom panel. Second is the real view.
      let viewContainer = slot.find('.view-container').children().eq(1);

      let viewContainerClass = viewContainer.attr('class').split(' ').find(clazz => clazz.indexOf('-view') > -1);
      switch (viewContainerClass) {
      case 'image-view':
        return this.promiseAdapter.promise((resolve) => {
          if (this.config.lockWidget) {
            var annToolbarBtnPoller = this.$interval(() => {
              let annotationsButton = slot.find('.mirador-osd-annotations-layer');
              if (annotationsButton.length > 0) {
                this.$interval.cancel(annToolbarBtnPoller);
                if (!annotationsButton.is('.selected')) {
                  annotationsButton.find('i').click();
                }
                // after this the annotations canvas layer have to be pooled until it gets displayed
                var drawingCanvasPoller = this.$interval(() => {
                  let drawingCanvas = slot.find('[id^="draw_canvas"]');
                  if (drawingCanvas.length > 0 && drawingCanvas.css('display') !== 'none') {
                    this.$interval.cancel(drawingCanvasPoller);
                    this.handleCanvasViewPrintMode(miradorIframe, slotId).then(() => {
                      resolve();
                    }).catch((error) => {
                      this.logger.error(error);
                      resolve();
                    });
                  }
                }, 100);
              }
            }, 100);
          } else {
            this.handleCanvasViewPrintMode(miradorIframe, slotId).then(() => {
              resolve();
            }).catch((error) => {
              this.logger.error(error);
              resolve();
            });
          }
        });
      case 'book-view':
        return this.promiseAdapter.promise((resolve) => {
          this.handleCanvasViewPrintMode(miradorIframe, slotId).then(() => {
            resolve();
          })
            .catch((error) => {
              this.logger.error(error);
              resolve();
            });
        });
      case 'scroll-view':
      case 'thumbnail-view':
        return this.promiseAdapter.promise((resolve) => {
          this.handleImageViewPrintMode(miradorIframe, slotId, viewContainerClass).then(() => {
            resolve();
          }).catch((error) => {
            this.logger.error(error);
            resolve();
          });
        });
      default:
        return this.promiseAdapter.promise((resolve) => {
          this.logger.error(`Not recognised view found: ${viewContainerClass}. Container class list: ${viewContainer.attr('class').split(' ')}`);
          resolve();
        });
      }
    });

    this.promiseAdapter.all(slotsReadyToPrintPromises).then(() => {
      this.publishWidgetReadyEvent(this.control.getId());
    });
  }

  /**
   * Handles Mirador views which rely on <img> tags for their view.
   * Images are extracted, waited to load and appended to the print container.
   *
   * @param miradorIframe
   * @param slotId
   * @param view type of view. Used to specify proper selector.
   * @returns {*}
   */
  handleImageViewPrintMode(miradorIframe, slotId, view) {
    return this.promiseAdapter.promise((resolve) => {
      let innerJQuery = miradorIframe.eq(0)[0].contentWindow.$ || $;
      let container = innerJQuery(miradorIframe.eq(0)[0].contentDocument).find(`div[data-layout-slot-id='${slotId}'] .${view}`).detach();
      let images = container.find('img');
      let imagesCount = images.length;
      if (imagesCount === 0) {
        resolve();
      } else {
        images.on('load', function () {
          let img = innerJQuery(this);
          // Finish all animations added by Mirador
          img.finish();
          imagesCount--;
          if (imagesCount === 0) {
            resolve();
          }
        });
        // load all lazy images
        images.each((i, img) => {
          let image = $(img);
          image.attr('src', image.attr('data'));
        });
      }
      // no need to keep aspect ratio for gallery (thumbnail) view
      let layoutSlot = this.control.element.find(`.image-widget-viewer div[data-layout-slot-id='${slotId}']`);
      layoutSlot.css({'padding-bottom': 0});
      layoutSlot.append(container);
    });
  }

  /**
   * Handles Mirador view which rely on <canvas> tags for their view.
   * Canvases are extracted, and appended to the print container. Resize is waited upon so that it
   *  has properly fitted its widget container.
   * @param miradorIframe
   * @param slotId
   * @returns {*}
   */
  handleCanvasViewPrintMode(miradorIframe, slotId) {
    return this.promiseAdapter.promise((resolve) => {
      let container = miradorIframe.contents().find(`div[data-layout-slot-id='${slotId}'] .openseadragon-container`);
      container.css({'position': 'absolute'});
      let layoutSlot = this.control.element.find(`.image-widget-viewer div[data-layout-slot-id='${slotId}']`);
      // wait for container to get resized otherwise it can be printed with wrong dimensions
      let resizeTimeout, resizeListener;
      let resizeCallback = () => {
        this.$timeout.cancel(resizeTimeout);
        resolve();
      };
      resizeListener = this.resizeDetectorAdapter.addResizeListener(container[0], resizeCallback);
      resizeTimeout = this.$timeout(() => {
        resizeListener();
        resolve();
      }, 500);
      layoutSlot.append(container);
    });
  }

  publishWidgetReadyEvent(widgetId) {
    this.eventbus.publish(new WidgetReadyEvent({widgetId}));
  }

  handleCommentsFilteredEvent(event) {
    if (event[0] === this.control.getId()) {
      this.eventbus.publish(new UpdateMiradorAnnotationListEvent(this.control.getId()));
      this.config.filters = this.filterConfig.filters;
    }
  }

  /**
   * Initializes image comments section view.
   *
   * @param event originator event, containing id of the widget.
   * @param shouldChangeVisibilityOnly optional param, used to change visibility state of comments section.
   */
  appendImageComments(event, shouldChangeVisibilityOnly) {
    if (event[0] === this.control.getId()) {
      let imageCommentsElement = this.element.find('.image-comments');
      if (this.config.hideAnnotationLayer) {
        imageCommentsElement.addClass('comments-hidden');
      } else {
        imageCommentsElement.removeClass('comments-hidden');
      }

      // if the widget has already been initialized
      // and only comment specific config has been changed,
      // there is no need to reinitialize the comment component.
      if (shouldChangeVisibilityOnly) {
        return;
      }

      if (imageCommentsElement.children().length) {
        return;
      }
      this.commentsConfig = {
        commentConfig: event[1],
        filterConfig: this.filterConfig
      };
      let imageCommentsComponent = $('<image-comments context="::imageWidget.context" control="::imageWidget.control" config="imageWidget.commentsConfig"></image-comments>');
      imageCommentsElement.append(imageCommentsComponent);
      this.$compile(imageCommentsComponent)(this.$scope);
    }
  }

  getMiradorPath() {
    return MiradorViewer.PATH;
  }

  loadObjects(config, context, searchAruments) {
    searchAruments.orderBy = this.config.orderBy;
    searchAruments.orderDirection = this.config.orderDirection;
    return this.objectSelectorHelper.getSelectedObjects(config, context, searchAruments).then((selectedObjectsResponse) => {
      var objects = selectedObjectsResponse.results;
      if (objects && objects.length > 0) {
        // Some of the manually selected objects may be deleted so we need to remove them
        if (config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
          return this.loadNonDeletedObjects(objects);
        }

        return this.promiseAdapter.resolve(objects);
      }
      return this.promiseAdapter.reject(NO_IMAGES_SELECTED);
    });
  }

  loadNonDeletedObjects(selectedObjects) {
    return this.context.getSharedObjects(selectedObjects, this.control.getId(), true).then((sharedObjects) => {
      // Remove all deleted instances from the widget configuration
      if (sharedObjects.notFound.length > 0) {
        this.objectSelectorHelper.removeSelectedObjects(this.config, sharedObjects.notFound);
        this.control.saveConfig(this.config);
      }

      if (sharedObjects.data.length < 1) {
        return this.promiseAdapter.reject(NO_IMAGES_SELECTED);
      }

      return sharedObjects.data.map((sharedObject) => {
        return sharedObject.getId();
      });
    });
  }

  loadMirador() {
    let selectedObjectsCount;
    if (this.widgetShouldBeEmpty()) {
      this.element.find('.image-widget-viewer iframe').contents().find('.mirador-viewer').hide();
      this.element.find('.image-comments').hide();
      delete this.errorMessage;
      return;
    }
    return this.loadObjects(this.config, this.context, this.searchArguments).then((selectedObjects) => {
      this.element.find('.image-widget-viewer iframe').contents().find('.mirador-viewer').show();
      if (!this.config.hideAnnotationLayer) {
        this.element.find('.image-comments').show();
      }
      delete(this.errorMessage);
      this.instantiateMiradorViewer();
      selectedObjectsCount = selectedObjects.length;
      return this.getManifestId(selectedObjects);
    })
      .then(this.getManifestById.bind(this))
      .then((manifest) => {
        this.miradorViewer.setManifest(manifest);
        if (this.isSaved()) {
          this.miradorViewer.setupConfig(this.config.miradorCurrentConfig, true);
          this.setSaved(false);
        } else {
          this.miradorViewer.setupConfig(this.processManifest(selectedObjectsCount));
        }
        this.reloadMiradorIframe();
      }).catch((error) => {
        // If no object is selected to be displayed mark widget as ready for print
        this.eventbus.publish(new WidgetReadyEvent({
          widgetId: this.control.getId()
        }));
        this.errorMessage = error.reason;
      });
  }

  setSaved(saved) {
    let dataValueConfig = this.control.getDataValue();
    if (dataValueConfig) {
      dataValueConfig.saved = saved;
    } else {
      dataValueConfig = {saved};
    }
    this.control.setDataValue(dataValueConfig);
  }

  isSaved() {
    let dataValueConfig = this.control.getDataValue();
    if (dataValueConfig) {
      return dataValueConfig.saved;
    } else {
      return false;
    }
  }

  /**
   * Gets the current config of the mirador and saves it to the config of the widget.
   * Removes the temp saved data in the attribute.
   */
  saveMiradorConfig() {
    // Don't save new config when the mirador has not loaded
    if (this.miradorViewer && !this.miradorViewer.miradorInstance) {
      return;
    }
    if (this.miradorViewer && this.miradorViewer.miradorInstance && !this.errorMessage) {
      this.config.miradorCurrentConfig = this.miradorViewer.getCurrentConfig();
      this.setSaved(true);
    } else {
      this.setSaved(false);
    }
    this.control.storeDataInAttribute({}, ImageWidget.TEMP_SAVE_ATTRIBUTE);
    this.control.skipChangeHandler = true;
    this.control.saveConfig(this.config);
  }

  /**
   * Creates the object with the image ids and manifest id, sends it to the server and sets the manifest id
   * which is returned by the server.
   *
   * @param selectedObjects the selected objects
   */
  getManifestId(selectedObjects) {
    let data = {
      imageWidgetId: this.control.getId(),
      selectedImageIds: selectedObjects
    };
    if (!this.config.manifestId) {
      return this.imageService.createManifest(data).then((response) => {
        return response.data;
      }).catch(() => {
        return Promise.reject(CANNOT_LOAD_IMAGES);
      });
    } else {
      data.manifestId = this.config.manifestId;
      return this.imageService.updateManifest(data).then((response) => {
        return response.data;
      }).catch(() => {
        return Promise.reject(CANNOT_LOAD_IMAGES);
      });
    }
  }

  getManifestById(manifestId) {
    return this.imageService.getManifest(manifestId).then((response) => {
      return response.data;
    });
  }

  /**
   * Mirador does not have functionality for updating its config in runtime, so any time it obtains
   * a manifest and the configuration is changed the current object has to be destroyed and a new one with the new
   * config has to be created. It is achieved by reloading the iframe.
   */
  reloadMiradorIframe() {
    this.miradorViewer.reload(false);
  }

  /**
   * Creates the initial mirador config
   * @param selectedObjectsCount It needs the count of the images in order to show them in library or image view mode.
   * @returns {{id, layout, data, saveSession, windowObjects, mainMenuSettings, availableAnnotationDrawingTools, annotationEndpoint}|*}
   */
  processManifest(selectedObjectsCount) {
    return this.createConfig(this.miradorViewer.getManifestBlobUri(), selectedObjectsCount);
  }

  instantiateMiradorViewer() {
    if (!this.miradorViewer) {
      this.viewerFrame = this.element.find('iframe')[0];
      this.miradorViewer = new MiradorViewer(this);
    }
  }

  createConfig(manifestBlobUri, selectedObjectsCount) {
    return {
      id: 'viewer',
      layout: '1x1',
      data: [{
        manifestUri: manifestBlobUri
      }],
      windowObjects: [{
        loadedManifest: manifestBlobUri,
        viewType: (selectedObjectsCount === 1) ? 'ImageView' : 'ThumbnailsView'
      }]
    };
  }

  loadOrRecoverMirador() {
    if (_.isEmpty(this.control.getDataFromAttribute(ImageWidget.TEMP_SAVE_ATTRIBUTE))) {
      this.loadMirador();
    } else {
      // The load event of iframe is not fired when the iframe content if fully loaded.
      // Not all browsers support https://developer.mozilla.org/en-US/docs/Web/Events/DOMFrameContentLoaded
      // Wait some time in order to have the iframe code evaluated
      setTimeout(() => {
        this.recoverMirador();
      }, 300);
    }
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
    if (this.miradorViewer) {
      this.miradorViewer.destroy();
      this.miradorViewer = null;
    }

    if (this.numberOfSlotsAddedToDOMPoller) {
      this.$interval.cancel(this.numberOfSlotsAddedToDOMPoller);
    }
    this.watcherFn();
  }

  ngAfterViewInit() {
    this.$scope.$watchCollection(() => {
      return [this.config.selectObjectMode, this.config.selectedObjects];
    }, () => {
      //The objects selector deletes the selected objects every time they are returned.
      //The watcher catches the change and invokes the method again which is unnecessary.
      //To prevent the second invocation I have set this check.
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY && !this.secondQueryAutoSelect) {
        this.loadOrRecoverMirador();
        this.secondQueryAutoSelect = true;
      } else if (this.config.selectObjectMode !== SELECT_OBJECT_AUTOMATICALLY) {
        this.loadOrRecoverMirador();
        delete this.secondQueryAutoSelect;
      }
    });
  }

  recoverMirador() {
    this.instantiateMiradorViewer();
    let tempStoredConfig = this.control.getDataFromAttribute(ImageWidget.TEMP_SAVE_ATTRIBUTE);
    this.miradorViewer.setManifest(tempStoredConfig.manifest);
    this.miradorViewer.setupConfig(tempStoredConfig.config, true);
    this.reloadMiradorIframe();
  }

  widgetShouldBeEmpty() {
    return this.context.isModeling() && (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY || this.config.selectObjectMode === SELECT_OBJECT_CURRENT);
  }

}

ImageWidget
  .TEMP_SAVE_ATTRIBUTE = 'temp-save';
export class MiradorViewer {

  constructor(imageWidget) {
    this.widget = imageWidget;
    this.commentRestService = imageWidget.commentRestService;
    this.manifest = {};
    this.viewerFrame = imageWidget.viewerFrame;
    this.eventbus = imageWidget.eventbus;
    this.widgetConfig = imageWidget.config;

    this.events = [imageWidget.control.subscribe('widgetExpanded', (expanded) => {
      if (!expanded) {
        this.destroy();
      }
    })];
    this.viewerFrame.addEventListener('load', () => {
      this.annotationEndpoint = new AnnotationEndpoint(this.commentRestService, this.widget);
      this.viewerFrame.contentWindow.annotationEndpoint = {
        endpoint: this.annotationEndpoint
      };
      this.subscribeToMiradorLoaded('miradorLoaded', this.handleMiradorLoad.bind(this));
      // the blob uri is lost if mirador has been hidden by the ToC
      this.currentConfig = this.fixLoadedManifest(this.currentConfig);

      if (this.currentConfig) {
        // Using lodash's deepClone cause frame's window to leak
        let clonedConfig = JSON.parse(JSON.stringify(this.currentConfig));

        this.viewerFrame.contentWindow.init({
          config: clonedConfig,
          annotationEndpoint: this.annotationEndpoint
        });
      }
    }, false);
    this.context = imageWidget.context;
    this.widgetId = imageWidget.control.getId();
  }

  subscribeToMiradorLoaded(eventName, callback) {
    // sometimes the load event is called before mirador script and there is not loaded jquery
    if (this.viewerFrame.contentWindow.$) {
      this.viewerFrame.contentWindow.$.subscribe(eventName, callback);
    }

  }

  subscribeTo(eventname, eventEmitter, callback) {
    eventEmitter.subscribe(eventname, callback);
  }

  handleMiradorLoad() {
    this.miradorInstance = this.viewerFrame.contentWindow.mirador;

    this.subscribeTo('saveControllerConfigUpdated', this.miradorInstance.eventEmitter, this.handleConfigChange.bind(this));
    this.eventsAdapter = new MiradorEventsAdapter(this.eventbus, this.miradorInstance.eventEmitter, this.widgetId);

    this.commentConfig = {
      eventsAdapter: this.eventsAdapter
    };

    this.eventbus.publish(new MiradorLoadedEvent(this.widgetId, this.commentConfig));
  }

  handleConfigChange() {
    if (this.context.isEditMode()) {
      this.setupConfig(this.miradorInstance.viewer.state.currentConfig, false);
      this.eventbus.publish(new MiradorSaveControllerUpdated(this.widgetId, {
        manifest: this.getManifest(),
        config: this.getCurrentConfig()
      }));
    }
  }

  /**
   * Sets the config with which the Mirador will be instantiated.
   * Also if withRefactor is setup it will replace all manifestUris in the config with the current blob uri
   * and merge new configs
   * @param config the config
   */
  setupConfig(config, withRefactor) {
    if (config.windowObjects && config.windowObjects.length > 1 && !config.windowObjects[0].id) {
      config.windowObjects.splice(0, 1);
    }
    this.currentConfig = config;

    if (this.context.isPreviewMode() && this.widgetConfig.lockWidget) {
      this.mergeInCurrentConfig(miradorStaticConfigZenMode);
    } else {
      this.mergeInCurrentConfig(miradorStaticConfig);
    }

    this.mergeInCurrentConfig(miradorAnnotationEndpointConfig);

    if (withRefactor) {
      this.currentConfig = this.fixConfig(this.currentConfig);
    }

    if (UrlUtils.getParameter(this.widget.locationAdapter.url(), 'mode') === MODE_PRINT) {
      this.currentConfig.windowSettings.physicalRuler = false;
    }

  }

  mergeInCurrentConfig(config) {
    _.merge(this.currentConfig, config);
  }

  /**
   * Gets the current config of Mirador.
   * @returns {null|*} the Mirador's current config
   */
  getCurrentConfig() {
    return this.miradorInstance.viewer.state.currentConfig;
  }

  getManifestBlobUri() {
    return this.manifestBlobUri;
  }

  setManifest(manifest) {
    this.manifest = manifest;
    this.manifestBlobUri = this.createManifestBlob(manifest);
  }

  getManifest() {
    return this.manifest || {};
  }

  createManifestBlob(manifest) {
    var blob = new Blob([JSON.stringify(manifest)], {
      type: 'application/json'
    });
    return URL.createObjectURL(blob);
  }

  /**
   * The saved config may contain stale data that should be corrected if needed
   * @param manifest the manifest
   * @returns {null|*|*} the updated config
   */
  fixConfig(config) {
    config = this.fixLoadedManifest(config);
    config = this.fixCanvasId(config);
    return config;
  }

  /**
   * Since the config is saved inside the widget it may carry stale canvas ids inside its slots
   * If the canvas id is not presented inside the manifest then load manifest from scratch
   * @param config
   * @returns {*}
   * @private
   */
  fixCanvasId(config) {
    if (!config.windowObjects) {
      return config;
    }
    for (let i = 0; i < config.windowObjects.length; i++) {

      if (config.windowObjects[i].canvasID && this.manifest) {
        let canvasId = config.windowObjects[i].canvasID;
        let found = _.find(this.manifest.sequences[0].canvases, function (canvas) {
          return canvasId === canvas['@id'];
        });

        if (!found) {
          config.windowObjects[i] = {
            loadedManifest: this.getManifestBlobUri(),
            viewType: config.windowObjects[i].viewType,
            slotAddress: config.windowObjects[i].slotAddress,
            windowOptions: {
              osdBounds: config.windowObjects[i].windowOptions ? config.windowObjects[i].windowOptions.osdBounds : undefined
            }
          };

        }
      }
    }

    return config;
  }

  /**
   * After the config is saved and the page is reloaded the current blob uri is removed,
   * so it has to create a new blob uri and to replace the old with the new one in the config.
   * Also Mirador extracts the url from the blob in its state config so after extracting
   * it must be refactored
   *
   * @param manifest the manifest
   * @returns {null|*|*} the updated config
   */
  fixLoadedManifest(config) {
    if (config && config.data) {
      config.data[0].manifestUri = this.manifestBlobUri;
      for (let i = 0; i < config.windowObjects.length; i++) {
        config.windowObjects[i].loadedManifest = this.manifestBlobUri;
      }
    }
    return config;
  }

  reload(forceReload) {
    if (this.viewerFrame.contentWindow) {
      this.viewerFrame.contentWindow.location.reload(forceReload);
    }
  }

  destroy() {
    if (this.eventsAdapter) {
      this.eventsAdapter.destroy();
      this.eventsAdapter = null;
    }

    this.events.forEach(eventSubscription => eventSubscription.unsubscribe());
    this.miradorInstance = null;
  }
}

const MIRADOR_PATH = 'common/lib/mirador/index.html';
MiradorViewer.PATH = Paths.getBaseScriptPath() + MIRADOR_PATH;
