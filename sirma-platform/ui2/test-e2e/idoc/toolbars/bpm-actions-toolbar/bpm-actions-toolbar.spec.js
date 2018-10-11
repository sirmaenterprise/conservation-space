"use strict";

let IdocPage = require('../../idoc-page').IdocPage;
let IdocPageToolbar = require('../idoc-page-toolbar');

describe('BPM action toolbar ', () => {
  let idocPage;

  beforeEach(() => {
    idocPage = new IdocPage('/sandbox/idoc/idoc-page');
  });

  it('should display buttons when save is canceled', () => {
    idocPage.open(false);
    idocPage.waitForPreviewMode();
    let toolbar = new IdocPageToolbar();
    let bpmSection = toolbar.getBPMToolbar();
    expect(bpmSection.getButtons().isDisplayed()).to.eventually.be.true;
    expect(bpmSection.getButtons().isEnabled()).to.eventually.be.false;
    idocPage.open(true);
    expect(bpmSection.isToolbarPresent()).to.eventually.be.false;
    idocPage.open(false);
    idocPage.waitForPreviewMode();
    expect(bpmSection.isToolbarPresent()).to.eventually.be.true;
    expect(bpmSection.getButtons().isDisplayed()).to.eventually.be.true;
  });

});