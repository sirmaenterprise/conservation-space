'use strict';

let SandboxPage = require('../../page-object.js').SandboxPage;

class SourceareaSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/components/sourcearea');
  }

  getSourceArea() {
    let sourcearea = $('.sourcearea');
    let codemirror = $('.CodeMirror');
    browser.wait(EC.and(EC.presenceOf(sourcearea), EC.presenceOf(codemirror)), DEFAULT_TIMEOUT, 'Sourcearea field should be present and initialized!');
    return new Sourcearea(sourcearea);
  }

  getSelectedValue() {
    return $('.selected-items');
  }

  getChangedValue() {
    return $('.changed-value').getText();
  }
}

class Sourcearea {

  constructor(element) {
    this.element = element;
  }

  /**
   * Extracts value using the codemirror component API directly as it's hard to get it other way.
   */
  getValue() {
    let webElement = this.element.getWebElement();
    // This is the recommended way to get value from the codemirror component
    return browser.executeScript('var editor = $(arguments[0]).find(\'.CodeMirror\')[0].CodeMirror; return editor.getValue();', webElement);
  }

  /**
   * Finds the codemirror component in current context and sets the new value.
   */
  setValue(value) {
    let webElement = this.element.getWebElement();
    // This is the recommended way to set value in the codemirror component
    browser.executeScript(`var editor = $(arguments[0]).find('.CodeMirror')[0].CodeMirror;editor.setValue('${value}');`, webElement);
  }
}

Sourcearea.SELECTOR = '.sourcearea';

module.exports.SourceareaSandboxPage = SourceareaSandboxPage;
module.exports.Sourcearea = Sourcearea;