var SandboxPage = require('../../page-object').SandboxPage;

const REPEATER = {
  TABS: 'tab in tabs.config.tabs'
};

function activateHorizontal() {
  $('.activate-horizontal').click();
}

function activateVertical() {
  $('.activate-vertical').click();
}

const HORIZONTAL_MENU_TARGETS = ['.tab1-target', '.tab2-target'];

var Tabs = require('./tabs');

var page = new SandboxPage();

describe('Test for tabs component ', function () {

  beforeEach(()=> {
    page.open('sandbox/components/tabs');
  });

  it('should create 2 horizontal tabs', function () {
    activateHorizontal();

    var tabs = new Tabs();
    return expect(tabs.getTabs().count()).to.eventually.equal(2);
  });

  it('should create 1 vertical tab menu', function () {
    activateVertical();

    var verticalMenu = $('.vertical');
    return expect(verticalMenu.isPresent()).to.eventually.be.true;
  });

  it('should create 2 vertical tabs', function () {
    activateVertical();

    var tabs = new Tabs();
    return expect(tabs.getTabs().count()).to.eventually.equal(2);
  });

  it('should set Tab2 in the horizontal menu as active', function () {
    activateHorizontal();

    var tabs = new Tabs();
    var tab2 = tabs.getTab(1);
    return expect(tab2.getClasses('class')).to.eventually.contain('active');
  });

  it('should set data-target attribute to tabs in the horizontal menu', function () {
    activateHorizontal();

    var tabs = new Tabs();
    var tabsAnchors = tabs.getTabs().all(by.css('a'));
    return expect(tabsAnchors.getAttribute('data-target')).to.eventually.eql(HORIZONTAL_MENU_TARGETS);
  });

  it('should set Tab1 in the vertical menu as active by default', function () {
    activateVertical();

    var tabs = new Tabs();
    var tab1 = tabs.getTab(0);
    return expect(tab1.getClasses()).to.eventually.contain('active');
  });

  it('should set custom classes per tab', ()=> {
    activateHorizontal();

    var tabs = new Tabs();
    var tab1 = tabs.getTab(0);
    return expect(tab1.getClasses()).to.eventually.contain('custom-class');
  });

  it('should allow additional DOM element to be inserted after the title', ()=>{
    activateHorizontal();

    var tabs = new Tabs();
    var tab1 = tabs.getTab(1);
    var postfix = tab1.getPostfix();

    expect(postfix.isDisplayed()).to.eventually.be.true;
    expect(postfix.getText()).to.eventually.equal('Custom');
  });

});
