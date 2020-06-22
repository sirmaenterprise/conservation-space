'use strict';

let Toolbar = require('../../components/toolbars/toolbar');
let BpmToolbar = require('./bpm-actions-toolbar/bpm-toolbar');

class IdocPageToolbar extends Toolbar {

  constructor() {
    super('.toolbar');
  }

  getBPMToolbar() {
    let toolbar = new BpmToolbar();
    toolbar.waitUntilOpened();
    return toolbar;
  }

  isBPMToolbarVisible() {
    let toolbar = new BpmToolbar();
    return toolbar.isToolbarPresent();
  }
}

module.exports = IdocPageToolbar;