'use strict';

let IdocPage = require('../../idoc-page').IdocPage;
let IdocPageToolbar = require('../idoc-page-toolbar');

describe('BPM action toolbar ', () => {

  let idocPage;

  beforeEach(() => {
    idocPage = new IdocPage('/sandbox/idoc/idoc-page');
  });

  it('should be visible in preview mode', () => {
    idocPage.open(false);
    idocPage.waitForPreviewMode();
    let toolbar = new IdocPageToolbar();
    let bpmSection = toolbar.getBPMToolbar();

    expect(bpmSection.isToolbarPresent()).to.eventually.be.true;
    expect(bpmSection.getButtons().isDisplayed()).to.eventually.be.true;
    expect(bpmSection.getButtons().isEnabled()).to.eventually.be.false;
  });

  it('should be hidden in edit mode', () => {
    idocPage.open(true);
    idocPage.waitForEditMode();
    let toolbar = new IdocPageToolbar();

    expect(toolbar.isBPMToolbarVisible()).to.eventually.be.false;
  });
});