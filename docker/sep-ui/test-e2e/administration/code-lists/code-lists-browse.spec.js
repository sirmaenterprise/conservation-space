'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;
let assertCodeListHeader = require('./code-lists-assertions').assertCodeListHeader;
let assertCodeDetails = require('./code-lists-assertions').assertCodeDetails;
let assertCodeDescriptions = require('./code-lists-assertions').assertCodeDescriptions;
let assertCodeValuesPage = require('./code-lists-assertions').assertCodeValuesPage;
let assertCodeValueActiveState = require('./code-lists-assertions').assertCodeValueActiveState;

describe('Controlled vocabularies management - browsing', () => {

  let management;

  function openPage(userLang, systemLang) {
    let sandbox = new CodeListsSandbox();
    sandbox.open(false, userLang, systemLang);
    management = sandbox.getManagement();
  }

  it('should list the available control vocabularies in the system', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(5);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
      assertCodeListHeader(codeLists[2], '3', 'Level');
      assertCodeListHeader(codeLists[3], '13', 'Language');
      assertCodeListHeader(codeLists[4], '555', 'Country');
    });
  });

  it('should allow to toggle control vocabulary details section', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectStateCl = codeLists[0];
      projectStateCl.waitUntilClosed();

      projectStateCl.open();
      projectStateCl.close();

      projectStateCl.open();
      projectStateCl.close();
    });
  });

  it('should display control vocabulary details in the section', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectStateDetails = codeLists[0].open().getDetails();
      assertCodeDetails(projectStateDetails, '1', 'Project state', 'The available project states', '', '', '');

      let projectTypeDetails = codeLists[1].open().getDetails();
      assertCodeDetails(projectTypeDetails, '2', 'Project type', '', 'One extra', 'Two extras', 'Three extras');

      let levelDetails = codeLists[2].open().getDetails();
      assertCodeDetails(levelDetails, '3', 'Level', '', '', '', '');

      let languageDetails = codeLists[3].open().getDetails();
      assertCodeDetails(languageDetails, '13', 'Language', '', '', '', '');
    });
  });

  it('should display control vocabulary descriptions in different languages ordered alphabetically', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectStateDetails = codeLists[0].open().getDetails();
      let descriptionsDialog = projectStateDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertCodeDescriptions(descriptions[0], 'BG', 'Статус на проект', '');
        assertCodeDescriptions(descriptions[1], 'DE', 'Projektstatus', 'Die verfügbaren Projektstatus');
        assertCodeDescriptions(descriptions[2], 'EN', 'Project state', 'The available project states');
        assertCodeDescriptions(descriptions[3], 'FI', '', '');
      });
    });
  });

  it('should list control vocabulary values details', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.getValues().then(values => {
        assertCodeValueActiveState(values[0], false);
        assertCodeValueActiveState(values[1], true);

        assertCodeDetails(values[0], 'APPROVED', 'Approved', 'The approved project states', '', '', '');
        assertCodeDetails(values[1], 'COMPLETED', 'Completed', '', 'The extra one', 'The extra two', 'The extra three');
      });
    });
  });

  it('should paginate control vocabulary values', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectType = codeLists[1].open();

      let pagination = projectType.getPagination();
      expect(pagination.getPages().count()).to.eventually.equal(3);

      projectType.getValues().then(values => {
        assertCodeValuesPage(values, ['PR0001', 'PR0002', 'PR0003']);
      });

      pagination.goToPage(2);
      projectType.getValues().then(values => {
        assertCodeValuesPage(values, ['PR0004', 'PR0005', 'PR0006']);
      });

      pagination.goToPage(3);
      projectType.getValues().then(values => {
        assertCodeValuesPage(values, ['PR0007']);
      });
    });
  });

  it('should list control vocabulary value descriptions in different languages', () => {
    openPage();
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.getValues().then(values => {
        let approvedDescriptionsDialog = values[0].openDescriptions();
        approvedDescriptionsDialog.getDescriptions().then(descriptions => {
          assertCodeDescriptions(descriptions[0], 'BG', 'Одобрен', '');
          assertCodeDescriptions(descriptions[1], 'DE', 'Genehmigt', 'Die Genehmigt projektstatus');
          assertCodeDescriptions(descriptions[2], 'EN', 'Approved', 'The approved project states');
          assertCodeDescriptions(descriptions[3], 'FI', '', 'Null to test the validation');
        });
      });
    });
  });

  it('should use the system language if for the user\'s there are no descriptions', () => {
    openPage('nl', 'de');
    management.getCodeLists().then(codeLists => {
      assertCodeListHeader(codeLists[0], '1', 'Projektstatus');
      assertCodeListHeader(codeLists[1], '2', 'Projekttyp');
      assertCodeListHeader(codeLists[2], '3', 'Level');
      assertCodeListHeader(codeLists[3], '13', 'Sprache');

      let projectState = codeLists[0].open();
      assertCodeDetails(projectState.getDetails(), '1', 'Projektstatus', 'Die verfügbaren Projektstatus', '', '', '');

      projectState.getValues().then(values => {
        assertCodeDetails(values[0], 'APPROVED', 'Genehmigt', 'Die Genehmigt projektstatus', '', '', '');
        assertCodeDetails(values[1], 'COMPLETED', 'Abgeschlossen', '', 'The extra one', 'The extra two', 'The extra three');
      });
    });
  });

  it('should use the default language if the user\'s and the system\'s are not available in the descriptions', () => {
    openPage('nl', 'ch');
    management.getCodeLists().then(codeLists => {
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
      assertCodeListHeader(codeLists[2], '3', 'Level');
      assertCodeListHeader(codeLists[3], '13', 'Language');
    });
  });

});