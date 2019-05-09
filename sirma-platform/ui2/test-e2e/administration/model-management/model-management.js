'use strict';

let Dialog = require('../../components/dialog/dialog');
let Tabs = require('../../components/tabs/tabs').Tabs;
let PageObject = require('../../page-object').PageObject;
let SandboxPage = require('../../page-object').SandboxPage;
let Button = require('../../form-builder/form-control').Button;
let FormControl = require('../../form-builder/form-control').FormControl;
let InputField = require('../../form-builder/form-control').InputField;
let CheckboxField = require('../../form-builder/form-control').CheckboxField;
let SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;
let MultySelectMenu = require('../../form-builder/form-control').MultySelectMenu;
let Sourcearea = require('../../components/sourcearea/sourcearea').Sourcearea;
let ObjectBrowser = require('../../components/object-browser/object-browser').ObjectBrowser;
let Notification = require('../../components/notification').Notification;
let ModelHeadersSection = require('./sections/headers/model-headers-section').ModelHeadersSection;
let ModelActionsSection = require('./sections/actions/model-actions-section').ModelActionsSection;
let hasClass = require('../../test-utils').hasClass;

const SANDBOX_URL = '/sandbox/administration/model-management/';

function convertToBase64(model) {
  return Buffer.from(model).toString('base64');
}

function getModelId(id) {
  return id.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, '\\$&');
}

/**
 * PO for interacting with the model management sandbox page.
 *
 * @author Svetlozar Iliev
 */
class ModelManagementSandbox extends SandboxPage {

  open(userLanguage = 'en', systemLang = 'en', model = '', saveStatus = 'SUCCESS_SAVE', publishStatus = 'SUCCESS_PUBLISH') {
    let hash = `?userLang=${userLanguage}&systemLang=${systemLang}&saveStatus=${saveStatus}&publishStatus=${publishStatus}&model=${convertToBase64(model)}`;
    super.open(SANDBOX_URL, hash);
    return this;
  }

  getCurrentUrl() {
    return browser.getCurrentUrl();
  }

  getModelTree() {
    return new ModelTree($(ModelTree.COMPONENT_SELECTOR));
  }

  getModelData() {
    return new ModelData($(ModelData.COMPONENT_SELECTOR));
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

  toggleNode(node) {
    return this.getNode(node).expand();
  }

  isNodeModified(node) {
    return hasClass(this.getNode(node).getAnchor(), 'modified-node');
  }

  isNodeSelected(node) {
    return this.getNode(node).isHighlighted();
  }
}

ModelTree.COMPONENT_SELECTOR = '.model-tree';

/**
 * PO for interacting with the model sections and data.
 *
 * @author Svetlozar Iliev
 */
class ModelData extends Tabs {

  constructor(element) {
    super(element);
  }

  getGeneralSection() {
    this.getTab(ModelData.GENERAL_SECTION).click();
    return new ModelGeneralSection($(ModelGeneralSection.COMPONENT_SELECTOR));
  }

  isGeneralSectionModified() {
    return this.isSectionModified(ModelData.GENERAL_SECTION);
  }

  getFieldsSection() {
    this.getTab(ModelData.FIELDS_SECTION).click();
    return new ModelFieldsSection($(ModelFieldsSection.COMPONENT_SELECTOR));
  }

  isFieldsSectionModified() {
    return this.isSectionModified(ModelData.FIELDS_SECTION);
  }

  getActionsSection() {
    this.getTab(ModelData.ACTIONS_SECTION).click();
    let actionSectionElement = $(ModelActionsSection.COMPONENT_SELECTOR);
    return new ModelActionsSection(actionSectionElement, new ModelAttributeExtractor(actionSectionElement));
  }

  isActionsSectionModified() {
    return this.isSectionModified(ModelData.ACTIONS_SECTION);
  }

  getHeadersSection() {
    this.getTab(ModelData.HEADERS_SECTION).click();
    return new ModelHeadersSection($(ModelHeadersSection.COMPONENT_SELECTOR));
  }

  isHeadersSectionModified() {
    return this.isSectionModified(ModelData.HEADERS_SECTION);
  }

  getDeployPanel() {
    this.getDeployControl().click();
    return new ModelDeployDialog($(Dialog.COMPONENT_SELECTOR));
  }

  getDeployControl() {
    return new ModelControl(this.element.$('.model-deploy'));
  }

  isModelSelectMessageDisplayed() {
    browser.wait(EC.presenceOf(this.element.$('.select-message')), DEFAULT_TIMEOUT);
    return true;
  }

  isModelLoadingMessageDisplayed() {
    browser.wait(EC.presenceOf(this.element.$('.loading-message')), DEFAULT_TIMEOUT);
    return true;
  }

  isSectionModified(tabIndex) {
    let section = this.getTab(tabIndex);
    return section.getClasses().then(classes => classes.indexOf('modified-section') !== -1);
  }

