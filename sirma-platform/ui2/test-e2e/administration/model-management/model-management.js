'use strict';

let Dialog = require('../../components/dialog/dialog');
let Tabs = require('../../components/tabs/tabs').Tabs;
let PageObject = require('../../page-object').PageObject;
let SandboxPage = require('../../page-object').SandboxPage;
let InputField = require('../../form-builder/form-control').InputField;
let CheckboxField = require('../../form-builder/form-control').CheckboxField;
let ObjectBrowser = require('../../components/object-browser/object-browser').ObjectBrowser;

const SANDBOX_URL = '/sandbox/administration/model-management/';

function convertToBase64(model) {
  return Buffer.from(model).toString('base64');
}

/**
 * PO for interacting with the model management sandbox page.
 *
 * @author Svetlozar Iliev
 */
class ModelManagementSandbox extends SandboxPage {

  open(userLanguage = 'en', systemLang = 'en', model = '') {
    let hash = `?userLang=${userLanguage}&systemLang=${systemLang}&model=${convertToBase64(model)}`;
    super.open(SANDBOX_URL, hash);
    return this;
  }

  getCurrentUrl() {
    return browser.getCurrentUrl();
  }

  getModelTree() {
    return new ModelTree($(ModelTree.COMPONENT_SELECTOR));
  }

  getModelSection() {
    return new ModelSection($(ModelSection.COMPONENT_SELECTOR));
  }

  isModelProvided(model) {
    return this.getCurrentUrl().then(url => unescape(url).includes(convertToBase64(model)));
  }
}

/**
 * PO for interacting with the model tree functionality.
 *
 * @author Svetlozar Iliev
 */
class ModelTree extends ObjectBrowser {

  constructor(element) {
    super(element);
  }
}

ModelTree.COMPONENT_SELECTOR = '.model-tree';

/**
 * PO for interacting with the model sections and data.
 *
 * @author Svetlozar Iliev
 */
class ModelSection extends Tabs {

  constructor(element) {
    super(element);
  }

  getGeneralSection() {
    this.getTab(0).click();
    return new ModelGeneralSection($(ModelGeneralSection.COMPONENT_SELECTOR));
  }

  getFieldsSection() {
    this.getTab(1).click();
    return new ModelFieldsSection($(ModelFieldsSection.COMPONENT_SELECTOR));
  }

  isModelSelectMessageDisplayed() {
    browser.wait(EC.presenceOf(this.element.$('.select-message')), DEFAULT_TIMEOUT);
    return true;
  }

  isModelLoadingMessageDisplayed() {
    browser.wait(EC.presenceOf(this.element.$('.loading-message')), DEFAULT_TIMEOUT);
    return true;
  }
}

ModelSection.COMPONENT_SELECTOR = '.model-data';

/**
 * PO for interacting with the model general section.
 *
 * @author Svetlozar Iliev
 */
class ModelGeneralSection extends PageObject {

  constructor(element) {
    super(element);
    this.attributeExtensions = {};
    this.attributeExtensions['label'] = (element) => new ModelLabelAttribute(element);
    this.attributeExtensions['string'] = (element) => new ModelStringAttribute(element);
    this.attributeExtensions['boolean'] = (element) => new ModelBooleanAttribute(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getClassName() {
    let header = this.getModelHeader('semantic');
    return header.$('.model-header-name').getText();
  }

  getClassIdentifier() {
    let header = this.getModelHeader('semantic');
    return header.$('.model-header-id').getText();
  }

  getClassAttribute(id) {
    return this.getModelAttribute(id, 'semantic');
  }

  getDefinitionName() {
    let header = this.getModelHeader('definition');
    return header.$('.model-header-name').getText();
  }

  getDefinitionIdentifier() {
    let header = this.getModelHeader('definition');
    return header.$('.model-header-id').getText();
  }

  getDefinitionAttribute(id) {
    return this.getModelAttribute(id, 'definition');
  }

  getModelHeader(model) {
    return this.getModelSection(model).$('.model-header');
  }

  getModelSection(model) {
    return this.element.$(`.model-${model}-attributes`);
  }

  getModelAttribute(id, model) {
    let escaped = id.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, '\\$&');
    let extension = this.getModelSection(model).$(`#${escaped}`);
    let attribute = extension.$('.attribute-extension > div');
    return attribute.getAttribute('class').then(classes => {
      let type = classes.replace(/model-([a-z]+)-attribute (.*)/g, '$1');
      return this.attributeExtensions[type](extension);
    });
  }
}

ModelGeneralSection.COMPONENT_SELECTOR = '.model-general';

/**
 * PO for interacting with the model fields section.
 *
 * @author Svetlozar Iliev
 */
class ModelFieldsSection extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getRegion(id) {
    return new ModelRegion(this.element.$(`#${id}`).$(ModelRegion.COMPONENT_SELECTOR));
  }

  isRegionDisplayed(id) {
    return this.element.$(`#${id}`).$(ModelRegion.COMPONENT_SELECTOR).isDisplayed();
  }

  getField(id) {
    return new ModelField(this.element.$(`#${id}`).$(ModelField.COMPONENT_SELECTOR));
  }

  isFieldDisplayed(id) {
    return this.element.$(`#${id}`).$(ModelField.COMPONENT_SELECTOR).isDisplayed();
  }

  filterByKeyword(keyword) {
    browser.wait(this.filterKeywordControl.sendKeys(keyword), DEFAULT_TIMEOUT);
  }

  toggleHidden() {
    this.filterHiddenControl.toggleCheckbox();
  }

  toggleSystem() {
    this.filterSystemControl.toggleCheckbox();
  }

