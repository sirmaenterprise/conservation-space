import {View, Inject, NgScope, NgElement, NgCompile} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY} from 'idoc/widget/object-selector/object-selector';
import {DEFAULT_CONTENT_TYPE, OCR_CONTENT_TYPE} from 'idoc/widget/content-viewer/content-viewer-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {StatusCodes} from 'services/rest/status-codes';
import _ from 'lodash';

import template from './content-viewer.html!text';

const LABEL_PREVIEW_NONE = 'labels.preview.none';
// NOTE: discuss with others this configuration property to be placed inside the db
export const PDF_CHUNK_SIZE = 5242880;

@Widget
@View({
  template
})
@Inject(NgScope, NgElement, NgCompile, Eventbus, ObjectSelectorHelper, PluginsService)
export class ContentViewer {

  constructor($scope, $element, $compile, eventbus, objectSelectorHelper, pluginsService) {
    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;
    this.eventbus = eventbus;
    this.objectSelectorHelper = objectSelectorHelper;
    this.pluginsService = pluginsService;

    this.config = this.config || {};
    this.config.contentType = this.config.contentType || DEFAULT_CONTENT_TYPE;

    this.registerWatchers();
  }

  registerWatchers() {
    this.$scope.$watchCollection(() => {
      return [this.config.selectObjectMode, this.config.selectedObject, this.config.contentType];
    }, this.changeHandler.bind(this));
  }

  changeHandler() {
    if (this.widgetShouldBeEmpty()) {
      this.clearMemory();
      delete this.errorMessage;
      this.fireWidgetReadyEvent();
      return;
    }

    let selectorArguments = {ignoreNotPersisted: true};
    this.objectSelectorHelper.getSelectedObject(this.config, this.context, undefined, selectorArguments).then((objectId) => {
      this.checkIfPresentAndDisplay(objectId);
    }).catch((rejection) => {
      this.errorMessage = rejection.reason;
      // If no object is selected to be displayed mark widget as ready for print
      this.fireWidgetReadyEvent();
    });
  }

  checkIfPresentAndDisplay(objectId) {
    return this.context.getSharedObject(objectId, this.control.getId(), true).then((object) => {
      this.displayContent(object);
    }).catch((error) => {
      // If requested object is not found, then its identifier is removed from the widget config. This results in watcher
      // being triggered again. Then ObjectSelectionHelper.getSelectedObject rejects with no selection.
      if (error && error.status === StatusCodes.NOT_FOUND && this.config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
        this.objectSelectorHelper.removeSelectedObjects(this.config, [objectId]);
        this.control.saveConfig(this.config);
      } else {
        this.errorMessage = error.statusText;
        this.fireWidgetReadyEvent();
      }
    });
  }

  displayContent(object) {
    this.clearMemory();
    let compatibleViewer = this.getCompatibleViewer(object);
    if (compatibleViewer) {
      delete this.errorMessage;
      this.pluginsService.executeImport(compatibleViewer.module).then(() => {
        let comp = `<${compatibleViewer.component} instance-id="'${object.getId()}'" mode="'${this.context.getMode()}'" content-type="'${this.config.contentType}'" on-ready="contentViewer.fireWidgetReadyEvent()"></${compatibleViewer.component}>`;

        this.innerScope = this.$scope.$new();
        let viewerComponent = this.$compile(comp)(this.innerScope);

        this.viewerWrapper = this.$element.find('.viewer-wrapper');
        this.viewerWrapper.append(viewerComponent);
      });
    } else {
      this.errorMessage = LABEL_PREVIEW_NONE;
      this.fireWidgetReadyEvent();
    }
  }

  getCompatibleViewer(object) {
    let mimeType = object.getPropertyValue('mimetype');
    let mediaViewers = this.pluginsService.getDefinitions('media-viewers');
    let compatibleViewer = _.find(mediaViewers, (mediaViewer) => {
      return _.find(mediaViewer.mimetypes, (supportedMimeType) => {
        // wildcard support
        supportedMimeType = supportedMimeType.replace(/[*]/g, '.*');
        return new RegExp('^' + supportedMimeType + '$').test(mimeType);
      });
    });

    // fall back to pdf viewer if there is no compatible viewer found because Alfresco can generate pdf preview for some mimetypes
    if (!compatibleViewer || this.config.contentType === OCR_CONTENT_TYPE) {
      compatibleViewer = _.find(mediaViewers, (mediaViewer) => {
        return mediaViewer.name === 'pdf-viewer';
      });
    }
    return compatibleViewer;
  }

  fireWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  widgetShouldBeEmpty() {
    return this.context.isModeling() && (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY || this.config.selectObjectMode === SELECT_OBJECT_CURRENT);
  }

  clearMemory() {
    if (this.innerScope) {
      this.innerScope.$destroy();
    }

    if (this.viewerWrapper) {
      this.viewerWrapper.empty();
    }
  }

  ngOnDestroy() {
    this.clearMemory();
  }
}