  getErrorsDialog() {
    let dialog = new Dialog($('.modal-dialog'));
    dialog.waitUntilOpened();
    return dialog;
  }

  getSaveFailedDialog() {
    return new ModelSaveDialog($('.modal-dialog'));
  }
}

ModelData.GENERAL_SECTION = 0;
ModelData.FIELDS_SECTION = 1;
ModelData.ACTIONS_SECTION = 2;
ModelData.HEADERS_SECTION = 3;
ModelData.COMPONENT_SELECTOR = '.model-data';

/**
 * PO for interacting with the model general section.
 *
 * @author Svetlozar Iliev
 */
class ModelGeneralSection extends PageObject {

  constructor(element) {
    super(element);
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
    return this.getModelData(model).$('.model-header');
  }

  getModelData(model) {
    let data = this.element.$(`.model-${model}-attributes`);
    browser.wait(EC.visibilityOf(data), DEFAULT_TIMEOUT);
    return data;
  }

  getModelAttribute(id, model) {
    return new ModelAttributeExtractor(this.getModelData(model)).getModelAttribute(id);
  }

  getModelControls() {
    return new ModelControls(this.element.$(ModelControls.COMPONENT_SELECTOR));
  }
}

ModelGeneralSection.COMPONENT_SELECTOR = '.section.model-general';

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

  createField() {
    this.fieldCreateControl.click();
    return new ModelCreateFieldDialog($(Dialog.COMPONENT_SELECTOR));
  }

  createProperty() {
    this.propertyCreateControl.click();
    return new ModelCreatePropertyDialog($(Dialog.COMPONENT_SELECTOR));
  }

  getProperty(id) {
    return new ModelProperty(this.element.$(`#${getModelId(id)}`).$(ModelProperty.COMPONENT_SELECTOR));
  }

  isPropertyDisplayed(id) {
    return this.element.$(`#${getModelId(id)}`).$(ModelProperty.COMPONENT_SELECTOR).isPresent();
  }

  getRegion(id) {
    return new ModelRegion(this.element.$(`#${getModelId(id)}`).$(ModelRegion.COMPONENT_SELECTOR));
  }

  getRegions() {
    return this.element.$$(ModelRegion.COMPONENT_SELECTOR).then(regionElements => {
      return regionElements.map(regionElement => new ModelRegion(regionElement));
    });
  }

  isRegionDisplayed(id) {
    return this.element.$(`#${getModelId(id)}`).$(ModelRegion.COMPONENT_SELECTOR).isPresent();
  }

  getField(id) {
    return new ModelField(this.element.$(`#${getModelId(id)}`).$(ModelField.COMPONENT_SELECTOR));
  }

  getFields() {
    return this.element.$$(ModelField.COMPONENT_SELECTOR).then(fieldElements => {
      return fieldElements.map(fieldElement => new ModelField(fieldElement));
    });
  }

  isFieldDisplayed(id) {
    return this.element.$(`#${getModelId(id)}`).$(ModelField.COMPONENT_SELECTOR).isPresent();
  }

  getModelControls() {
    return new ModelControls(this.element.$(ModelControls.COMPONENT_SELECTOR));
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

  isNoResultsMessagePresent() {
    return this.element.$('.filter-message').isPresent();
  }

  hasSelectedModel() {
    return this.element.$(ModelDetails.COMPONENT_SELECTOR).isPresent();
  }

  getModelDetails() {
    return new ModelDetails(this.element.$(ModelDetails.COMPONENT_SELECTOR));
  }

  getModelControlsSection() {
    return new ModelControlsSection(this.element.$(ModelControlsSection.COMPONENT_SELECTOR));
  }

  isModelControlsSectionVisible() {
    return this.element.$(ModelControlsSection.COMPONENT_SELECTOR).isPresent();
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
    return this.filterSection.$('.filter-field');
  }

  get fieldCreateControl() {
    return this.controlSection.$('.create-field');
  }

  get propertyCreateControl() {
    return this.controlSection.$('.create-property');
  }

  get controlSection() {
    return this.element.$('.control-section');
  }

  get filterSection() {
    return this.element.$('.filter-section');
  }

  get displaySection() {
    return this.element.$('.display-section');
  }
}

ModelFieldsSection.COMPONENT_SELECTOR = '.section.model-fields';

/**
 * PO representing common model controls.
 *
 * @author Svetlozar Iliev
 */
class ModelControls extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getModelSave() {
    return new ModelControl(this.modelActions.$('.model-save'));
  }

  getModelCancel() {
    return new ModelControl(this.modelActions.$('.model-cancel'));
  }

  get modelActions() {
    return this.element.$('.model-actions > .controls');
  }
}

ModelControls.COMPONENT_SELECTOR = '.model-controls';

/**
 * PO representing a single model control.
 *
 * @author Svetlozar Iliev
 */
