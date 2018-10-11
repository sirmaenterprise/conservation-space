'use strict';

let FormControl = require('../form-control.js').FormControl;

class RichText extends FormControl {

  constructor(control) {
    super(control);
    this.waitUntilLoaded();
  }

  /**
   * It's not mandatory that the field will be visible. If displayType=HIDDEN, then only the wrapper might be visible.
   */
  waitUntilLoaded() {
    this.contentArea = this.control.$('.cke_editable');
    browser.wait(EC.visibilityOf(this.control), DEFAULT_TIMEOUT, 'Richtext field editor should be visible!');
  }

  focusEditor() {
    this.getContentArea().click();
    return this;
  }

  blurEditor() {
    this.getContentArea().sendKeys(protractor.Key.TAB);
    this.isToolbarHidden();
  }

  isValid() {
    let toBeValid = () => {
      return this.control.getAttribute('class').then((attribute) => {
        return attribute.indexOf('has-error') === -1;
      });
    };
    browser.wait(toBeValid, DEFAULT_TIMEOUT, 'Richtext field should be valid!');
    return this;
  }

  isInvalid() {
    let toBeInvalid = () => {
      return this.control.getAttribute('class').then((attribute) => {
        return attribute.indexOf('has-error') !== -1;
      });
    };
    browser.wait(toBeInvalid, DEFAULT_TIMEOUT, 'Richtext field should be marked as invalid!');
    return this;
  }

  /**
   * Ensure that the field is visible and editable as well as the toolbar is present.
   * @returns {RichText}
   */
  isEditable() {
    this.isVisible();
    browser.wait(() => {
      return this.getContentArea().getAttribute('contenteditable').then((isEditable) => {
        return isEditable === 'true';
      });
    }, DEFAULT_TIMEOUT, 'Field should be editable!');
    this.getEditorToolbar().isVisible();
    return this;
  }

  isReadonly(text) {
    browser.wait(() => {
      return this.getContentArea().getAttribute('contenteditable').then((isEditable) => {
        return isEditable === 'false';
      });
    }, DEFAULT_TIMEOUT, 'Field should be readonly!');
    if (text) {
      browser.wait(EC.textToBePresentInElement(this.getAsText(), text), DEFAULT_TIMEOUT, 'Richtext field should be rendered in preview mode and text should be visible!');
    }
    return this;
  }

  isOptional() {
    browser.wait(EC.invisibilityOf(this.control.$('sup')), DEFAULT_TIMEOUT, 'Richtext field should be optional!');
    return this;
  }

  isMandatory() {
    browser.wait(EC.visibilityOf(this.control.$('sup')), DEFAULT_TIMEOUT, 'Richtext field should be marked as mandatory!');
    return this;
  }

  isHidden() {
    browser.wait(EC.invisibilityOf(this.contentArea), DEFAULT_TIMEOUT, 'Richtext field editor should be hidden!');
    return this;
  }

  /**
   * Ensure that field wrapper and the rich editor are present and visible.
   * @returns {RichText}
   */
  isVisible() {
    browser.wait(EC.visibilityOf(this.control), DEFAULT_TIMEOUT, 'Richtext field wrapper should be visible!');
    browser.wait(EC.visibilityOf(this.contentArea), DEFAULT_TIMEOUT, 'Richtext field editor should be visible!');
    return this;
  }

  getContentArea() {
    return this.contentArea;
  }

  getAsText() {
    return this.getContentArea().getText();
  }

  getAsHtml() {
    return this.getContentArea().getAttribute('innerHTML');
  }

  type(text) {
    this.getContentArea().sendKeys(text);
    return this;
  }

  newLine() {
    this.getContentArea().sendKeys(protractor.Key.ENTER);
    return this;
  }

  tab() {
    this.getContentArea().sendKeys(protractor.Key.TAB);
    return this;
  }

  clear() {
    this.getContentArea().clear();
    return this;
  }

  setContent(content) {
    // TODO: not tested - borrowed from idoc page
    // browser.executeScript('CKEDITOR.instances[arguments[0].id].setData(arguments[1])', this.getContentArea().getWebElement(), content);
  }

