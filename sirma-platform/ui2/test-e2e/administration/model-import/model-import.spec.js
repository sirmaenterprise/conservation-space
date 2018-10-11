'use strict';

let ModelImportSandboxPage = require('./model-import').ModelImportSandboxPage;

describe('Model import', () => {

  let page = new ModelImportSandboxPage();

  it('should have the import type select drop down visible & present', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    // Then the import select drop down should be displayed & present
    expect(panel.isImportTypeSelectionDisplayed()).to.eventually.be.true;
  });

  it('should have the import type definition selected as default', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    // Then definition should be selected as default for import type select
    expect(panel.getSelectedImportType()).to.eventually.eq('definition');
  });

  it('should allow upload of multiple files', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    // Then the import and clear buttons should not be visible
    expect(panel.isImportAllowed()).to.eventually.be.false;
    expect(panel.isClearAllowed()).to.eventually.be.false;

    // When I attach zip, xml and bpmn file
    panel.selectFiles('1.zip', '2.xml');

    expect(panel.getSelectedFiles()).to.eventually.eql(['1.zip', '2.xml']);

    // And select the Upload action
    panel.import();

    // And when the upload finishes I should see a notification
    panel.waitForNotification();

    // And clear selected files
    expect(panel.getSelectedFiles()).to.eventually.be.empty;
  });

  it('should ignore duplicate files', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    // When I select a file
    panel.selectFiles('1.zip', '2.xml');

    // And then select it again
    panel.selectFiles('1.zip');

    // Then I should see the file selected only once
    expect(panel.getSelectedFiles()).to.eventually.eql(['1.zip', '2.xml']);
  });

  it('should ignore not allowed file types when import type is definition', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    panel.selectDefinitionImportType();

    // When I select a that is not allowed (.txt)
    panel.selectFiles('1.txt');

    // Then I should not see the not allowed file in the selected files list
    expect(panel.getSelectedFiles()).to.eventually.eql([]);
  });

  it('should allow clearing of the selected files', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    // When I select a file
    panel.selectFiles('1.zip');

    // And select the Clear action
    panel.clear();

    // Then the selected files should be cleared
    expect(panel.getSelectedFiles()).to.eventually.be.empty;
  });

  it('should display list of model validation errors in a modal window', () => {
    // Given I have opened the model import page in failing mode
    page.open(true, true);

    let panel = page.getModelImportPanel();

    // When I select a file
    panel.selectFiles('1.zip');

    // And select the Upload action
    panel.import();

    // And I should see dialog with error messages
    expect(panel.getErrorsDialog().getBodyElement().getText()).to.eventually.contains('Something went wrong');

    // And clear selected files
    expect(panel.getSelectedFiles()).to.eventually.be.empty;
  });

  it('should display empty error dialog when operation fails unexpectedly', () => {
    // Given I have opened the model import page in failing mode
    page.open(true, false);

    let panel = page.getModelImportPanel();

    // When I select a file
    panel.selectFiles('1.zip');

    // And select the Upload action
    panel.import();

    // And I should see dialog with error messages
    expect(panel.getErrorsDialog().getBodyElement().getText()).to.eventually.equals('');
  });

  it('should allow the correct file types when import type is ontology', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    panel.selectOntologyImportType();

    // When I select a that is allowed (.ttl)
    panel.selectFiles('2.ttl');

    // When I select a that is allowed (.ts)
    panel.selectFiles('3.ns');

    // Then I should not see the allowed files in the selected files list
    expect(panel.getSelectedFiles()).to.eventually.eql(['2.ttl', '3.ns']);
  });

  it('should ignore not allowed file types when import type is ontology', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel();

    panel.selectOntologyImportType();

    // When I select a that is not allowed (.txt)
    panel.selectFiles('1.txt');

    // When I select a that is allowed (.sparql)
    panel.selectFiles('3.sparql');

    // Then I should not see the not allowed file in the selected files list
    expect(panel.getSelectedFiles()).to.eventually.eql(['3.sparql']);
  });

  it('should allow downloading selected models', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // Then then download should not be allowed
    expect(panel.isDownloadAllowed()).to.eventually.be.false;

    // When I select a definition
    panel.getImportedDefinitions().then(definitions => definitions[0].click());

    // Then the download should be allowed
    expect(panel.isDownloadAllowed()).to.eventually.be.true;

    // When I also select a template
    panel.getImportedTemplates().then(template => template[0].click());

    // Then the download should be allowed
    expect(panel.isDownloadAllowed()).to.eventually.be.true;

    // When I select the download button
    panel.download();

    // Then the file should be provided for download
    expect(page.getDownloadedFileName()).to.eventually.equals('models.zip');

    page.getDownloadRequest().then(downloadRequest => {
      expect(downloadRequest).to.eql({
        definitions: ['OT210027'],
        templates: ['commonDocument']
      });
    });
  });

  it('should allow downloading all models', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // When I select all templates and definitions
    panel.selectAllTemplates();
    panel.selectAllDefinitions();

    // And when I select the download button
    panel.download();

    // Then the file should be provided for download
    expect(page.getDownloadedFileName()).to.eventually.equals('models.zip');

    page.getDownloadRequest().then(downloadRequest => {
      expect(downloadRequest).to.eql({
        allTemplates: true,
        allDefinitions: true
      });
    });
  });

  it('should allow selecting and deselecting of all templates', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // When I select "select all templates" checkbox
    panel.selectAllTemplates();

    let importedTemplates = panel.getImportedTemplates();

    // Then all templates should get selected
    importedTemplates.then(templates => {
      templates.forEach(template => expect(template.isSelected()).to.eventually.be.true);
    });

    // when I deselect one template
    importedTemplates.then(template => template[0].click());

    // Then the "select all templates" checkbox should get unchecked
    expect(panel.isSelectAllTemplatesCheckBoxSelected()).to.eventually.be.false;

    // When I select it again,
    importedTemplates.then(templates => templates[0].click());

    // Then "select all templates" should get enabled
    expect(panel.isSelectAllTemplatesCheckBoxSelected()).to.eventually.be.true;

    // When I select "select all templates" again
    panel.selectAllTemplates();

    // Then all templates should get deselected
    importedTemplates.then(templates => {
      templates.forEach(template => expect(template.isSelected()).to.eventually.be.false);
    });
  });

  it('should allow selecting and deselecting of all definitions', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // When I select "select all definitions" checkbox
    panel.selectAllDefinitions();

    let importedDefinitions = panel.getImportedDefinitions();

    // Then all definitions should get selected
    importedDefinitions.then(definitions => {
      definitions.forEach(definition => expect(definition.isSelected()).to.eventually.be.true);
    });

    // when I deselect one definition
    importedDefinitions.then(definition => definition[0].click());

    // Then the "select all definitions" checkbox should get unchecked
    expect(panel.isSelectAllDefinitionsCheckBoxSelected()).to.eventually.be.false;

    // When I select it again,
    importedDefinitions.then(definitions => definitions[0].click());

    // Then "select all definitions" should get enabled
    expect(panel.isSelectAllDefinitionsCheckBoxSelected()).to.eventually.be.true;

    // When I select "select all definitions" again
    panel.selectAllDefinitions();

    // Then all definitions should get deselected
    importedDefinitions.then(definitions => {
      definitions.forEach(definition => expect(definition.isSelected()).to.eventually.be.false);
    });
  });

  it('should correctly display imported templates data', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // Then I should see the templates displayed in the table
    panel.getImportedTemplates().then(templates => {
      expect(templates.length).to.equals(2);

      expect(templates[0].getTitle()).to.eventually.equals('Common Document Template');
      expect(templates[0].getPurpose()).to.eventually.equals('uploadable');
      expect(templates[0].getPrimary()).to.eventually.equals('false');
      expect(templates[0].getForType()).to.eventually.equals('Audio');
      expect(templates[0].getModifiedOn()).to.eventually.contain('19.03.18');
      expect(templates[0].getModifiedBy()).to.eventually.equals('User1 Header');

      expect(templates[1].getTitle()).to.eventually.equals('Test Document Template');
      expect(templates[1].getPurpose()).to.eventually.equals('uploadable');
      expect(templates[1].getPrimary()).to.eventually.equals('true');
      expect(templates[1].getForType()).to.eventually.equals('Audio');
      expect(templates[1].getModifiedOn()).to.eventually.contain('19.03.18');
      expect(templates[1].getModifiedBy()).to.eventually.equals('User2 Header');
    });
  });

  it('should correctly display imported definitions data', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // Then I should see the definitions displayed in the table
    panel.getImportedDefinitions().then(definitions => {
      expect(definitions.length).to.equals(2);

      expect(definitions[0].getIdentifier()).to.eventually.equals('OT210027');
      expect(definitions[0].getType()).to.eventually.equals('Common document');
      expect(definitions[0].isAbstract()).to.eventually.equals('True');
      expect(definitions[0].getFileName()).to.eventually.equals('OT210027.xml');
      expect(definitions[0].getModifiedOn()).to.eventually.contain('19.03.18');
      expect(definitions[0].getModifiedBy()).to.eventually.equals('User1 Header');

      expect(definitions[1].getIdentifier()).to.eventually.equals('MS210001');
      expect(definitions[1].getType()).to.eventually.equals('Approval document');
      expect(definitions[1].isAbstract()).to.eventually.equals('False');
      expect(definitions[1].getFileName()).to.eventually.equals('MS210001.xml');
      expect(definitions[1].getModifiedOn()).to.eventually.contain('19.03.18');
      expect(definitions[1].getModifiedBy()).to.eventually.equals('Admin Header');
    });
  });

  it('should allow filtering of the imported models using single filter', () => {
    // Given I have opened the model import page
    page.open();

    let panel = page.getModelImportPanel().getModelDownloadPanel();

    // When I input a filtering string in the filter field
    panel.filter('user1');

    // Then I should see only the models that match the filtering criteria
    panel.getImportedDefinitions().then(definitions => {
      expect(definitions.length).to.equals(1);

      expect(definitions[0].getIdentifier()).to.eventually.equals('OT210027');
    });

    panel.getImportedTemplates().then(templates => {
      expect(templates.length).to.equals(1);

      expect(templates[0].getTitle()).to.eventually.equals('Common Document Template');
    });

    // When I clear the filter term
    panel.filter('');

    // Then I should see all imported definitions and templates
    panel.getImportedDefinitions().then(definitions => {
      expect(definitions.length).to.equals(2);
    });

    panel.getImportedTemplates().then(templates => {
      expect(templates.length).to.equals(2);
    });
  });

});