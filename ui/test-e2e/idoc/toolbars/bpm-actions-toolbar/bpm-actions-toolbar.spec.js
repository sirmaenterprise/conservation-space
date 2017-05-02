"use strict";

let IdocPage = require('../../idoc-page');
let IdocPageToolbar = require('../idoc-page-toolbar');

describe('BPM action toolbar ', () => {
  let idocPage;

  beforeEach(() => {
    idocPage = new IdocPage();
  });

  it('should not display the transition buttons when the editor is in edit mode', () => {
    idocPage.open(false);
    idocPage.waitForPreviewMode();
    let toolbar = new IdocPageToolbar();
    let bpmSection = toolbar.getBPMToolbar();
    expect(bpmSection.getButtons().isDisplayed()).to.eventually.be.true;
    expect(bpmSection.getButtons().isEnabled()).to.eventually.be.false;
    idocPage.open(true);
    expect(bpmSection.isToolbarPresent()).to.eventually.be.false;
  });

});