class ModelControl extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  isDisabled() {
    return this.control.isDisabled();
  }

  isEnabled() {
    return this.control.isEnabled();
  }

  isLoading() {
    return this.button.$('.fa-spinner').isDisplayed();
  }

  click() {
    this.control.click();
  }

  getLabel() {
    this.button.getText();
  }

  getNotification() {
    return new Notification().waitUntilOpened();
  }

  get control() {
    return new Button(this.button);
  }

  get button() {
    return this.element.$('.btn');
  }
}

/**
 * PO representing a model container.
 *
 * @author Svetlozar Iliev
 */
class ModelContainer extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLabel() {
    return this.containerHeader.$('.container-name').getText();
  }

  isCollapsed() {
    return this.containerBody.isDisplayed().then(result => !result);
  }

  showAttributes() {
    browser.executeScript('$(arguments[0]).click();', this.containerHeader.$('.model-select button').getWebElement());
    return this;
  }

  get containerHeader() {
    return this.element.$('.container-header');
  }

  get containerBody() {
    return this.element.$('.container-body');
  }

  get containerButtons() {
    return this.element.$('.container-buttons');
  }

  get attributesButton() {
    return new ModelControl(this.containerButtons.$('.attributes'));
  }
}

ModelContainer.COMPONENT_SELECTOR = '.model-container';

/**
 * PO representing a model property.
 *
 * @author Svetlozar Iliev
 */
class ModelProperty extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getLabel() {
    return this.modelLabel.getText();
  }

  getParent() {
    return this.parentLabel.getText();
  }

  getTooltip() {
    browser.actions().mouseMove(this.modelTooltip).perform();
    return this.modelTooltip.getAttribute('data-original-title');
  }

  showAttributes() {
    browser.executeScript('$(arguments[0]).click();', this.element.$('.model-select button').getWebElement());
  }

  hasTooltip() {
    return this.modelTooltip.isPresent();
  }

  get parentLabel() {
    return this.modelLabelControl.$('.property-parent');
  }

  get modelLabel() {
    return this.modelLabelControl.$('.property-label');
  }

  get modelTooltip() {
    return this.modelDataControl.$('.property-hint > i');
  }

  get modelLabelControl() {
    return this.element.$('.control-label');
  }

  get modelDataControl() {
    return this.element.$('.control-data');
  }

  get propertyButtons() {
    return this.element.$('.property-buttons');
  }

  get attributesButton() {
    return new ModelControl(this.propertyButtons.$('.attributes'));
  }
}

ModelProperty.COMPONENT_SELECTOR = '.model-property';

/**
 * PO representing a model region.
 *
 * @author Svetlozar Iliev
 */
class ModelRegion extends ModelContainer {

  constructor(element) {
    super(element);
  }

  getFields() {
    return this.containerBody.$$(ModelField.COMPONENT_SELECTOR).then(fields => {
      // filter out all fields which are hidden or not displayed and get only the visible ones
      let filtered = fields.map(field => field.isDisplayed().then(v => v && new ModelField(field)));
      return Promise.all(filtered).then(results => results.filter(result => !!result));
    });
  }

  hasFields() {
    return this.getFields().then(fields => !!fields.length);
  }
}

ModelRegion.COMPONENT_SELECTOR = ModelContainer.COMPONENT_SELECTOR;

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

  getTooltip() {
    browser.actions().mouseMove(this.modelLabelControl).perform();
    return this.modelLabelControl.$('.field-label').getAttribute('data-original-title');
  }

  getViewControl() {
    return this.modelViewControls;
  }

  getEditControl() {
    return this.modelEditControls;
  }

  hasTooltip() {
    return this.getTooltip().then(hintText => {
      return !!hintText && hintText.length > 0;
    });
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
    return this.modelLabelControl.$('.mandatory-mark').isPresent();
  }

  isDirty() {
    return hasClass(this.element, 'dirty-model');
  }

  showAttributes() {
    browser.executeScript('$(arguments[0]).click();', this.element.$('.model-select button').getWebElement());
    return this;
  }

  isHighlighted() {
    return hasClass(this.element, 'highlighted-model');
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

  get modelTooltip() {
    return this.element.$('.field-hint > i');
  }

  get modelButtonControls() {
    return this.element.$('.control-buttons');
  }

  get attributesButton() {
    return new ModelControl(this.modelButtonControls.$('.attributes'));
  }
}

ModelField.COMPONENT_SELECTOR = '.model-field';

/**
 * PO representing a model list.
 *
 * @author Svetlozar Iliev
 */
class ModelList extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getItem(index) {
    return this.getItems()[index];
  }

  getItems() {
    return this.element.$$(ModelItem.COMPONENT_SELECTOR).then(items => {
      // transform the incoming items to a proper list item
      return items.map(item => new ModelItem(item));
    });
  }

  selectAll() {
    return this.element.$('.select-all').click();
  }

  deselectAll() {
    return this.element.$('.deselect-all').click();
  }

  isSingleSelection() {
    return this.isMultiSelection().then(result => !result);
  }

  isMultiSelection() {
    return this.element.$('.list-actions').isPresent();
  }
}

