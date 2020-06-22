'use strict';

let Collapsible = require('../../components/collapsible/collapsible.js').Collapsible;
let SandboxPage = require('../../page-object').SandboxPage;

let page = new SandboxPage();

describe('Region', () => {
  it('should show first two regions as expanded if collapsibleRegions is true', () => {
    page.open('/sandbox/form-builder/region', 'collapsibleRegions=true');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);

    let collapsible1 = new Collapsible('#region1field-wrapper .collapsible');
    expect(collapsible1.isCollapsible()).to.eventually.be.true;
    expect(collapsible1.isCollapsed()).to.eventually.be.false;
    collapsible1.toggleCollapse();
    expect(collapsible1.isCollapsed()).to.eventually.be.true;

    let collapsible2 = new Collapsible('#region1field-wrapper .collapsible');
    expect(collapsible2.isCollapsible()).to.eventually.be.true;
  });

  it('should show third region as collapsed if collapsibleRegions is true', () => {
    page.open('/sandbox/form-builder/region', 'collapsibleRegions=true');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);

    let collapsible3 = new Collapsible('#region3field-wrapper .collapsible');
    expect(collapsible3.isCollapsible()).to.eventually.be.true;
    expect(collapsible3.isCollapsed()).to.eventually.be.true;
    collapsible3.toggleCollapse();
    expect(collapsible3.isCollapsed()).to.eventually.be.false;
  });

  it('should show normal regions if collapsibleRegions is false', () => {
    page.open('/sandbox/form-builder/region', 'collapsibleRegions=false');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
    let collapsible1 = new Collapsible('#region1field-wrapper .collapsible');
    expect(collapsible1.isElementPresent()).to.eventually.be.false;

    let collapsible2 = new Collapsible('#region1field-wrapper .collapsible');
    expect(collapsible2.isElementPresent()).to.eventually.be.false;
  });

  it('should show/hide region names', () => {
    page.open('/sandbox/form-builder/region', 'collapsibleRegions=true');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);

    let firstRegion = element(by.id('region1field-wrapper'));
    expect(firstRegion.$('.panel-heading').isDisplayed()).to.eventually.be.true;

    let collapsible = new Collapsible('#region1field-wrapper .collapsible');
    collapsible.toggleCollapse();

    element(by.id('showRegionsNames')).click();
    expect(firstRegion.isElementPresent(by.css('.panel-heading'))).to.eventually.be.false;
    expect(element(by.id('singleCheckboxEdit1-wrapper')).isDisplayed()).to.eventually.be.true;
    expect(element(by.id('singleCheckboxEdit2-wrapper')).isDisplayed()).to.eventually.be.true;

    element(by.id('showRegionsNames')).click();
    expect(firstRegion.isElementPresent(by.css('.panel-heading'))).to.eventually.be.true;
    expect(element(by.id('singleCheckboxEdit1-wrapper')).isDisplayed()).to.eventually.be.true;
    expect(element(by.id('singleCheckboxEdit2-wrapper')).isDisplayed()).to.eventually.be.true;
  });
});