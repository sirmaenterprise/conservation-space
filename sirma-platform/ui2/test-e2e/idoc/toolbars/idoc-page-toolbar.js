"use strict";

var Toolbar = require('../../components/toolbars/toolbar');
var BpmToolbar = require('./bpm-actions-toolbar/bpm-toolbar');

class IdocPageToolbar extends Toolbar {

  constructor() {
    super('.toolbar');
  }

  getBPMToolbar() {
    let toolbar = new BpmToolbar();
    toolbar.waitUntilOpened();
    return toolbar;
  }
}

module.exports = IdocPageToolbar;