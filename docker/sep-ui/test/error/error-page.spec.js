import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ErrorPage} from 'error/error-page';

describe('Tests for ErrorPage component', () => {
  it('should return correct message when "not_found" error key is sent to error page', () => {
    const MESSAGE = 'Sorry, the page you requested was not found';
    let translateService = mockTranslateService(MESSAGE);
    let translateSpy = sinon.spy(translateService, 'translateInstant');
    let errorPage = new ErrorPage(mockStateParamsAdapter(ErrorPage.NOT_FOUND), translateService);

    expect(errorPage.errorKey).to.equal(ErrorPage.NOT_FOUND);
    expect(errorPage.errorMessage).to.equal(MESSAGE);
    expect(translateSpy.calledWith('error.page.not.found')).to.be.true;
  });

  it('should return correct message when "error.page.not.unknown" error key is sent to error page', () => {
    var MESSAGE = 'Web server is returning an unknown error';
    let translateService = mockTranslateService(MESSAGE);
    let translateSpy = sinon.spy(translateService, 'translateInstant');
    let errorPage = new ErrorPage(mockStateParamsAdapter(ErrorPage.UNKNOWN), translateService);

    expect(errorPage.errorKey).to.equal(ErrorPage.UNKNOWN);
    expect(errorPage.errorMessage).to.equal(MESSAGE);
    expect(translateSpy.calledWith('error.page.unknown')).to.be.true;
  });

  it('should return undefined when undefined error key is sent to error page', () => {
    const KEY = 'undefined_key';
    let errorPage = new ErrorPage(mockStateParamsAdapter(KEY), mockTranslateService());

    expect(errorPage.errorKey).to.equal(KEY);
    expect(errorPage.errorMessage).to.equal(undefined);
  });
});

function mockStateParamsAdapter(error) {
  let stateParams = {
    'key': error
  };
  return new StateParamsAdapter(stateParams);
}

function mockTranslateService(message) {
  return {
    translateInstant: ()=> message
  };
}