ModelList.COMPONENT_SELECTOR = '.model-list';

/**
 * PO representing the model list in deploy panel
 *
 * @author Radoslav Dimitrov
 */
class ModelValidationList extends ModelList {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getItems() {
    return this.element.$$(ModelItem.COMPONENT_SELECTOR).then(items => {
      // transform the incoming items to a proper list item
      return items.map(item => new ModelValidationItem(item));
    });
  }
}

/**
 * PO representing a model list item.
 *
 * @author Svetlozar Iliev
 */
class ModelItem extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getIdentifier() {
    return this.element.$('.model-data .model-id').getText();
  }

  getName() {
    return this.element.$('.model-data .model-name').getText();
  }

  getControl() {
    return new CheckboxField(this.element.$('label'));
  }

  select() {
    return this.getControl().toggleCheckbox();
  }

  isSelected() {
    return this.getControl().isChecked().then(isChecked => !!isChecked);
  }
}

ModelItem.COMPONENT_SELECTOR = '.model-item';

/**
 * PO representing a model list item for deploy.
 *
 * @author Radoslav Dimitrov
 */
class ModelValidationItem extends ModelItem {

  constructor(element) {
    super(element);
  }

  getValidationLog() {
    return new ModelValidationLog(this.element.$(ModelValidationLog.COMPONENT_SELECTOR));
  }
}

/**
 * PO representing model validation messages.
 *
 * @author Radoslav Dimitrov
 */
class ModelValidationLog extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  hasErrors() {
    return this.getErrors().then(errors => !!errors.length);
  }

  getErrors() {
    return this.modelErrors.then(errors => {
      return errors.map(error => error.getText());
    });
  }

  hasWarnings() {
    return this.getWarnings().then(warnings => {
      return warnings.length > 0;
    });
  }

  getWarnings() {
    return this.modelWarnings.then(warnings => {
      return warnings.map(warning => warning.getText());
    });
  }

  get modelErrors() {
    return this.element.$$('.model-error');
  }

  get modelWarnings() {
    return this.element.$$('.model-warning');
  }
}

ModelValidationLog.COMPONENT_SELECTOR = '.model-validation-log';

/**
 * PO representing model details (list of attributes).
 *
 * @author Mihail Radkov
 */
class ModelDetails extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getGeneralAttributesPanel() {
    return new GeneralModelAttributesPanel(this.element.$(GeneralModelAttributesPanel.COMPONENT_SELECTOR));
  }

  hasGeneralAttributes() {
    return this.element.$(GeneralModelAttributesPanel.COMPONENT_SELECTOR).isPresent();
  }

  getBehaviourAttributesPanel() {
    return new BehaviourModelAttributesPanel(this.element.$(BehaviourModelAttributesPanel.COMPONENT_SELECTOR));
  }
}

ModelDetails.COMPONENT_SELECTOR = '.model-details';

/**
 * Base PO for model details panel (field or region).
 *
 * @author Mihail Radkov
 */
class ModelBaseAttributesPanel extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getModelTitle() {
    return this.panelHeading.$('.model-title').getText();
  }

  toggleCollapse() {
    this.panelHeading.click();
  }

  isCollapsed() {
    return this.element.$('.panel-body').isDisplayed().then(state => !state);
  }

  getAttribute(id) {
    return new ModelAttributeExtractor(this.element).getModelAttribute(id);
  }

  get panelHeading() {
    return this.element.$('.panel-heading');
  }

  get panelBody() {
    return this.element.$('.panel-body');
  }
}

/**
 * PO for the semantic attributes of a model field
 *
 * @author Mihail Radkov
 */
class GeneralModelAttributesPanel extends ModelBaseAttributesPanel {

  constructor(element) {
    super(element);
  }

  editGeneralAttributes() {
    return this.editControl.click();
  }

  canEditAttributes() {
    return this.editControl.isPresent();
  }

  get editControl() {
    return this.panelHeading.$('.edit-property');
  }
}

GeneralModelAttributesPanel.COMPONENT_SELECTOR = '.model-general-attributes';

/**
 * PO for the behavioural (definition) attributes of a model field or region.
 *
 * @author Mihail Radkov
 */
class BehaviourModelAttributesPanel extends ModelBaseAttributesPanel {

  constructor(element) {
    super(element);
  }

  restoreAttributes() {
    this.restorationControl.click();
    return BehaviourModelAttributesPanel.getConfirmationDialog();
  }

  restoreAttribute(id) {
    this.getRestoreAttributeControl(id).click();
    return BehaviourModelAttributesPanel.getConfirmationDialog();
  }

