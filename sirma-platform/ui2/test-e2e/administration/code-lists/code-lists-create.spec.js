'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;

let assertCodeListHeader = require('./code-lists-assertions').assertCodeListHeader;
let assertCodeDetails = require('./code-lists-assertions').assertCodeDetails;
let assertPreviewMode = require('./code-lists-assertions').assertPreviewMode;
let assertCreateMode = require('./code-lists-assertions').assertCreateMode;
let assertCodeValueActiveState = require('./code-lists-assertions').assertCodeValueActiveState;
let assertCodeDescriptions = require('./code-lists-assertions').assertCodeDescriptions;

describe('Controlled vocabularies management - create:', () => {

  let management;

  beforeEach(() => {
    management = new CodeListsSandbox().open(false).getManagement();
  });

  it('should allow the creation of new controlled vocabularies and values', () => {
    management.addCodeList();

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(4);

      // Should be inserted on top
      let newCode = codeLists[0];
      // It should be opened by default & in create mode
      let newCodeDetails = newCode.getDetails();
      assertCreateMode(newCodeDetails);

      newCodeDetails.setId('123');
      newCodeDetails.setName('New code');
      newCodeDetails.setExtra('1', 'Extra 1');

      let confirmDialog = newCode.save();
      confirmDialog.ok();
    });

    // After save, code lists are reloaded and for new code lists the PO will be stale -> should be fetched again
    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(4);
      // Should be last after sorting
      let newCode = codeLists[3];
      newCode.open();
      let newCodeDetails = newCode.getDetails();

      assertPreviewMode(newCodeDetails);
      assertCodeListHeader(newCode, '123', 'New code');
      assertCodeDetails(newCodeDetails, '123', 'New code', '', 'Extra 1', '', '');
    });
  });

  it('should create the new controlled vocabulary in the user\'s language', () => {
    management.addCodeList().then(newCode => {
      let newCodeDetails = newCode.getDetails();
      newCodeDetails.setName('New code');
      newCodeDetails.setComment('New code to test language');

      let descriptionsDialog = newCodeDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertCodeDescriptions(descriptions[1], 'EN', 'New code', 'New code to test language');
      });
    });
  });

  it('should update the controlled vocabulary header when changing the ID', () => {
    management.addCodeList().then(newCode => {
      let newCodeDetails = newCode.getDetails();
      assertCodeListHeader(newCode, '', '');
      newCodeDetails.setId('123');
      newCodeDetails.setName('New code');
      assertCodeListHeader(newCode, '123', 'New code');
    });
  });

  it('should allow to cancel the creation of new controlled vocabulary', () => {
    management.addCodeList().then(newCode => {
      let confirmDialog = newCode.cancelCreate();
      confirmDialog.ok();

      management.getCodeLists().then(codeLists => {
        expect(codeLists.length).to.equal(3);
        // Checks if it is removed from top
        assertCodeListHeader(codeLists[0], '1', 'Project state');
      });
    });
  });

  it('should be able to dismiss the confirmation dialog for creating a controlled vocabulary', () => {
    management.addCodeList().then(newCode => {
      let newCodeDetails = newCode.getDetails();
      newCodeDetails.setId('123');
      newCodeDetails.setName('New code');

      let confirmDialog = newCode.save();
      confirmDialog.cancel();

      assertCreateMode(newCodeDetails);
    });
  });

  it('should allow the creation of new controlled vocabularies values', () => {
    management.addCodeList().then(newCode => {
      let newCodeDetails = newCode.getDetails();
      newCodeDetails.setId('123');
      newCodeDetails.setName('New code');

      newCode.addValue();

      newCode.getValues().then(values => {
        expect(values.length).to.equal(1);

        // Should be inserted on top
        let newValue = values[0];
        assertCreateMode(newValue);

        newValue.setId('V1');
        newValue.setName('Value 1');
      });

      let confirmDialog = newCode.save();
      confirmDialog.ok();
    });

    management.getCodeLists().then(codeLists => {
      let newCode = codeLists[3];
      newCode.open();
      newCode.getValues().then(values => {
        expect(values.length).to.equal(1);
        let newValue = values[0];
        assertPreviewMode(newValue);
        assertCodeDetails(newValue, 'V1', 'Value 1', '', '', '', '');
        assertCodeValueActiveState(newValue, true);
      });
    });
  });

  it('should allow the creation of new controlled vocabularies value in existing controlled vocabulary', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();

      projectState.edit();
      projectState.addValue();

      projectState.getValues().then(values => {
        expect(values.length).to.equal(3);

        let newValue = values[0];
        assertCreateMode(newValue);

        newValue.setId('V1');
        newValue.setName('Value 1');
      });

      let confirmDialog = projectState.save();
      confirmDialog.ok();

      // Saving existing code list should not make it stale in the DOM
      projectState.getValues().then(values => {
        // Should be sorted last after saving & reloading
        let newValue = values[2];
        assertPreviewMode(newValue);
        assertCodeDetails(newValue, 'V1', 'Value 1', '', '', '', '');
      });
    });
  });

  it('should allow to remove controlled vocabulary value which is not yet saved', () => {
    management.addCodeList().then(newCode => {
      newCode.addValue().then(value => {
        value.remove();
      });

      newCode.getValues().then(values => {
        expect(values.length).to.equal(0);
      });
    });
  });

  it('should update the pagination when inserting new controlled vocabulary values', () => {
    management.addCodeList().then(newCode => {
      let pagination = newCode.getPagination();

      newCode.addValue();
      newCode.addValue();
      newCode.addValue();
      pagination.waitUntilNotVisible();
      expect(pagination.getPages().count()).to.eventually.equal(1);

      newCode.addValue().then(value => {
        pagination.waitUntilVisible();
        expect(pagination.getPages().count()).to.eventually.equal(2);

        value.remove();
        pagination.waitUntilNotVisible();
        expect(pagination.getPages().count()).to.eventually.equal(1);
      });
    });
  });

  it('should disable edit while creating new code list', () => {
    management.addCodeList();

    management.getCodeLists().then(codeLists => {
      let newCodeList = codeLists[0];

      let newCodeDetails = newCodeList.getDetails();
      assertCreateMode(newCodeDetails);

      newCodeDetails.setId('123');
      newCodeDetails.setName('New code');

      expect(newCodeList.canSave()).to.eventually.be.true;
      // rest of code lists should not be editable
      for (let i = 1; i < codeLists.length; ++i) {
        expect(codeLists[i].open().canEdit()).to.eventually.be.false;
      }
    });
  });
});