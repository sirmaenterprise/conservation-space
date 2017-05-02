import {ToTrustedHtml} from 'filters/to-trusted-html';

describe('ToTrustedHtml', () => {

  var toTrustedHtml;
  beforeEach(() => {
    toTrustedHtml = new ToTrustedHtml(contextualEscapeAdapterMock());
  });

  it('should call angular`s sanitizer', () => {
    toTrustedHtml.filter('test');
    expect(toTrustedHtml.contextualEscapeAdapter.trustAsHtml.calledOnce);
    expect(toTrustedHtml.contextualEscapeAdapter.trustAsHtml.getCall(0).args[0]).to.equal('test');
  });

  it('should ensure the value is string before calling angular`s sanitizer', () => {
    toTrustedHtml.filter(123);
    expect(toTrustedHtml.contextualEscapeAdapter.trustAsHtml.calledOnce);
    expect(toTrustedHtml.contextualEscapeAdapter.trustAsHtml.getCall(0).args[0]).to.equal('123');
  });
});

function contextualEscapeAdapterMock() {
  return {
    trustAsHtml: sinon.spy()
  };
}