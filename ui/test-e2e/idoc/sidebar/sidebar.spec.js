const ObjectBrowser = require('../../components/object-browser/object-browser.js').ObjectBrowser;
const Sidebar = require('./sidebar').Sidebar;
const SidebarSandboxPage = require('./sidebar').SidebarSandboxPage;

var page = new SidebarSandboxPage();

describe('Test sidebar component', function () {
  var sidebar;

  beforeEach(() => {
    page.open();
    sidebar = page.getSidebar();
  });

  afterEach(() => {
    sidebar.onDestroy();
  });

  it('should be collapsed by default', function () {
    expect(sidebar.isCollapsed()).to.eventually.be.true;
  });

  it('should be expanded after expand/collapsed button is clicked', function () {
    sidebar.toggleCollapse();
    expect(sidebar.isCollapsed()).to.eventually.be.false;
  });

  it('should be expanded next time the page is opened', function () {
    sidebar.toggleCollapse();
    expect(sidebar.isCollapsed()).to.eventually.be.false;
    page.open();
    expect(sidebar.isCollapsed()).to.eventually.be.false;
  });

  it('should be collapsed after toggle collapse of expanded sidebar', function () {
    sidebar.toggleCollapse();
    expect(sidebar.isCollapsed()).to.eventually.be.false;
    sidebar.toggleCollapse();
    expect(sidebar.isCollapsed()).to.eventually.be.true;
  });

  it('sidebar should display the object browser', function () {
    sidebar.toggleCollapse();
    var objectBrowser = new ObjectBrowser($('.object-browser'));
    objectBrowser.getNode('Root');
  });
});