  canRestoreAttributes() {
    return this.restorationControl.isPresent();
  }

  canRestoreAttribute(id) {
    return this.getRestoreAttributeControl(id).isPresent();
  }

  navigateToParent() {
    return this.navigationControl.click();
  }

  canNavigate() {
    return this.navigationControl.isPresent();
  }

  getRestoreAttributeControl(id) {
    return this.panelBody.$(`#${getModelId(id)}`).$('.restore-attribute');
  }

  get restorationControl() {
    return this.panelHeading.$('.restore-field');
  }

  get navigationControl() {
    return this.panelHeading.$('.parent-field');
  }

  static getConfirmationDialog() {
    let dialog = new Dialog($(Dialog.COMPONENT_SELECTOR));
    dialog.waitUntilOpened();
    return dialog;
  }
}

BehaviourModelAttributesPanel.COMPONENT_SELECTOR = '.model-attributes';

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
    return this.modelLabelControl.getText();
  }

  hasTooltip() {
    return this.getTooltip().then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  getTooltip() {
    browser.actions().mouseMove(this.modelLabelControl).perform();
    return this.modelLabelControl.$('.control-description').getAttribute('data-original-title');
  }

  isInvalid() {
    return hasClass(this.element, 'has-error');
  }

  isDirty() {
    return hasClass(this.element, 'dirty-model');
  }

  getValidationMessages() {
    return new ModelAttributeValidationMessages(this.element.$(ModelAttributeValidationMessages.COMPONENT_SELECTOR));
  }

  get modelLabelControl() {
    return this.element.$('.control-label');
  }
}

ModelAttribute.COMPONENT_SELECTOR = '.model-attribute';

/**
 * Wrapper page object for validation error messages to a model attribute.
 *
 * @author Mihail Radkov
 */
class ModelAttributeValidationMessages extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  hasValidationRuleError() {
    return this.element.$$('.validation-rule-error').isPresent();
  }

  getMessages() {
    return this.element.$$('.validation-rule-error').getText();
  }

}

ModelAttributeValidationMessages.COMPONENT_SELECTOR = '.model-validation-messages';

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
 * PO representing a number type model attribute.
 *
 * @author svelikov
 */
class ModelNumberAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new InputField(this.element.$(ModelNumberAttribute.COMPONENT_SELECTOR));
  }
}

ModelNumberAttribute.COMPONENT_SELECTOR = '.model-number-attribute';

/**
 * PO representing a source type model attribute.
 *
 * @author svelikov
 */
class ModelSourceAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new Sourcearea(this.element.$(ModelSourceAttribute.COMPONENT_SELECTOR));
  }
}

ModelSourceAttribute.COMPONENT_SELECTOR = '.model-source-attribute';

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
 * PO representing a select type model attribute.
 *
 * @author Stela Djulgerova
 */
class ModelOptionAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelOptionAttribute.COMPONENT_SELECTOR));
  }
}

ModelOptionAttribute.COMPONENT_SELECTOR = '.model-option-attribute';

/**
 * PO representing a domain type attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelDomainAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelDomainAttribute.COMPONENT_SELECTOR));
  }
}

ModelDomainAttribute.COMPONENT_SELECTOR = '.model-domain-attribute';

/**
 * PO representing a range type attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelRangeAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelRangeAttribute.COMPONENT_SELECTOR));
  }
}

ModelRangeAttribute.COMPONENT_SELECTOR = '.model-range-attribute';

/**
 * PO representing a code list select model attribute.
 *
 * @author Stela Djulgerova
 */
class ModelCodeListAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelCodeListAttribute.COMPONENT_SELECTOR));
  }
}

ModelCodeListAttribute.COMPONENT_SELECTOR = '.model-codelist-attribute';

/**
 * PO representing a code value select model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelCodeValueAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelCodeValueAttribute.COMPONENT_SELECTOR));
  }
}

ModelCodeValueAttribute.COMPONENT_SELECTOR = '.model-codevalue-attribute';

/**
 * PO representing a value model attribute.
 *
 * @author svelikov
 */
class ModelValueAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return this.element.$(ModelValueAttribute.COMPONENT_SELECTOR).getAttribute('value-type').then((type) => {
      // extract the field from an attribute type PO based on the current element selector
      return ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS[type](this.element).getField();
    });
  }
}

ModelValueAttribute.COMPONENT_SELECTOR = '.model-value-attribute';

/**
 * PO representing a field type select model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelFieldTypeAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelFieldTypeAttribute.COMPONENT_SELECTOR));
  }
}

ModelFieldTypeAttribute.COMPONENT_SELECTOR = '.model-field-type-attribute';

/**
 * PO representing a property type select model attribute.
 *
 * @author Svetlozar Iliev
 */
class ModelPropertyTypeAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelPropertyTypeAttribute.COMPONENT_SELECTOR));
  }
}

