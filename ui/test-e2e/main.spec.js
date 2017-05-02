describe('Main module', function () {

  it('should bootstrap the application', function () {
    browser.get('/sandbox');

    browser.wait(EC.visibilityOf($('#layout')), DEFAULT_TIMEOUT);
  });
});