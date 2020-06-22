import {HEADER_DEFAULT, HEADER_COMPACT} from 'instance-header/header-constants';
import {ObjectLinkWidget} from 'idoc/widget/object-link-widget/object-link-widget';
import {IdocMocks} from '../../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('ObjectLinkWidget', () => {
  it('should call instance service with proper arguments', () => {
    var loadInstanceConfig = {
      skipInterceptor: true,
      params: {
        deleted: true,
        properties: [HEADER_COMPACT, "emf:isDeleted"]
      }
    };
    var objectLinkWidget = getObjectLinkWidget();
    expect(objectLinkWidget.instanceRestService.load.calledOnce).to.be.true;
    expect(objectLinkWidget.instanceRestService.load.getCall(0).args[0]).to.be.equal("emf:123");
    expect(objectLinkWidget.instanceRestService.load.getCall(0).args[1]).to.deep.equal(loadInstanceConfig);
  });

  describe('insertLink', () => {
    it('should append the widget inside the dom element', () => {
      var widget = getObjectLinkWidget();
      var spy = sinon.spy();
      widget.$element.append = spy;
      widget.insertLink("header", {});
      expect(spy.called).to.be.true;
    });

    it('should publish ready event', () => {
      var widget = getObjectLinkWidget();
      var spy = sinon.spy();
      widget.eventbus.publish = spy
      widget.insertLink("header", {});
      expect(spy.called).to.be.true;
    });
  });
});

function getObjectLinkWidget() {
  var id = "emf:123";
  ObjectLinkWidget.prototype.config = {
    selectedObject: id
  };
  ObjectLinkWidget.prototype.control = {
    getId: ()=> {
      return id
    }
  }
  var instance = new ObjectLinkWidget(mockInstanceService(), IdocMocks.mockTranslateService(), IdocMocks.mockEventBus(),
    IdocMocks.mockElement(), mock$scope(), mockCompile(), IdocMocks.mockTimeout());
  return instance;
}

function mockInstanceService() {
  var response = {
    data: {
      headers: {
        breadcrumb_header: 'header'
      },
      properties: {
        'emf:isDeleted': false
      }
    }
  };
  return {
    load: sinon.spy(() => {
      return PromiseStub.resolve(response);
    })
  }
}

function mockCompile() {
  var compile = () => {
    return () => {
      return ['test'];
    };
  };
  return sinon.spy(compile);
}