ModelPropertyTypeAttribute.COMPONENT_SELECTOR = '.model-property-type-attribute';

/**
 * PO representing a data type model attribute.
 *
 * @author Stela Djulgerova
 */
class ModelConstraintAttribute extends ModelAttribute {

  constructor(element) {
    super(element);
  }

  getField() {
    return new SingleSelectMenu(this.element.$(ModelConstraintAttribute.COMPONENT_SELECTOR));
  }

  getAlphaNumericRestrictionField() {
    return new InputField(this.element.$('.alpha-numeric-length-restriction-field'));
  }

  getFloatingPointLengthRestrictionField() {
    return new InputField(this.element.$('.floating-point-length-restriction-field'));
  }

  getAfterFloatingPointRestrictionField() {
    return new InputField(this.element.$('.after-floating-point-restriction-field'));
  }

  getTypeField() {
    return new FormControl(this.element.$('.type-abbreviation'));
  }
}

ModelConstraintAttribute.COMPONENT_SELECTOR = '.model-constraint-attribute';

/**
 * PO for a dialog listing a collection of values
 *
 * @author Svetlozar Iliev
 */
class ModelValuesDialog extends Dialog {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelValuesDialog.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getLabel(index) {
    return this.element.$$('.model-value .control-label').get(index).getText();
  }

  getField(index) {
    return new InputField(this.element.$$('.model-value .control-value').get(index));
  }

  isFieldInvalid(index) {
    return hasClass(this.element.$$('.model-value').get(index), 'has-error');
  }
}

ModelValuesDialog.COMPONENT_SELECTOR = '.model-values-view';

/**
 * PO representing deployment dialog.
 *
 * @author Svetlozar Iliev
 */
class ModelDeployDialog extends Dialog {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelList.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getModels() {
    return new ModelValidationList(this.element.$(ModelList.COMPONENT_SELECTOR));
  }
}

/**
 * PO representing error dialog on save.
 *
 * @author Radoslav Dimitrov
 */
class ModelSaveDialog extends Dialog {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelList.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getItems() {
    return this.element.$$(ModelItem.COMPONENT_SELECTOR).then(items => {
      // transform the incoming items to a proper list item
      return items.map(item => new ModelValidationItem(item));
    });
  }
}

/**
 * PO representing the create dialog for fields section
 *
 * @author Svetlozar Iliev
 */
class ModelCreateFieldDialog extends Dialog {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelCreateFieldDialog.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getAttribute(id) {
    return new ModelAttributeExtractor(this.element).getModelAttribute(id);
  }

  getModelControls() {
    return new ModelControls(this.element.$(ModelControls.COMPONENT_SELECTOR));
  }

  getPropertySelect() {
    return new SingleSelectMenu(this.propertySelect.$('.control-data'));
  }

  isMissingPropertyType() {
    return this.missingType.isPresent();
  }

  get propertySelect() {
    return this.element.$('.select-property');
  }

  get missingType() {
    return this.element.$('.missing-type');
  }
}

ModelCreateFieldDialog.COMPONENT_SELECTOR = '.model-form';

/**
 * PO representing the create dialog for property creation
 *
 * @author Svetlozar Iliev
 */
class ModelCreatePropertyDialog extends Dialog {

  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    super.waitUntilOpened();
    browser.wait(EC.visibilityOf(this.element.$(ModelCreatePropertyDialog.COMPONENT_SELECTOR)), DEFAULT_TIMEOUT);
  }

  getPropertyAttribute(id) {
    return new ModelAttributeExtractor(this.propertySection).getModelAttribute(id);
  }

  getPropertyTitle() {
    return this.propertySection.$('.model-title').getText();
  }

  getFieldAttribute(id) {
    return new ModelAttributeExtractor(this.fieldSection).getModelAttribute(id);
  }

  getFieldTitle() {
    return this.fieldSection.$('.model-title').getText();
  }

  getModelControls() {
    return new ModelControls(this.element.$(ModelControls.COMPONENT_SELECTOR));
  }

  getDestinationModels() {
    return this.destinationModelsMessage.$('.models').getText();
  }

  isPropertySectionVisible() {
    return this.propertySection.isPresent();
  }

  isFieldSectionVisible() {
    return this.fieldSection.isPresent();
  }

  isDestinationMessageVisible() {
    return this.destinationModelsMessage.isPresent();
  }

  isDuplicateMessageVisible() {
    return this.duplicatePropertyMessage.isPresent();
  }

  get destinationModelsMessage() {
    return this.element.$('.destination-models-message');
  }

  get duplicatePropertyMessage() {
    return this.element.$('.duplicate-property-message');
  }

  get fieldSection() {
    return this.element.$('.field-section');
  }

  get propertySection() {
    return this.element.$('.property-section');
  }
}

ModelCreatePropertyDialog.COMPONENT_SELECTOR = '.model-create-property';

