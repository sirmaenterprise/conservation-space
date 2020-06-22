'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let INSTANCE_HEADERS = require('../../../../instance-header/instance-header').INSTANCE_HEADERS;
let LanguageSelector = require('./model-headers-section.js').LanguageSelector;

describe('Model management headers section - editing', () => {

  let sandbox;
  let modelData;
  let headersSection;
  let tree;

  function openPage(userLang, systemLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    headersSection = modelData.getHeadersSection();
    tree = sandbox.getModelTree();
  }

  it('should be able to edit and revert a header back to initial value', () => {
    // Given I have opened a Media definition
    // When I select headers tab
    openPage('en', 'en', 'MX1001');

    // Then I expect save and cancel controls to be disabled
    saveAndCancelAreDisabled();

    // When I change the default header value
    let defaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);
    defaultHeader.setHeaderValue('new header value');

    // Then I expect the save and cancel controls to be enabled
    saveAndCancelAreEnabled();

    // And the header panel to be highlighted
    expect(defaultHeader.isDirty(), 'Changed header should be marked as dirty!').to.eventually.be.true;

    // And I expect the headers tab to be highlighted as modified
    expect(modelData.isHeadersSectionModified(), 'Headers section should be marked as dirty!').to.eventually.be.true;

    // When I complete the old header value back in the field
    defaultHeader.setHeaderValue('${eval(<span>(MX1001) Default Header</span>)}');

    // Then I expect save and cancel controls to be disabled again
    saveAndCancelAreDisabled();

    // And the header panel to not be highlighted
    expect(defaultHeader.isDirty(), 'Changed header should not be marked as dirty!').to.eventually.be.false;

    // And I expect the headers tab to not be highlighted
    expect(modelData.isHeadersSectionModified(), 'Headers section should not be marked as dirty!').to.eventually.be.false;
  });

  it('should properly manage the Modified state of the headers tab - multiple headers editing', () => {
    // Given I have opened a Media definition
    // When I select headers tab
    openPage('en', 'en', 'MX1001');

    // When I change the default header value
    let defaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);
    defaultHeader.setHeaderValue('new header value');

    // When I change the compact header value
    let compactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    compactHeader.setHeaderValue('new header value');

    // Then I expect the save and cancel controls to be enabled
    saveAndCancelAreEnabled();

    // And the header panel to be highlighted as modified
    expect(defaultHeader.isDirty(), 'Changed header should be marked as dirty!').to.eventually.be.true;

    // And I expect the headers tab to be highlighted as modified
    expect(modelData.isHeadersSectionModified(), 'Headers section should be marked as dirty!').to.eventually.be.true;

    // When I complete the old default header value back in the field
    defaultHeader.setHeaderValue('${eval(<span>(MX1001) Default Header</span>)}');

    // Then I expect the default header panel to not be highlighted
    expect(defaultHeader.isDirty(), 'Default header should not be marked as dirty!').to.eventually.be.false;

    // And I expect save and cancel controls to be enabled as the compact header is still modified
    saveAndCancelAreEnabled();

    // And I expect the headers tab to be highlighted as the compact header is still modified
    expect(modelData.isHeadersSectionModified(), 'Headers section should be marked as dirty!').to.eventually.be.true;

    // When I complete the old compact header value back in the field
    compactHeader.setHeaderValue('${eval(<span>(EO1001) Compact Header</span>)}');

    // Then I expect the compact header panel to not be highlighted
    expect(compactHeader.isDirty(), 'Compact header should not be marked as dirty!').to.eventually.be.false;

    // And I expect save and cancel controls to be disabled again
    saveAndCancelAreDisabled();

    // And I expect the headers tab to not be highlighted
    expect(modelData.isHeadersSectionModified(), 'Headers section should not be marked as dirty!').to.eventually.be.false;
  });

  it('should be able to save new values', () => {
    // Given I have opened a Media definition
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // And I have changed the default header value
    headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT).setHeaderValue('new header value in EN');

    // And I have changed the default language
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_DE);

    headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT).setHeaderValue('new header value in DE');

    // When I save model
    headersSection.getModelControls().getModelSave().click();

    // Then I expect the default header to be saved and to have the new value in EN and DE
    expect(headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT).getHeaderValue(), 'New header value in DE should be present!').to.eventually.equal('new header value in DE');
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_EN);
    expect(headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT).getHeaderValue(), 'New default header value should be present!').to.eventually.equal('new header value in EN');

    // And I expect the save and cancel controls to be disabled
    saveAndCancelAreDisabled();

    // And I expect the header panel to not be highlighted
    expect(headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT).isDirty(), 'Changed header should not be marked as dirty!').to.eventually.be.false;

    // And I expect the headers tab to not be highlighted
    expect(modelData.isHeadersSectionModified(), 'Headers section should not be marked as dirty!').to.eventually.be.false;
  });

  it('should allow changes to be canceled', () => {
    // Given I have opened a Media definition
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // And I have changed the default and compact header values
    let defaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);
    defaultHeader.setHeaderValue('new header value');
    let compactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    compactHeader.setHeaderValue('new header value');

    // When I cancel changes
    headersSection.getModelControls().getModelCancel().click();

    // Then I expect the default and compact header values to be reverted to their initial values
    expect(defaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(MX1001) Default Header</span>)}');
    expect(compactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');

    // And I expect the default and compact header panels to not be highlighted
    expect(defaultHeader.isDirty(), 'Changed header should not be marked as dirty!').to.eventually.be.false;
    expect(compactHeader.isDirty(), 'Changed header should not be marked as dirty!').to.eventually.be.false;

    // And I expect the save and cancel controls to be disabled
    saveAndCancelAreDisabled();

    // And I expect the headers tab to not be highlighted
    expect(modelData.isHeadersSectionModified(), 'Headers section should not be marked as dirty!').to.eventually.be.false;
  });

  it('should propagate changes from parent to child and allow overriding of parent in non default language', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I change the default language
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_BG);

    // And I edit compact header value
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    mediaCompactHeader.setHeaderValue('заглавие на медия');

    // And I open the parent model
    tree.search('entity');
    tree.getNode('entity').openObject();

    // Then I expect to see the parent's header to have its default value
    let entityCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    expect(entityCompactHeader.getHeaderValue(), 'Entity compact header value should be present!').to.eventually.equal('${eval(<span>(EO1001) Съкратено заглавие</span>)}');

    // When I edit the parent header
    entityCompactHeader.setHeaderValue('заглавие на обект');

    // And I open the child model
    tree.search('Media');
    tree.getNode('Media').openObject();

    // Then I expect the child header to still have the overridden value
    expect(mediaCompactHeader.getHeaderValue(), 'Media compact header value should be present!').to.eventually.equal('заглавие на медия');

    // When I cancel changes
    headersSection.getModelControls().getModelCancel().click();

    // Then I expect the child header to inherit the parent header again
    expect(mediaCompactHeader.getHeaderValue(), 'Media compact header value should be present!').to.eventually.equal('заглавие на обект');
  });

  it('should propagate changes from parent to inherited child header', () => {
    // Given I have opened the Entity definition which is parent of Media
    // And I have selected the headers tab
    openPage('en', 'en', 'EO1001');

    // And I have changed the compact header value
    let entityCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    entityCompactHeader.setHeaderValue('new compact header');

    // When I open Media definition from the tree
    tree.search('Media');
    tree.getNode('Media').openObject();

    // Then I expect to see the compact header to have the same value as in the Entity definition
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    expect(mediaCompactHeader.getHeaderValue(), 'Media compact header value should be present!').to.eventually.equal('new compact header');

    // When I cancel changes in Entity model
    tree.search('entity');
    tree.getNode('entity').openObject();
    headersSection.getModelControls().getModelCancel().click();

    // Then I expect Entity and Media to have the default value restored
    entityCompactHeader.expand();
    expect(entityCompactHeader.getHeaderValue(), 'Entity compact header value should be present!').to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');
    tree.search('Media');
    tree.getNode('Media').openObject();
    mediaCompactHeader.expand();
    expect(mediaCompactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');
  });

  it('should allow overriding of a parent header', () => {
    // Given I have opened a Media definition
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // And I have changed the compact header value
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    mediaCompactHeader.setHeaderValue('new compact header');

    // When I open Entity definition from the tree
    tree.search('entity');
    tree.getNode('entity').openObject();

    // Then I expect to see the compact header to have its default value
    let entityCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    expect(entityCompactHeader.getHeaderValue(), 'Entity compact header value should be present!').to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');
  });

  it('should revert changes on cancel only for the current model', () => {
    // Given I have opened the Entity definition which is parent of Media
    // And I have selected the headers tab
    openPage('en', 'en', 'EO1001');

    // And I have changed the entity compact header value
    let entityCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    entityCompactHeader.setHeaderValue('entity compact header');

    // And I have changed the media compact header value
    tree.search('Media');
    tree.getNode('Media').openObject();
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    mediaCompactHeader.setHeaderValue('media compact header');

    // When I cancel changes in child media definition
    headersSection.getModelControls().getModelCancel().click();

    // Then I expect the changes in media definition to be reverted. The parent header value takes place because the header is inherited.
    expect(mediaCompactHeader.getHeaderValue()).to.eventually.equal('entity compact header');

    // And I expect parent entity definition to remain unchanged
    tree.search('entity');
    tree.getNode('entity').openObject();
    entityCompactHeader.expand();
    expect(entityCompactHeader.getHeaderValue()).to.eventually.equal('entity compact header');
  });

  it('should copy value from the default language to current header', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // And I have changed the default language
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_BG);

    // When I copy default value in compact_header
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    mediaCompactHeader.copyDefaultValue();

    // Then I expect the header to have the same value as the compact_header in default language
    expect(mediaCompactHeader.getHeaderValue(), 'Media compact header value should be present!').to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');

    // Then I expect the save and cancel controls to be enabled
    saveAndCancelAreEnabled();

    // And the header panel to be highlighted as modified
    expect(mediaCompactHeader.isDirty(), 'Changed header should be marked as dirty!').to.eventually.be.true;

    // And I expect the headers tab to be highlighted as modified
    expect(modelData.isHeadersSectionModified(), 'Headers section should be marked as dirty!').to.eventually.be.true;
  });

  // mandatory headers are those in the default (system) language and their owning definition is not abstract
  it('should not allow model to be saved when a mandatory header has no value', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I expand the tooltip header panel
    let tooltipHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_TOOLTIP);

    // Then I expect the header to be marked as mandatory
    // And I expect to have error message that the tooltip header is mandatory
    tooltipHeader.setHeaderValue('');
    expect(tooltipHeader.isMandatory(), 'Header should be mandatory!').to.eventually.be.true;

    // Then I expect the save control to be disabled
    expect(headersSection.getModelControls().getModelSave().isDisabled(), 'Save control should be disabled!').to.eventually.be.true;

    // When I complete some value in the tooltip header
    tooltipHeader.setHeaderValue('new header value');

    // Then I expect the header to not be marked as mandatory
    // And I expect the error message to be missing
    expect(tooltipHeader.isMandatory(), 'Header should not be mandatory!').to.eventually.be.false;

    // And I expect the save control to be enabled
    saveAndCancelAreEnabled();

    // When I enter the old value of tooltip header
    tooltipHeader.setHeaderValue('${eval(<span>(EO1001) Tooltip Header</span>)}');

    // Then I expect save and cancel to be disabled
    saveAndCancelAreDisabled();

    // Then I delete the value of tooltip header
    tooltipHeader.setHeaderValue('');

    // And I change language to BG - headers in non system languages are not mandatory but model still has to be validated
    // and missing mandatory headers to be reported
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_BG);

    // And I complete some value in the tooltip header in BG language
    tooltipHeader.setHeaderValue('ново разширено заглавие');

    // Then I expect the save control to be disabled
    expect(headersSection.getModelControls().getModelSave().isDisabled(), 'Save control should be disabled!').to.eventually.be.true;

    // And I expect the headers tab to be marked as it has error
    expect(modelData.isHeadersSectionModified(), 'Headers section should be marked as dirty!').to.eventually.be.true;
  });

  it('should not be able to restore already inherited header from parent definition', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I expand the tooltip header panel
    let compactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);

    // And I expect the save control to be disabled
    saveAndCancelAreDisabled();

    // Then should be able to restore when the header is overridden
    expect(compactHeader.canBeRestored()).to.eventually.be.false;

    // Then header is inherited from entity
    compactHeader.isHeaderInheritedFrom('entity');

    // Then expect the header to be inherited from entity and to have the proper value present inside of it
    expect(compactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');
  });

  it('should be able to restore overridden header from the parent model definition', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I expand the tooltip header panel
    let defaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);

    // And I expect the save control to be disabled
    saveAndCancelAreDisabled();

    // Then should be able to restore when the header is overridden
    expect(defaultHeader.canBeRestored()).to.eventually.be.true;

    // When restore the header
    defaultHeader.restoreHeader().ok();

    // Then header is inherited from entity
    defaultHeader.isHeaderInheritedFrom('entity');

    // Then expect the header to be inherited from entity and to have the proper value present inside of it
    expect(defaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Default Header</span>)}');

    // Then controls are enabled
    saveAndCancelAreEnabled();

    // Then we click cancel to undo all section operations
    headersSection.getModelControls().getModelCancel().click();

    // Then header should not be inherited
    defaultHeader.isHeaderInherited(false);

    // Then the header is restored back to it's overridden state containing the original value from the model
    expect(defaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(MX1001) Default Header</span>)}');

    // Then after cancel header should again be able to be restored
    expect(defaultHeader.canBeRestored()).to.eventually.be.true;

    // Then from is not dirty
    saveAndCancelAreDisabled();
  });

  it('should be able to cancel restore inherited', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I expand the tooltip header panel
    let defaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);

    // Then should be able to restore when the header is overridden
    expect(defaultHeader.canBeRestored()).to.eventually.be.true;

    // cancel restore the header
    defaultHeader.restoreHeader().cancel();

    // Then expect the header to have its original value and to have the proper value present inside of it
    expect(defaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(MX1001) Default Header</span>)}');

    // Then controls are disabled
    saveAndCancelAreDisabled();

    // Then header should not be inherited
    defaultHeader.isHeaderInherited(false);

    // Then after cancel header should again be able to be restored
    expect(defaultHeader.canBeRestored()).to.eventually.be.true;
  });

  function saveAndCancelAreDisabled() {
    expect(headersSection.getModelControls().getModelSave().isDisabled(), 'Save control should be disabled!').to.eventually.be.true;
    expect(headersSection.getModelControls().getModelCancel().isDisabled(), 'Cancel control should be disabled!').to.eventually.be.true;
  }

  function saveAndCancelAreEnabled() {
    expect(headersSection.getModelControls().getModelSave().isDisabled(), 'Save control should be enabled!').to.eventually.be.false;
    expect(headersSection.getModelControls().getModelCancel().isDisabled(), 'Cancel control should be enabled!').to.eventually.be.false;
  }
});