import {UrlDecorator} from 'layout/url-decorator/url-decorator';

describe('UrlDecorator', () => {

  it('should decorate on link click', () => {
    let decorator = getDecoratorInstance();
    let jQuerySpy = sinon.spy($.fn, 'closest');
    let event = {
      target: {}
    };
    decorator.decorate(event);
    expect(jQuerySpy.calledOnce).to.be.true;
    $.fn.closest.restore();
  });

  it('should not decorate without event target', () => {
    let decorator = getDecoratorInstance();
    let jQuerySpy = sinon.spy($.fn, 'closest');
    decorator.decorate({});
    expect(jQuerySpy.calledOnce).to.be.false;
    $.fn.closest.restore();
  });

  it('should decorate relative instance links', () => {
    let node = getInstanceLinkNode('/emf/some/url');
    UrlDecorator.decorateUrl(node);
    expect(node.attr('href')).to.equal('/remote/some/url');
  });

  it('should not decorate non-relative instance links', () => {
    let node = getInstanceLinkNode('http://non-relative/emf/url');
    let url = 'http://non-relative/emf/url';
    UrlDecorator.decorateUrl(node);
    expect(node.attr('href')).to.equal(url);
  });

  describe('decorate', () => {
    it('should call decorator with the current link node if any but won\'t reload', () => {
      let decorator = getDecoratorInstance();
      let stubDecorate = sinon.stub(UrlDecorator, 'decorateUrl');
      let jQueryStub = sinon.stub($.fn, 'closest');
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-1');
      jQueryStub.withArgs(UrlDecorator.INSTANCE_LINK_SELECTOR).returns(jqObject);
      let event = {
        target: {}
      };

      decorator.router.getStateUrl = () => '#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449';

      decorator.decorate(event);
      expect(stubDecorate.calledOnce).to.be.true;
      expect(decorator.router.navigate.notCalled).to.be.true;
      jQueryStub.restore();
      stubDecorate.restore();
    });

    it('should call decorator and trigger page reload if current view url is same as the triggered link', () => {
      let decorator = getDecoratorInstance();
      let stubDecorate = sinon.stub(UrlDecorator, 'decorateUrl');
      let jQueryStub = sinon.stub($.fn, 'closest');
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');
      jQueryStub.withArgs(UrlDecorator.INSTANCE_LINK_SELECTOR).returns(jqObject);
      let event = {
        target: {},
        stopPropagation: sinon.spy(),
        preventDefault: sinon.spy()
      };

      decorator.router.getStateUrl = () => '#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449';

      decorator.decorate(event);
      expect(stubDecorate.calledOnce).to.be.true;
      expect(decorator.router.navigate.calledOnce).to.be.true;
      expect(decorator.router.navigate.getCall(0).args).to.eql([
        'idoc',
        {param1: 'param1'},
        {reload: true}
      ]);
      jQueryStub.restore();
      stubDecorate.restore();
    });

    it('should not reload the page if the link is suppressed', () => {
      let decorator = getDecoratorInstance();
      let jQueryStub = sinon.stub($.fn, 'closest');
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');
      jqObject[0].classes = [UrlDecorator.SUPPRESSED_LINK_CLASS];

      jQueryStub.withArgs(UrlDecorator.INSTANCE_LINK_SELECTOR).returns(jqObject);
      let event = {
        target: {},
        stopPropagation: sinon.spy(),
        preventDefault: sinon.spy()
      };
      decorator.router.getStateUrl = () => '#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449';

      decorator.decorate(event);
      expect(decorator.router.navigate.called).to.be.false;
      expect(event.stopPropagation.called).to.be.false;
      expect(event.preventDefault.called).to.be.false;

      jQueryStub.restore();
    });
  });

  describe('stripSearch', () => {
    it('should return url without the server and protocol in the beggining and the search part in the end', () => {
      let actual = UrlDecorator.stripSearch('http://10.10.10.10:5000/#/idoc/emf:123456?type=documentinstance&instanceId=emf:123456');
      expect(actual).to.equal('#/idoc/emf:123456');
    });

    it('should strip only the server address and protocol if there is no search part', () => {
      let actual = UrlDecorator.stripSearch('http://10.10.10.10:5000/#/idoc/emf:123456');
      expect(actual).to.equal('#/idoc/emf:123456');
    });
  });

  describe('shouldReload', () => {
    it('should return true if widnow.location.href matches the triggered link source and the current state url matches the link', () => {
      let decorator = getDecoratorInstance('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');

      decorator.router.getStateUrl = () => '#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449';

      expect(decorator.shouldReload(jqObject)).to.be.true;
    });

    it('should return false if widnow.location.href differ from the triggered link source', () => {
      let decorator = getDecoratorInstance();
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3333333');

      decorator.router.getStateUrl = () => '#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449';

      expect(decorator.shouldReload(jqObject)).to.be.false;
    });

    it('should return false if widnow.location.href matches the triggered link but the current state url does not match the link', () => {
      let decorator = getDecoratorInstance('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');
      let jqObject = getInstanceLinkNode('http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449');

      decorator.router.getStateUrl = () => '#/idoc/test';

      expect(decorator.shouldReload(jqObject)).to.be.false;
    });
  });
});

function getDecoratorInstance() {
  let $state = {
    current: {
      name: 'idoc',
      url: ''
    }
  };
  let router = {
    navigate: sinon.stub(),
    getCurrentState: function () {
      return $state.current.name;
    }
  };
  let stateParamsAdapter = {
    getStateParams: sinon.stub().returns({
      param1: 'param1'
    })
  };
  let windowAdapter = {
    location: {
      href: 'http://ip:port/#/idoc/emf:3c669b39-be2c-407f-9cdf-3cf98cd11449'
    }
  };
  return new UrlDecorator(router, stateParamsAdapter, windowAdapter, $state);
}

function getInstanceLinkNode(href) {
  let jqObject = [{
    className: 'instance-link',
    classes: [],
    attributes: {
      href: {
        value: href
      }
    }
  }];
  jqObject.attr = (name, newValue) => {
    if (!newValue) {
      return jqObject[0].attributes.href.value
    }
    jqObject[0].attributes.href.value = newValue;
  };
  jqObject.hasClass = (clazz) => {
    return jqObject[0].classes.indexOf(clazz) > -1;
  };
  return jqObject;
}