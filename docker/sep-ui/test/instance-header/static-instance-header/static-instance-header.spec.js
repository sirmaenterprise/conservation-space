import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {DialogService} from 'components/dialog/dialog-service';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('StaticInstanceHeader', () => {

  let header;

  beforeEach(() => {
    header = new StaticInstanceHeader(mockElement(), mockCompile(), mock$scope(), IdocMocks.mockTimeout(), stub(DialogService));
  });

  it('should configure the header to be enabled by default', () => {
    expect(header.config.disabled).to.be.false;
  });

  it('should configure the header to not prevent link redirect by default', () => {
    expect(header.config.preventLinkRedirect).to.be.false;
  });

  it('should configure the header to not show redirect confirm dialog by default', () => {
    expect(header.config.linkRedirectDialog).to.be.false;
  });

  it('should not trigger compile if header is not present', () => {
    header.ngOnInit();
    expect(header.$compile.calledOnce).to.be.false;
  });

  it('should compile if header is present', () => {
    header.header = '<span>Header</span>';
    header.ngOnInit();
    expect(header.$compile.calledOnce).to.be.true;
  });

  it('should assign the header class onto the header', () => {
    header.header = '<span>Header</span>';
    header.assignHeaderIconClass = sinon.spy();
    header.ngOnInit();
    expect(header.assignHeaderIconClass.callCount).to.equals(1);
  });

  describe('when header or loaded flag changes', () => {
    it('should recompile & assign new header', () => {
      header.header = '<span>Header</span>';
      header.ngOnInit();
      header.$scope.$digest();
      header.$compile.reset();
      header.header = '<span>Header 2</span>';
      header.$scope.$digest();

      expect(header.$compile.calledOnce).to.be.true;
    });

    it('should not recompile & assign the header if it is the same', () => {
      header.header = '<span>Header</span>';
      header.ngOnInit();
      header.$scope.$digest();
      header.$compile.reset();

      header.header = '<span>Header</span>';
      header.$scope.$digest();

      expect(header.$compile.called).to.be.false;
    });

    it('should recompile and assign header when loaded flag is raised', () => {
      header.header = '<span>Header</span>';
      header.ngOnInit();
      header.$scope.$digest();
      header.$compile.reset();
      header.config.loaded = true;
      header.$scope.$digest();
      expect(header.$compile.calledOnce).to.be.true;

      header.$compile.reset();
      header.config.loaded = false;
      header.$scope.$digest();
      expect(header.$compile.notCalled).to.be.true;
    });
  });

  it('should prevent link redirect & not show a confirmation dialog if configured to do so', () => {
    let _$ = $;
    header.ngOnInit();

    let linkElements = mockElements(1);
    header.$element.find = sinon.spy(() => ({
      find: sinon.spy(() => linkElements)
    }));
    $ = function (arg) {
      return arg;
    };
    header.showRedirectConfirmation = sinon.spy();
    header.config.linkRedirectDialog = false;
    header.config.preventLinkRedirect = true;

    header.decorateInstanceLinkElement();

    _.forEach(linkElements, (linkElement) => {
      expect(linkElement.click.calledOnce).to.be.true;
      expect(linkElement.addClass.calledOnce).to.be.true;
      expect(linkElement.addClass.getCall(0).args[0]).to.equal(UrlDecorator.SUPPRESSED_LINK_CLASS);

      let clickHandler = linkElement.click.getCall(0).args[0];
      let event = {
        preventDefault: sinon.spy()
      };
      clickHandler(event);
      expect(event.preventDefault.calledOnce).to.be.true;
    });
    // element find should always be be called
    expect(header.$element.find.calledOnce).to.be.true;
    // dialog should not be called since preventLinkRedirect is true
    expect(header.showRedirectConfirmation.calledOnce).to.be.false;
    $ = _$;
  });

  it('should create a confirmation redirect dialog when configured to do so', () => {
    let _$ = $;
    header.ngOnInit();

    let linkElements = mockElements(1);
    header.$element.find = sinon.spy(() => ({
      find: sinon.spy(() => linkElements)
    }));
    $ = function (arg) {
      return arg;
    };
    header.showRedirectConfirmation = sinon.spy();
    header.config.linkRedirectDialog = true;
    header.config.preventLinkRedirect = false;

    header.decorateInstanceLinkElement();

    _.forEach(linkElements, (linkElement) => {
      expect(linkElement.click.calledOnce).to.be.true;
      expect(linkElement.addClass.calledOnce).to.be.true;
      expect(linkElement.addClass.getCall(0).args[0]).to.equal(UrlDecorator.SUPPRESSED_LINK_CLASS);

      let clickHandler = linkElement.click.getCall(0).args[0];
      let event = {
        preventDefault: sinon.spy()
      };
      clickHandler(event);
      expect(event.preventDefault.calledOnce).to.be.true;
    });
    // element find should always be be called
    expect(header.$element.find.calledOnce).to.be.true;
    // dialog should be called since preventLinkRedirect is false
    expect(header.showRedirectConfirmation.calledOnce).to.be.true;
    $ = _$;
  });

  it('should properly configure the confirmation dialog through dialog service', () => {
    header.ngOnInit();
    let linkElement = mockElement();
    linkElement.click = sinon.spy();
    linkElement.unbind = sinon.spy();

    header.showRedirectConfirmation(linkElement);
    expect(header.dialogService.confirmation.calledOnce).to.be.true;

    let dialogConfig = header.dialogService.confirmation.getCall(0).args[2];

    expect(dialogConfig.onButtonClick).to.exist;
    expect(dialogConfig.buttons.length).to.eq(2);
    expect(header.dialogService.createButton.calledTwice).to.be.true;
    expect(header.dialogService.createButton.getCall(0).calledWith(DialogService.YES)).to.be.true;
    expect(header.dialogService.createButton.getCall(1).calledWith(DialogService.NO)).to.be.true;

    let dialogDismiss = {
      dismiss: sinon.spy()
    };
    dialogConfig.onButtonClick(DialogService.YES, undefined, dialogDismiss);
    expect(linkElement.unbind.calledOnce).to.be.true;
    expect(dialogDismiss.dismiss.calledOnce).to.be.true;
  });

  it('should destroy inner scope and clear header wrapper element', () => {
    header.innerScope = {
      $destroy: sinon.spy()
    };
    header.instanceDataElement = {
      empty: sinon.spy()
    };

    header.clearData();

    expect(header.innerScope.$destroy.callCount).to.equals(1);
    expect(header.instanceDataElement.empty.callCount).to.equals(1);
  });
});

function mockElements(numElements) {
  let elements = [];
  for (let i = 0; i < numElements; ++i) {
    let element = mockElement();
    element.length = 1;
    elements.push(element);
  }
  return elements;
}

function mockElement() {
  return {
    find: sinon.spy(() => {
      return {
        append: () => {
        },
        empty: () => {
        },
        parent: () => {
          return {
            addClass: () => {
            }
          };
        }
      };
    }),
    click: sinon.spy(),
    addClass: sinon.spy()
  };
}

function mockCompile() {
  let compile = () => {
    let returnFunc = () => {
      return 'compile';
    };
    return sinon.spy(returnFunc);
  };
  return sinon.spy(compile);
}
