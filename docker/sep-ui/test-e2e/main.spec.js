describe('Main module', function () {

  it('should bootstrap the application', function () {
    browser.get('/sandbox#/idoc/john@domain?jwt=token');

    browser.wait(EC.visibilityOf($('#layout')), DEFAULT_TIMEOUT);
  });
});
