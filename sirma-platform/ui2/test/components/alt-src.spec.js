import {AltSrc} from 'components/alt-src';

var element;

describe('AltSrc', function() {

  beforeEach(function() {
    var attrStub = sinon.stub();
    attrStub.withArgs('src').returns(1);
    attrStub.withArgs('alt-src').returns(1);

    element = {
      on: sinon.spy(),
      attr: attrStub
    };
  });

  it('should bind to error event', function() {
    new AltSrc(element);

    expect(element.on.calledOnce).to.be.true;
    expect(element.on.getCall(0).args[0]).to.eq('error');
    expect(typeof element.on.getCall(0).args[1]).to.eq('function');
  });

  describe('handleErrorSrc(element)', function() {

    it('should not set src if src and alt-src have the same value', function() {
      new AltSrc(element).handleErrorSrc();

      expect(element.attr.calledTwice).to.be.true;
    });

    it('should set src if src and alt-src have different values', function() {
      element.attr.withArgs('alt-src').returns(2);
      new AltSrc(element).handleErrorSrc();

      expect(element.attr.calledThrice).to.be.true;
      expect(element.attr.getCall(2).args[0]).to.eq('src');
      expect(element.attr.getCall(2).args[1]).to.eq(2);
    });
  });
});