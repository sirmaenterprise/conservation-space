'use strict';
var HeaderContainerSandboxPage = require('./header-container').HeaderContainerSandboxPage

var page = new HeaderContainerSandboxPage();

describe('HeaderContainer', function () {

  var headerContainer;
  beforeEach(function () {
    page.open();
    headerContainer = page.getHeaderContainer();
  });

  it('should render header title', function () {
    expect(headerContainer.getHeader().getHeaderAsText()).to.eventually.equal('NN(Title) NN (Status)\nOwner: User name, Created on: DD.NN.YYYY, HH:mm');
  });

  it('should render header action menu', function () {
    expect(headerContainer.getActionMenu().getTriggerButton().isDisplayed()).to.eventually.be.true;
  });
});
