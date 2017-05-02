'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var Dialog = require('../../../components/dialog/dialog');

class ProcessSandboxPage {
  open() {
    browser.get('/sandbox/idoc/widget/process');
  }

  getWidget() {
    return new BusinessProcessDiagramWidget($('.bpmn-process'));
  }

}

class BusinessProcessDiagramWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
  }

  /**
   * Waits for message.
   *
   */
  isMessagePresent() {
      browser.wait(EC.visibilityOf(this.widgetElement.$('.message')), DEFAULT_TIMEOUT);
  }

  /**
   * A promise that will get the text of the displayed message in the widget.
   *
   * @return promise.
   */
  getMesageText() {
    return this.widgetElement.$('.message').getText();
  }

  /**
   *  Waits for button to be present.
   */
  isFullscreenButtonPresent() {
    browser.wait(EC.visibilityOf(this.widgetElement.$('.fullscreen')), DEFAULT_TIMEOUT);
  }

  /**
   * Waits for diagram to be present.
   */
  isDiagramPresent() {
     browser.wait(EC.visibilityOf('.viewport'), DEFAULT_TIMEOUT);
  }

  /**
   * Opens the widget in fullscreen mode, and waits for the dialog to be present before returning it.
   *
   * @return dialog window.
   */
  clickFullscreenButton() {
    this.widgetElement.$('.fullscreen').click();
    var dialog = new FullscreenDialog();
    dialog.waitUntilOpened();
    return dialog;
  }

}

class BusinessProcessDiagramConfigDialog extends WidgetConfigDialog {

  constructor() {
    super('business-process-diagram-widget');
  }

  /**
   * Gets the widget object selector.
   *
   * @return  new ObjectSelector.
   */
  getObjectSelector() {
    return new ObjectSelector();
  }

}

class FullscreenDialog extends Dialog {

  constructor() {
    super($('.modal-dialog'));
  }

  /**
   * A promise that will resolve to whether the element is present on the page.
   *
   * @return promise.
   */
  isDiagramPresent() {
    return $('.viewport').isPresent();
  }
}

BusinessProcessDiagramWidget.WIDGET_NAME = 'business-process-diagram-widget';

module.exports.ProcessSandboxPage = ProcessSandboxPage;
module.exports.BusinessProcessDiagramWidget = BusinessProcessDiagramWidget;
module.exports.BusinessProcessDiagramConfigDialog = BusinessProcessDiagramConfigDialog;
module.exports.FullscreenDialog = FullscreenDialog;