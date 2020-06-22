let Tab = require('./tabs').Tab;
let Tabs = require('./tabs').Tabs;
let TabsSandbox = require('./tabs').TabsSandbox;

describe('Test for tabs component ', function () {

  let sandbox;

  beforeEach(() => {
    sandbox = new TabsSandbox().open();
  });

  it('should create horizontal tab menu', function () {
    sandbox.activateHorizontal();

    let horizontalMenu = sandbox.getHorizontalTabs();
    return expect(horizontalMenu.isPresent()).to.eventually.be.true;
  });

  it('should create 2 horizontal tabs', function () {
    sandbox.activateHorizontal();

    let tabs = sandbox.getHorizontalTabs();
    return expect(tabs.getTabs().count()).to.eventually.equal(2);
  });

  it('should create vertical tab menu', function () {
    sandbox.activateVertical();

    let verticalMenu = sandbox.getVerticalTabs();
    return expect(verticalMenu.isPresent()).to.eventually.be.true;
  });

  it('should create 2 vertical tabs', function () {
    sandbox.activateVertical();

    let tabs = sandbox.getVerticalTabs();
    return expect(tabs.getTabs().count()).to.eventually.equal(2);
  });

  it('should create sorted tab menu', function () {
    sandbox.activateSorted();

    let sortedMenu = sandbox.getSortedTabs();
    return expect(sortedMenu.isPresent()).to.eventually.be.true;
  });

  it('should create 6 sorted tabs', function () {
    sandbox.activateSorted();

    let tabs = sandbox.getSortedTabs();
    return expect(tabs.getTabs().count()).to.eventually.equal(6);
  });

  it('should set Tab2 in the horizontal menu as active', function () {
    sandbox.activateHorizontal();

    let tabs = sandbox.getHorizontalTabs();
    let tab2 = tabs.getTab(1);
    return expect(tab2.getClasses('class')).to.eventually.contain('active');
  });

  it('should set Tab1 in the vertical menu as active by default', function () {
    sandbox.activateVertical();

    let tabs = sandbox.getVerticalTabs();
    let tab1 = tabs.getTab(0);
    return expect(tab1.getClasses()).to.eventually.contain('active');
  });

  it('should set custom classes per tab', () => {
    sandbox.activateHorizontal();

    let tabs = sandbox.getHorizontalTabs();
    let tab1 = tabs.getTab(0);
    return expect(tab1.getClasses()).to.eventually.contain('custom-class');
  });

  it('should allow additional DOM element to be inserted after the title', () => {
    sandbox.activateHorizontal();

    let tabs = sandbox.getHorizontalTabs();
    let tab1 = tabs.getTab(1);
    let postfix = tab1.getPostfix();

    expect(postfix.isDisplayed()).to.eventually.be.true;
    expect(postfix.getText()).to.eventually.equal('Custom');
  });

  it('should have all 6 tabs labels sorted in alphabetical order', function () {
    sandbox.activateSorted();
    // the provided tab labels which should be sorted in the following order
    let labels = ['Curie', 'Einstein', 'Feynman', 'Newton', 'Oppenheimer', 'Wheeler'];

    let tabs = sandbox.getSortedTabs().getTabs();
    tabs.each((item, index) => {
      let tab = new Tab(item);
      expect(tab.getLabel()).to.eventually.eq(labels[index]);
    });
  });
});
