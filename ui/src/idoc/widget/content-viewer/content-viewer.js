import {View, Inject, NgScope, NgElement, NgCompile} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Eventbus} from 'services/eventbus/eventbus';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {MODE_PRINT} from 'idoc/idoc-constants';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {InstanceRestService} from 'services/rest/instance-service';
import {ObjectSelector} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {SELECT_OBJECT_AUTOMATICALLY, SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {PdfViewer} from 'common/pdf-viewer';
import './content-viewer.css!';
import template from './content-viewer.html!text';

const LABEL_PREVIEW_NONE = 'labels.preview.none';

@Widget
@View({
  template: template
})
@Inject(NgScope, InstanceRestService, ObjectSelectorHelper, NgElement, Eventbus, NgCompile, StateParamsAdapter)
export class ContentViewer {

  constructor($scope, instanceRestService, objectSelectorHelper, $element, eventbus, $compile, stateParamsAdapter) {
    this.$scope = $scope;
    this.eventbus = eventbus;
    this.$element = $element;
    this.$compile = $compile;
    this.instanceRestService = instanceRestService;
    this.objectSelectorHelper = objectSelectorHelper;
    this.stateParamsAdapter = stateParamsAdapter;
    this.config = this.config || {};

    this.registerWatchers();
  }

  registerWatchers() {
    this.$scope.$watchCollection(() => {
      return [this.config.selectObjectMode, this.config.selectedObject];
    }, () => this.displaySelectedObject());
  }

  displaySelectedObject() {
    if (this.widgetShouldBeEmpty()) {
      this.clearMemory();
      delete this.errorMessage;
      return;
    }
    var selectorArguments = {ignoreNotPersisted: true};
    this.objectSelectorHelper.getSelectedObject(this.config, this.context, undefined, selectorArguments).then((objectId) => {
      this.checkIfPresentAndDisplay(objectId);
    }).catch((rejection) => {
      // If no object is selected to be displayed mark widget as ready for print
      this.fireWidgetReadyEvent();
      this.errorMessage = rejection.reason;
    });
  }

  checkIfPresentAndDisplay(objectId) {
    return this.context.getSharedObject(objectId, this.control.getId(), true).then(() => {
      this.displayContent(objectId);
    }).catch((error)=> {
      // If the selected object is not found mark widget as ready for print
      this.fireWidgetReadyEvent();
      return this.errorMessage = error.statusText;
    });
  }

  displayContent(objectId) {
    this.instanceRestService.preview(objectId).then((response) => {
      this.createPdfViewer(response);
    }).catch(() => {
      this.errorMessage = LABEL_PREVIEW_NONE;
      this.fireWidgetReadyEvent();
    });
  }

  createPdfViewer(response) {
    if (!this.hasPreview(response)) {
      this.errorMessage = LABEL_PREVIEW_NONE;
      this.fireWidgetReadyEvent();
    } else {
      this.clearMemory();
      this.blobUrl = this.getBlobUrl(response.data);
      this.pdfViewer = new PdfViewer(this.blobUrl, {
        zoom: 'page-fit'
      });
      this.appendPDFViewer();
      delete this.errorMessage;
    }
  }

  /**
   * Creates and appends pdf viewer iframe
   */
  appendPDFViewer() {
    let iframeHTML = `<iframe name="pdf-viewer" src="${this.pdfViewer.src}"></iframe>`;
    this.pdfViewerWrapper = this.$element.find('.pdf-viewer-wrapper');
    this.innerScope = this.$scope.$new();
    let iframe = this.$compile(iframeHTML)(this.innerScope, (clonedElement) => {
      clonedElement.on('load', () => {
        let mainContainer = clonedElement.contents().find('#mainContainer');
        clonedElement.contents().on('pagesloaded', () => {
          this.$scope.$evalAsync(() => {
            this.fireWidgetReadyEvent();
          });
          // Remove page border and center the page
          if (this.stateParamsAdapter.getStateParam('mode') === MODE_PRINT) {
            mainContainer.find('#pageContainer1').css({
              'border': 'none',
              'margin-left': '-30px',
              'margin-top': '-20px'
            });
            mainContainer.find('#viewer').children().not('#pageContainer1').css('visibility', 'hidden');
          }
        });
        // If page can't fit the container make pdf thumbnail smaller
        if (this.stateParamsAdapter.getStateParam('mode') === MODE_PRINT) {
          mainContainer.width(mainContainer.width() * 42 / 100).height(mainContainer.height() * 90 / 100);
        }
      });
    })[0];

    this.pdfViewerWrapper.append(iframe);
  }

  fireWidgetReadyEvent() {
    this.eventbus.publish(new WidgetReadyEvent({
      widgetId: this.control.getId()
    }));
  }

  hasPreview(response) {
    // If the status is anything else than 200 it is considered that the instance has no preview. However images
    // are returned with 200 but cannot be shown in this widget.
    return response.status === 200 && !this.isImage(response);
  }

  isImage(response) {
    return response.headers('content-type').indexOf('image') > -1;
  }

  getBlobUrl(data) {
    return URL.createObjectURL(new Blob([data], {
      type: "application/pdf"
    }));
  }

  widgetShouldBeEmpty() {
    return this.context.isModeling() && (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY || this.config.selectObjectMode === SELECT_OBJECT_CURRENT);
  }

  ngOnDestroy() {
    this.clearMemory();
  }

  clearMemory() {
    if (this.pdfViewerWrapper) {
      this.pdfViewerWrapper.empty();
    }
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl);
    }
  }
}