/**
 * Utility class for extracting model attributes
 *
 * @author Svetlozar Iliev
 */
class ModelAttributeExtractor {

  constructor(element) {
    this.element = element;
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getModelAttribute(id) {
    let escaped = getModelId(id);
    let extension = this.element.$(`#${escaped}`);
    let attribute = extension.$('.attribute-extension > div');
    return attribute.getAttribute('class').then(classes => {
      let type = classes.replace(/model-([a-z-]+)-attribute (.*)/g, '$1');
      return ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS[type](extension);
    });
  }
}

ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS = {};

ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['label'] = (element) => new ModelLabelAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['string'] = (element) => new ModelStringAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['number'] = (element) => new ModelNumberAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['boolean'] = (element) => new ModelBooleanAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['option'] = (element) => new ModelOptionAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['source'] = (element) => new ModelSourceAttribute(element);

ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['value'] = (element) => new ModelValueAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['codelist'] = (element) => new ModelCodeListAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['codevalue'] = (element) => new ModelCodeValueAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['field-type'] = (element) => new ModelFieldTypeAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['constraint'] = (element) => new ModelConstraintAttribute(element);

ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['range'] = (element) => new ModelRangeAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['domain'] = (element) => new ModelDomainAttribute(element);
ModelAttributeExtractor.ATTRIBUTE_EXTENSIONS['property-type'] = (element) => new ModelPropertyTypeAttribute(element);

/**
 * PO representing model controls section.
 *
 * @author svelikov
 */
class ModelControlsSection extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  get sectionTitle() {
    return this.panelHeading.getText();
  }

  get panelHeading() {
    return this.element.$('.model-field-controls-panel-heading');
  }

  getSectionControlLink(controlType) {
    let control = this.sectionControlLinks.$(`.${controlType}`);
    browser.wait(EC.visibilityOf(control), DEFAULT_TIMEOUT);
    return control;
  }

  getControlsCount() {
    return this.sectionControlLinks.$$('.add-control').count().then(count => count);
  }

  canAddControls() {
    return this.getControlsCount().then(count => count > 0);
  }

  addControl(controlType) {
    this.getSectionControlLink(controlType).click();
    return this.getControl(controlType);
  }

  isRemoveDisabled(controlType) {
    return this.getControl(controlType).isRemoveDisabled();
  }

  removeControl(controlType) {
    this.getControl(controlType).removeControl();
    return this;
  }

  get sectionControlLinks() {
    return this.element.$('.section-controls');
  }

  getControl(controlType) {
    let selector = `${ModelFieldControl.COMPONENT_SELECTOR}.${controlType}`;
    let controlPanel = this.element.$(selector);
    browser.wait(EC.visibilityOf(controlPanel), DEFAULT_TIMEOUT);
    return ModelControlsSection.CONTROL_EXTENSIONS[controlType](controlPanel);
  }

  hasVisibleControls() {
    return this.element.$$(ModelFieldControl.COMPONENT_SELECTOR).count().then(count => count > 0);
  }

  isControlPresent(controlType) {
    let controlPanel = this.element.$(`${ModelFieldControl.COMPONENT_SELECTOR}.${controlType}`);
    return controlPanel.isPresent();
  }

  get restorationControl() {
    return this.panelHeading.$('.restore-controls');
  }

  canRestoreControls() {
    return this.restorationControl.isPresent();
  }

  restoreControls() {
    this.restorationControl.click();
    return BehaviourModelAttributesPanel.getConfirmationDialog();
  }
}

ModelControlsSection.COMPONENT_SELECTOR = '.model-field-controls-panel';

ModelControlsSection.CONTROL_EXTENSIONS = {};
ModelControlsSection.CONTROL_EXTENSIONS.RICHTEXT = (element) => new RichtextControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.PICKER = (element) => new PickerControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.CONCEPT_PICKER = (element) => new ConceptPickerControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.DEFAULT_VALUE_PATTERN = (element) => new DefaultValuePatternControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.RELATED_FIELDS = (element) => new RelatedFieldsControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.BYTE_FORMAT = (element) => new ByteFormatControl(element);
ModelControlsSection.CONTROL_EXTENSIONS.OBJECT_TYPE_SELECT = (element) => new ObjectTypeSelectControl(element);

/**
 * Provides generic implementation for all model controls.
 *
 * @author svelikov
 */
class ModelFieldControl extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getControlTitleText() {
    return this.controlTitle.getText();
  }

  isRemoveDisabled() {
    return this.element.$$('.remove-control').count().then(count => count === 0);
  }

  removeControl() {
    let removeControlButton = this.element.$('.remove-control');
    browser.wait(EC.visibilityOf(removeControlButton), DEFAULT_TIMEOUT);
    removeControlButton.click();
  }

  isDirty() {
    return hasClass(this.element, 'dirty-model');
  }

  hasTooltip() {
    browser.actions().mouseMove(this.controlTitle).perform();
    return this.controlTitle.$('.header-label').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  get controlTitle() {
    return this.element.$('.model-header');
  }
}