  isToolbarHidden() {
    new EditorToolbar(this.control).isHidden();
  }

  getEditorToolbar() {
    this.focusEditor();
    return new EditorToolbar(this.control);
  }
}

/**
 * PO which wraps the ckeditor toolbar. In order to work with the toolbar properly, the editor should be
 * focused first. This is due to the fact that it's configured for inline mode. In regard to the toolbar
 * this means it will be hidden until editor is focused.
 */
class EditorToolbar {

  constructor(control) {
    this.control = control;
  }

  /**
   * Toolbars for inline configured ckeditor are bound to document.body and not in the scope of the editor or the form
   * control. So in order to get the respective toolbar element, we first need to construct its unique identifier, then
   * we could find it in the DOM.
   * @returns The toolbar element.
   */
  getToolbarElement() {
    return this.control.$('textarea').getAttribute('id').then((id) => {
      let toolbarSelector = '#cke_' + id;
      return $(toolbarSelector);
    });
  }

  isVisible() {
    this.getToolbarElement().then((toolbar) => {
      browser.wait(EC.visibilityOf(toolbar), DEFAULT_TIMEOUT, 'Richtext field toolbar should be visible!');
    });
  }

  isHidden() {
    this.getToolbarElement().then((toolbar) => {
      browser.wait(EC.invisibilityOf(toolbar), DEFAULT_TIMEOUT, 'Richtext field toolbar should be hidden!');
    });
  }

  getActionBackgroundColor() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__bgcolor');
    });
  }

  isActionBackgroundColorVisible() {
    this.getActionBackgroundColor().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Background Color should be visible!');
    });
  }

  getActionFontColor() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__textcolor');
    });
  }

  isActionFontColorVisible() {
    this.getActionFontColor().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Font Color should be visible!');
    });
  }

  getActionFontSize() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_combo__fontsize');
    });
  }

  fontSize(size) {
    this.getActionFontSize().then((fontSizeMenu) => {
      return fontSizeMenu.click();
    }).then(() => {
      browser.switchTo().frame(0).then(() => {
        browser.driver.findElement(by.css(`[title='${size}']`)).click();
        browser.switchTo().defaultContent();
      });
    });
  }

  isActionFontSizeVisible() {
    this.getActionFontSize().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Font Size should be visible!');
    });
  }

  getActionBold() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__bold');
    });
  }

  isActionBoldVisible() {
    this.getActionBold().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Bold should be visible!');
    });
  }

  bold() {
    this.getActionBold().then((button) => {
      button.click();
    });
  }

  getActionItalic() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__italic');
    });
  }

  isActionItalicVisible() {
    this.getActionItalic().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Italic should be visible!');
    });
  }

  italic() {
    this.getActionItalic().then((button) => {
      button.click();
    });
  }

  getActionOrderedList() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__numberedlist');
    });
  }

  isActionOrderedListVisible() {
    this.getActionOrderedList().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Ordered list should be visible!');
    });
  }

  orderedList() {
    return this.getActionOrderedList().then((button) => {
      return button.click();
    });
  }

  getActionUnorderedList() {
    return this.getToolbarElement().then((toolbar) => {
      return toolbar.$('.cke_button__bulletedlist');
    });
  }

  isActionUnorderedListVisible() {
    this.getActionUnorderedList().then((button) => {
      browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Toolbar action Unordered list should be visible!');
    });
  }

  unorderedList() {
    return this.getActionUnorderedList().then((button) => {
      return button.click();
    });
  }

  fontColor(color) {
    this.getActionFontColor().then((fontMenu) => {
      return fontMenu.click();
    }).then(() => {
      browser.switchTo().frame(0).then(() => {
        browser.driver.findElement(by.css(`[title='${color}']`)).click();
        browser.switchTo().defaultContent();
      });
    });
  }

  backgroundColor(color) {
    this.getActionBackgroundColor().then((backgroundMenu) => {
      return backgroundMenu.click();
    }).then(() => {
      browser.switchTo().frame(0).then(() => {
        browser.driver.findElement(by.css(`[title='${color}']`)).click();
        browser.switchTo().defaultContent();
      });
    });
  }
}

module.exports.RichText = RichText;