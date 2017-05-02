import {ContentViewer} from 'idoc/widget/content-viewer/content-viewer';
import {PDFJS_PATH} from 'common/pdf-viewer';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';

describe('ContentViewer', () => {

  var scope;
  var widget;

  beforeEach(() => {
    scope = mock$scope();
    let instanceRestService = {};
    let objectSelectorHelper = {
      getSelectedObject: sinon.spy(() => {
        return PromiseStub.reject();
      }),
      isModeling: () => {
        return false;
      }
    };
    var result = {
      contents: () => {
        return {
          on: (evt, callback) => {
            callback && callback();
          }
        }
      },
      append: () => sinon.spy(),
      empty: () => sinon.spy()
    };
    result.length = 1;
    let $element = {
      find: () => {
        return result
      }
    };
    let eventbus = new Eventbus();
    let $compile = () => {
      return ()=> {
        return [{}];
      };
    };
    ContentViewer.prototype.context = {
      isModeling: () => {
        return false;
      }
    };
    widget = new ContentViewer(scope, instanceRestService, objectSelectorHelper, $element, eventbus, $compile);
    widget.control = {
      getId: () => {
        return 'widgetuuid';
      }
    };
  });

  it('should determine that an object is an image by response headers', () => {
    var responseMock = {
      headers: () => {
        return 'image/something'
      }
    };
    expect(widget.isImage(responseMock)).to.be.true;
  });

  it('should determine that an object is not an image by response headers', () => {
    var responseMock = {
      headers: () => {
        return 'application/something'
      }
    };
    expect(widget.isImage(responseMock)).to.be.false;
  });

  it('should call object selector helper with proper arguments', () => {
    scope.$digest();
    var selectorSpy = widget.objectSelectorHelper.getSelectedObject;
    expect(selectorSpy.calledOnce).to.be.true;
    expect(selectorSpy.getCall(0).args[3]).to.deep.equal({ignoreNotPersisted: true});
  });

  it('should show empty widget in modeling view', () => {
    widget.clearMemory = sinon.spy();
    widget.context.isModeling = () => {
      return true;
    };
    widget.config.selectObjectMode = "automatically";
    widget.displaySelectedObject();
    expect(widget.errorMessage).to.equal(undefined);
    expect(widget.clearMemory.calledOnce).to.be.true;
  });

  it('should display the selected object when the selection is changed', () => {
    widget.displaySelectedObject = sinon.spy();
    widget.config.selectedObject = '';
    scope.$digest();
    expect(widget.displaySelectedObject.calledOnce).to.be.true;
  });

  it('should fire widgetReadyEvent if no object is selected', () => {
    let spyEventHandler = sinon.spy();
    widget.eventbus.subscribe(WidgetReadyEvent, spyEventHandler);
    widget.displaySelectedObject();
    expect(spyEventHandler.calledOnce).to.be.true;
    expect(spyEventHandler.getCall(0).args[1].topic).to.equal('widgetReadyEvent');
  });

  it('should fetch the content of a document', () => {
    widget.instanceRestService = {
      preview: sinon.spy(() => {
        return PromiseStub.reject();
      })
    };
    widget.displayContent('some-id');
    expect(widget.instanceRestService.preview.calledOnce).to.be.true;
    expect(widget.instanceRestService.preview.getCall(0).args[0]).to.equal('some-id');
  });

  it('should delete the error message if content is displayable', () => {
    widget.errorMessage = 'some.error';
    widget.isImage = () => {
      return false;
    };
    widget.getContent = () => {
      return "Some PDF";
    };
    var response = getResponse(200);
    widget.createPdfViewer(response);
    expect(widget.errorMessage).to.not.exist;
  });

  it('should display error message if the content is an image', () => {
    widget.errorMessage = undefined;
    widget.isImage = () => {
      return true;
    };
    var response = getResponse(200);
    widget.createPdfViewer(response);
    expect(widget.errorMessage).to.exist;
  });

  it('should display error message if the content is not found', (done) => {
    widget.errorMessage = undefined;
    widget.context = {
      getSharedObject: function () {
        return PromiseStub.reject({statusText: 'error'});
      },
      isModeling: () => {
        return false;
      }
    };
    widget.checkIfPresentAndDisplay('objectId').then(()=> {
      expect(widget.errorMessage).to.equal('error');
      done();
    });
  });

  it('should display error message if the content has no preview', () => {
    widget.errorMessage = undefined;
    widget.isImage = () => {
      return false;
    };
    var response = getResponse(204);
    widget.createPdfViewer(response);
    expect(widget.errorMessage).to.exist;
  });

  it('should construct a PdfViewer if content is displayable', () => {
    widget.pdfViewer = undefined;
    widget.isImage = () => {
      return false;
    };
    widget.getContent = () => {
      return "Some PDF";
    };
    let appendPDFViewerSpy = sinon.spy(widget, 'appendPDFViewer');
    let clearMemorySpy = sinon.spy(widget, 'clearMemory');
    var response = getResponse(200);
    widget.createPdfViewer(response);
    expect(widget.pdfViewer).to.exist;
    expect(appendPDFViewerSpy.callCount).to.equals(1);
    expect(clearMemorySpy.callCount).to.equals(1);
  });

  it('should construct a PdfViewer with correct URL', () => {
    widget.pdfViewer = undefined;
    widget.isImage = () => {
      return false;
    };
    widget.getBlobUrl = () => {
      return 'blob-marley'
    };
    widget.getContent = () => {
      return "Some PDF";
    };
    var response = getResponse(200);
    widget.createPdfViewer(response);
    expect(widget.pdfViewer.src).to.contains(PDFJS_PATH);
    expect(widget.pdfViewer.src).to.contains('file=blob-marley');
  });

  it('should construct a blob URL', () => {
    var data = ['a', 'b'];
    var url = widget.getBlobUrl(data);
    expect(url).to.contains('blob:');
  });

  it('clearMemory should dispose all in memory resources', () => {
    widget.pdfViewer = {
      src: 'testSrc'
    };
    widget.blobUrl = 'blobURL';
    widget.appendPDFViewer();
    let wrapperEmptySpy = sinon.spy(widget.pdfViewerWrapper, 'empty');
    let innerScopeDestroySpy = sinon.spy(widget.innerScope, '$destroy');
    let revokeObjectURLSpy = sinon.spy(URL, 'revokeObjectURL');
    widget.clearMemory();
    expect(wrapperEmptySpy.callCount).to.equals(1);
    expect(innerScopeDestroySpy.callCount).to.equals(1);
    expect(revokeObjectURLSpy.callCount).to.equals(1);
  });

  function getResponse(status) {
    return {
      data: [],
      status: status
    }
  }
});