  toggleInherited() {
    this.filterInheritedControl.toggleCheckbox();
  }

  isInheritedFilterEnabled() {
    return this.filterInheritedControl.isChecked().then(isChecked => !!isChecked);
  }

  isSystemFilterEnabled() {
    return this.filterSystemControl.isChecked().then(isChecked => !!isChecked);
  }

  isHiddenFilterEnabled() {
    return this.filterHiddenControl.isChecked().then(isChecked => !!isChecked);
  }

  isKeywordFilterEmpty() {
    return this.filterKeywordControl.getAttribute('value').then(value => !value.length);
  }

  isIncorrectModelProvided() {
    return this.element.$('.selected-message').isDisplayed();
  }

  isNoResultsMessageDisplayed() {
    return this.element.$('.filter-message').isDisplayed();
  }

  get filterHiddenControl() {
    return new CheckboxField(this.displaySection.$('.filter-hidden'));
  }

  get filterSystemControl() {
    return new CheckboxField(this.displaySection.$('.filter-system'));
  }

  get filterInheritedControl() {
    return new CheckboxField(this.displaySection.$('.filter-inherited'));
  }

  get filterKeywordControl() {
    return this.controlSection.$('.filter-field');
  }

  get controlSection() {
    return this.element.$('.control-section');
  }

  get displaySection() {
    return this.element.$('.display-section');
  }
}

ModelFieldsSection.COMPONENT_SELECTOR = '.model-fields';

/**
 * PO representing a model region.
 *
 * @author Svetlozar Iliev
 */
class ModelRegion extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLabel() {
    return this.regionHeader.$('.region-name').getText();
  }

  getFields() {
    return this.regionBody.$$(ModelField.COMPONENT_SELECTOR).then(fields => {
      // filter out all fields which are hidden or not displayed and get only the visible ones
      let filtered = fields.map(field => field.isDisplayed().then(v => v && new ModelField(field)));
      return Promise.all(filtered).then(results => results.filter(result => !!result));
    });
  }

  isCollapsed() {
    return this.regionBody.isDisplayed().then(result => !result);
  }

  get regionHeader() {
    return this.element.$('.region-header');
  }

  get regionBody() {
    return this.element.$('.region-body');
  }
}

ModelRegion.COMPONENT_SELECTOR = '.model-region';

/**
 * PO representing a model field.
 *
 * @author Svetlozar Iliev
 */
class ModelField extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLabel() {
    return this.modelLabel.getText();
  }

  getViewControl() {
    return this.modelViewControls;
  }

  getEditControl() {
    return this.modelEditControls;
  }

  hasTooltip() {
    return this.element.$('.field-hint > .seip-hint > i').isPresent();
  }

  getParent() {
    return this.parentLabel.getText();
  }

  isInherited() {
    return this.parentLabel.isPresent();
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }

  isEditable() {
    return this.modelEditControls.isPresent();
  }

  isReadOnly() {
    return this.modelViewControls.isPresent();
  }

  isMandatory() {
    return this.modelLabelControl.$('.mandatory-mark').isDisplayed();
  }

  get parentLabel() {
    return this.modelLabelControl.$('.field-parent');
  }

  get modelLabel() {
    return this.modelLabelControl.$('.field-label');
  }

  get modelLabelControl() {
    return this.element.$('.control-label');
  }

  get modelEditControls() {
    return this.element.$('.control-edit');
  }

  get modelViewControls() {
    return this.element.$('.control-view');
  }

  get modelButtonControls() {
    return this.element.$$('.control-buttons');
  }
}

ModelField.COMPONENT_SELECTOR = '.model-field';

/**
 * PO representing an abstract model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelAttribute extends PageObject {

  constructor(element) {
    super(element);

    if (typeof this.getField !== 'function') {
      throw new Error('Must override getField function!');
    }
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLabel() {
    return this.element.$('.control-label').getText();
  }
}

ModelAttribute.COMPONENT_SELECTOR = '.model-attribute';

/**
 * PO representing a string type model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelStringAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new InputField(this.element.$(ModelStringAttribute.COMPONENT_SELECTOR));
  }
}

ModelStringAttribute.COMPONENT_SELECTOR = '.model-string-attribute';

/**
 * PO representing a label type model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelLabelAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new InputField(this.element.$(ModelLabelAttribute.COMPONENT_SELECTOR));
  }

  getValuesDialog() {
    this.openMultiLanguageValuesDialog();
    return new ModelValuesDialog($(Dialog.COMPONENT_SELECTOR));
  }

  openMultiLanguageValuesDialog() {
    this.element.$('.multi-language-btn').click();
  }
}

ModelLabelAttribute.COMPONENT_SELECTOR = '.model-label-attribute';

/**
 * PO representing a boolean type model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelBooleanAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new CheckboxField(this.element.$(ModelBooleanAttribute.COMPONENT_SELECTOR));
  }
}

ModelBooleanAttribute.COMPONENT_SELECTOR = '.model-boolean-attribute';

/**
 * PO for a dialog listing a collection of values
 *
 * @author Svetlozar Iliev
 */
class ModelValuesDialog extends Dialog {

  constructor(element) {
    super(element);
    super.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelValuesDialog.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getLabel(index) {
    return this.element.$$('.model-value .control-label').get(index).getText();
  }

  getField(index) {
    return new InputField(this.element.$$('.model-value .input-group').get(index));
  }
}

ModelValuesDialog.COMPONENT_SELECTOR = '.model-values-view';

module.exports = {
  ModelManagementSandbox,
  ModelGeneralSection,
  ModelSection,
  ModelTree,
};