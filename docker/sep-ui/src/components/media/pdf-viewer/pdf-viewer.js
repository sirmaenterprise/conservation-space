import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {AuthenticationService} from 'security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {MODE_PRINT} from 'idoc/idoc-constants';
import Paths from 'common/paths';

import template from './pdf-viewer.html!text';
import './pdf-viewer.css!';

const PDFJS_PATH = 'common/lib/pdfjs/web/viewer.html';
const PAGES_LOADED_EVENT = 'pagesloaded';
const PDF_CHUNK_SIZE = 5242880;

@Component({
  selector: 'seip-pdf-viewer',
  properties: {
    'instanceId': 'instance-id',
    'contentType': 'content-type',
    'mode': 'mode'
  },
  events: ['onReady']
})
@View({
  template
})
@Inject(NgElement, NgScope, AuthenticationService, InstanceRestService)
export class PdfViewer {
  constructor($element, $scope, authenticationService, instanceRestService) {
    this.$element = $element;
    this.$scope = $scope;
    this.authenticationService = authenticationService;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    this.src = this.instanceRestService.getContentPreviewUrl(this.instanceId, this.contentType);

    let zoom = 'auto';
    this.pdfConfig = {
      zoom
    };
    this.$element.find('.pdf-viewer-body').append(this.createIframe());
  }

  createIframe() {
    let pdfViewerSrc = this.createPdfViewerSrc();
    let iframe = $(`<iframe name="pdf-viewer" allowfullscreen="true" src="${pdfViewerSrc}"></iframe>`);
    let onLoad = () => {
      this.getPdfConfig().then(config => {
        return this.open(iframe[0], config);
      }).then(() => {
        iframe.parent().removeClass('hidden');
        iframe.contents().on(PAGES_LOADED_EVENT, () => {
          this.prepareForPrint(iframe.contents());
          this.$scope.$evalAsync(() => {
            this.onReady();
          });
        });
      }).catch(() => {
        this.$scope.$evalAsync(() => {
          iframe.parent().empty();
          this.errorMessage = 'labels.preview.none';
          this.onReady();
        });
      });
    };
    iframe.on('load', onLoad);
    return iframe;
  }

  createPdfViewerSrc() {
    let pdfViewerSrc = Paths.getBaseScriptPath() + PDFJS_PATH + '?file=';
    let separator = '#';
    return Object.keys(this.pdfConfig).reduce((previous, key) => {
      let result = `${previous}${separator}${key}=${this.pdfConfig[key]}`;
      separator = '&';
      return result;
    }, pdfViewerSrc);
  }

  open(iframe, config) {
    let viewerInstance = this.getViewerInstance(iframe);
    if (viewerInstance) {
      return viewerInstance.open(config.url, config.params);
    }
  }

  getViewerInstance(iframe) {
    let contentWindow = iframe.contentWindow;
    if (contentWindow && contentWindow.PDFViewerApplication) {
      return contentWindow.PDFViewerApplication;
    }
  }

  prepareForPrint(iframeContents) {
    if (this.mode === MODE_PRINT) {
      let visiblePage = iframeContents.find('.page[data-loaded="true"]').eq(0);
      let canvasDataUrl = visiblePage.find('canvas')[0].toDataURL();
      let image = $('<img>', {src: canvasDataUrl});
      let viewerBody = this.$element.find('.pdf-viewer-body');

      viewerBody.find('iframe').remove();
      viewerBody.append(image);
    }
  }

  getPdfConfig() {
    return this.authenticationService.getToken().then(token => {
      return {
        url: this.src,
        params: {
          httpHeaders: {
            jwt: token
          },
          rangeChunkSize: PDF_CHUNK_SIZE
        }
      };
    });
  }
}
