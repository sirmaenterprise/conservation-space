import {PdfViewer} from 'components/media/pdf-viewer/pdf-viewer';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {AuthenticationService} from 'services/security/authentication-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {MODE_PRINT, MODE_EDIT} from 'idoc/idoc-constants';
import {stub} from 'test/test-utils';

describe('PdfViewer', () => {
  let pdfViewer;
  let elementMock;

  beforeEach(() => {
    elementMock = {
      append: sinon.spy(),
      remove: sinon.spy(),
      find: (selector) => {
        if (selector === 'canvas') {
          return [{
            toDataURL: () => 'test-data-url'
          }];
        }
        return elementMock;
      },
      eq: () => elementMock
    };
    pdfViewer = new PdfViewer(elementMock, mock$scope(), stub(AuthenticationService), stub(InstanceRestService));
  });

  it('should generate proper configuration for PDF.js viewer', () => {
    pdfViewer.createIframe = sinon.spy();

    pdfViewer.mode = MODE_EDIT;
    pdfViewer.ngOnInit();
    expect(pdfViewer.pdfConfig.zoom).to.equals('auto');
  });

  it('should create iframe on init', () => {
    pdfViewer.createIframe = sinon.spy();
    pdfViewer.ngOnInit();
    expect(pdfViewer.createIframe.callCount).to.equals(1);
  });

  it('should create correct src string based on the config', () => {
    pdfViewer.pdfConfig = {
      page: 2,
      zoom: 'auto'
    };
    expect(pdfViewer.createPdfViewerSrc()).to.equals('/common/lib/pdfjs/web/viewer.html?file=#page=2&zoom=auto');
  });

  it('should open the pdf by viewer', () => {
    let spy = sinon.spy();
    pdfViewer.open(mockIframe(spy), {});
    expect(spy.called).to.be.true;
  });

  it('should open the pdf with proper arguments', () => {
    let spy = sinon.spy();
    pdfViewer.open(mockIframe(spy), {
      url: 'url',
      params: 'params'
    });
    let result = spy.getCall(0);
    expect(result.args[0]).to.equal('url');
    expect(result.args[1]).to.equal('params');
  });

  describe('in print mode', () => {
    it('should extract canvas contents and append to the pdf viewer body', () => {
      pdfViewer.mode = MODE_PRINT;
      pdfViewer.prepareForPrint(elementMock);

      expect(elementMock.remove.calledOnce).to.be.true;
      expect(elementMock.append.calledOnce).to.be.true;
      expect(elementMock.append.getCall(0).args[0].attr('src')).to.equal('test-data-url');
    });

  });

});

function mockIframe(spy) {
  return {
    contentWindow: {
      PDFViewerApplication: {
        open: spy
      }
    }
  };
}
