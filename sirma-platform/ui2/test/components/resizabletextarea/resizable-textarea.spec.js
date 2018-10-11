import {ResizableTextarea} from 'components/resizabletextarea/resizable-textarea';
import {NavigatorAdapter} from 'adapters/navigator-adapter';
import {DefinitionModelProperty} from 'models/definition-model';

describe('ResizableTextarea', () => {
  var clock;
  let ieOrEdgeStub;
  let isIEStub;
  let fakeElement;
  let fakeElementSpy;
  let fieldViewModel = new DefinitionModelProperty({
    rendered: true,
    preview: false
  });
  ResizableTextarea.prototype.fieldViewModel = fieldViewModel;
  let resizableTextarea;
  let uiWrapper = {
    css: () => {
    },
  };
  let uiWrapperSpy = sinon.spy(uiWrapper, 'css');

  beforeEach(() => {
    fakeElement = $(`<textarea class="form-control edit-field textarea"></textarea>`);
    fakeElement.parentElement = $(`<div class="form-field"></div>`);
    fakeElement.closest = () => {
      return uiWrapper;
    };
    fakeElementSpy = sinon.spy(fakeElement, 'css');
    isIEStub = sinon.stub(NavigatorAdapter, 'isInternetExplorer');
    ieOrEdgeStub = sinon.stub(NavigatorAdapter, 'isEdgeOrIE', () => true);

    clock = sinon.useFakeTimers();
    isIEStub.returns(true);
    fieldViewModel.preview = false;
  });

  afterEach(() => {
    fakeElementSpy.reset();
    clock.restore();
    resizableTextarea.ngOnDestroy();
    isIEStub.restore();
    ieOrEdgeStub.restore();
    uiWrapperSpy.reset();
  });

  it('should init property if browser is Edge', () => {
    isIEStub.returns(false);
    resizableTextarea = new ResizableTextarea(fakeElement);
    clock.tick(15);

    expect(fakeElementSpy.calledWith({'width': '100%', 'height': '50px'})).to.be.true;
  });

  describe('Internet Explorer', () => {
    beforeEach(() => {
      resizableTextarea = new ResizableTextarea(fakeElement);
      clock.tick(15);
    });

    it('should init properly if browser is InternetExplorer', () => {
      expect(fakeElementSpy.calledWith({'width': '100%', 'height': '46px'}), 'called with').to.be.true;
    });

    it('should handle preview change ', () => {
      let handlePreviewSpy = sinon.spy(resizableTextarea, 'handleVisibility');
      expect(uiWrapperSpy.calledWith('display', 'block')).to.be.true;
      fieldViewModel.preview = true;
      expect(handlePreviewSpy.calledOnce).to.be.true;
      expect(handlePreviewSpy.calledWith(false)).to.be.true;
      expect(uiWrapperSpy.calledWith('display', 'none')).to.be.true;
    });
  });
});