ModelFieldControl.COMPONENT_SELECTOR = '.model-field-control-view';

/**
 * PO representing richtext control.
 *
 * @author svelikov
 */
class RichtextControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }
}

/**
 * PO representing byte format control.
 *
 * @author Stella D
 */
class ByteFormatControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }
}

/**
 * PO representing object type control.
 *
 * @author Stella D
 */
class ObjectTypeSelectControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }
}

/**
 * PO representing default_value_pattern control.
 *
 * @author svelikov
 */
class DefaultValuePatternControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }

  hasControlTooltip() {
    browser.actions().mouseMove(this.controlLabel).perform();
    return this.controlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  get templateField() {
    let templateField = this.element.$('.param-template .sourcearea');
    return new Sourcearea(templateField);
  }

  get controlLabel() {
    return this.element.$('.control-label');
  }
}

/**
 * PO representing picker control.
 *
 * @author svelikov
 */
class PickerControl extends ModelFieldControl {
  constructor(element) {
    super(element);
  }

  hasRestrictionsTooltip() {
    browser.actions().mouseMove(this.restrictionsControlLabel).perform();
    return this.restrictionsControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  hasRangeTooltip() {
    browser.actions().mouseMove(this.rangeControlLabel).perform();
    return this.rangeControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  get rangeField() {
    let rangeField = this.element.$('.param-range .range');
    return new InputField(rangeField);
  }

  get restrictionsField() {
    let restrictionsField = this.element.$('.param-restrictions .sourcearea');
    return new Sourcearea(restrictionsField);
  }

  get restrictionsControlLabel() {
    return this.element.$('.param-restrictions').$('.control-label');
  }

  get rangeControlLabel() {
    return this.element.$('.param-range').$('.control-label');
  }
}

/**
 * PO representing related_fields control.
 *
 * @author Stella D
 */
class RelatedFieldsControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }

  hasRerenderTooltip() {
    browser.actions().mouseMove(this.rerenderControlLabel).perform();
    return this.rerenderControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  hasFilterSourceTooltip() {
    browser.actions().mouseMove(this.filterSourceControlLabel).perform();
    return this.filterSourceControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  hasCustomTooltip() {
    browser.actions().mouseMove(this.customControlLabel).perform();
    return this.customControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  hasInclusiveTooltip() {
    browser.actions().mouseMove(this.inclusiveControlLabel).perform();
    return this.inclusiveControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  getFilterSourceField(multiple) {
    let filterSourceField = this.element.$('.param-filter-source .filter-source');
    if (multiple) {
      return new MultySelectMenu(filterSourceField);
    }
    return new SingleSelectMenu(filterSourceField);
  }

  get rerenderField() {
    let rerenderField = this.element.$('.param-rerender .rerender');
    return new SingleSelectMenu(rerenderField);
  }

  get customField() {
    let customField = this.element.$('.param-custom-filter .custom-filter');
    return new SingleSelectMenu(customField);
  }

  get inclusiveField() {
    let inclusiveField = this.element.$('.param-inclusive .inclusive');
    return new CheckboxField(inclusiveField);
  }

  get rerenderControlLabel() {
    return this.element.$('.param-rerender').$('.control-label');
  }

  get filterSourceControlLabel() {
    return this.element.$('.param-filter-source').$('.control-label');
  }

  get customControlLabel() {
    return this.element.$('.param-custom-filter').$('.control-label');
  }

  get inclusiveControlLabel() {
    return this.element.$('.param-inclusive').$('.control-label');
  }
}

/**
 * PO representing concept picker control.
 *
 * @author Stella D
 */
class ConceptPickerControl extends ModelFieldControl {

  constructor(element) {
    super(element);
  }

  hasSchemeTooltip() {
    browser.actions().mouseMove(this.schemeControlLabel).perform();
    return this.schemeControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  hasBroaderTooltip() {
    browser.actions().mouseMove(this.broaderControlLabel).perform();
    return this.broaderControlLabel.$('.control-description').getAttribute('data-original-title').then(hintText => {
      return !!hintText && hintText.length > 0;
    });
  }

  get schemeField() {
    let schemeField = this.element.$('.param-scheme .scheme .model-string-attribute');
    return new InputField(schemeField);
  }

  get broaderField() {
    let broaderField = this.element.$('.param-broader .broader .model-string-attribute');
    return new InputField(broaderField);
  }

  get schemeControlLabel() {
    return this.element.$('.param-scheme').$('.control-label');
  }

  get broaderControlLabel() {
    return this.element.$('.param-broader').$('.control-label');
  }
}

module.exports = {
  ModelManagementSandbox,
  ModelData,
  ModelTree,
  ModelAttributeExtractor
};