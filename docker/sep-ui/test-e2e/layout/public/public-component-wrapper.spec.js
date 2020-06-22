let PublicComponentWrapperSandbox = require('./public-component-wrapper').PublicComponentWrapperSandbox;

describe('PublicComponentWrapper', () => {

  let page = new PublicComponentWrapperSandbox();
  let wrapper;

  it('should have main logo', () => {
    openSandboxPage();

    wrapper.waitForMainLogo();
  });

  it('should have powered by logo', () => {
    openSandboxPage();

    wrapper.waitForPoweredByLogo();
  });

  it('should load home component', () => {
    openSandboxPage(PublicComponentWrapperSandbox.HOME_COMPONENT_ID);

    wrapper.waitForHomeComponent();
  });

  it('should load info component', () => {
    openSandboxPage(PublicComponentWrapperSandbox.INFO_COMPONENT_ID);

    wrapper.waitForInfoComponent();
  });

  function openSandboxPage(componentId) {
    page.open(componentId);
    wrapper = page.getPublicComponentWrapper();
  }

});