var Collapsible = require('./collapsible.js').Collapsible;
var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Collapsible', () => {

  beforeEach(() => {
    page.open('/sandbox/components/collapsible');
  });

  it('should collapse/expand first and second section when clicked', () => {
    var firstCollapsible = new Collapsible('#first-section-collapse');
    var secondCollapsible = new Collapsible('#second-section-collapse');

    expect(firstCollapsible.isCollapsed()).to.eventually.be.true;
    expect(secondCollapsible.isCollapsed()).to.eventually.be.false;

    firstCollapsible.toggleCollapse();
    secondCollapsible.toggleCollapse();

    expect(firstCollapsible.isCollapsed()).to.eventually.be.false;
    expect(secondCollapsible.isCollapsed()).to.eventually.be.true;

    firstCollapsible.toggleCollapse();
    secondCollapsible.toggleCollapse();

    expect(firstCollapsible.isCollapsed()).to.eventually.be.true;
    expect(secondCollapsible.isCollapsed()).to.eventually.be.false;
  });

  it('should show section collapsed by default if it\'s empty', () => {
    var thirdCollapsible = new Collapsible('#third-section-collapse');
    expect(thirdCollapsible.isCollapsedAndEmpty()).to.eventually.be.true;
  });
});
