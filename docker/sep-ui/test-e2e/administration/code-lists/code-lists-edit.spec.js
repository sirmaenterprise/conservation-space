'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;

let assertCodeListHeader = require('./code-lists-assertions').assertCodeListHeader;
let assertCodeDescriptions = require('./code-lists-assertions').assertCodeDescriptions;
let assertCodeDetails = require('./code-lists-assertions').assertCodeDetails;
let assertPreviewMode = require('./code-lists-assertions').assertPreviewMode;
let assertEditMode = require('./code-lists-assertions').assertEditMode;
let assertDescriptionEditMode = require('./code-lists-assertions').assertDescriptionEditMode;
let assertCodeValueActiveState = require('./code-lists-assertions').assertCodeValueActiveState;

describe('Controlled vocabularies management - edit:', () => {

  let management;

  beforeEach(() => {
    let sandbox = new CodeListsSandbox();
    sandbox.open(false);
    management = sandbox.getManagement();
  });

  it('should not allow to edit controlled vocabulary details, descriptions and values by default', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      let projectStateDetails = projectState.getDetails();
      assertPreviewMode(projectStateDetails);

      projectState.getValues().then(values => {
        assertPreviewMode(values[0]);
        assertPreviewMode(values[1]);
      });

      let descriptionsDialog = projectStateDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertDescriptionEditMode(descriptions[0], true);
        assertDescriptionEditMode(descriptions[1], true);
        assertDescriptionEditMode(descriptions[2], true);
      });

    });
  });

  it('should allow to enable edit mode for controlled vocabulary details form and values', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      let projectStateDetails = projectState.getDetails();

      projectState.edit();
      assertEditMode(projectStateDetails);

      projectState.getValues().then(values => {
        assertEditMode(values[0]);
        assertEditMode(values[1]);
      });

      let descriptionsDialog = projectStateDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertDescriptionEditMode(descriptions[0], false);
        assertDescriptionEditMode(descriptions[1], false);
        assertDescriptionEditMode(descriptions[2], false);
      });
    });
  });

  it('should update controlled vocabulary header when changing the name', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      let projectStateDetails = projectState.getDetails();
      let newName = 'Project state - updated';

      projectState.edit();
      projectStateDetails.setName(newName);
      assertCodeListHeader(projectState, '1', newName);
    });
  });

  it('should update controlled vocabulary name and comment in the current language through the descriptions', () => {
    // This tests the binding between description and descriptions fields
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      let projectStateDetails = projectState.getDetails();
      let newName = 'Project state - updated';
      let newComment = 'The available project states - updated';

      projectState.edit();
      projectStateDetails.setName(newName);
      projectStateDetails.setComment(newComment);

      let descriptionsDialog = projectStateDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertCodeDescriptions(descriptions[2], 'EN', newName, newComment);

        descriptions[2].setName(newName + '2');
        descriptions[2].setComment(newComment + '2');

        descriptionsDialog.confirm();
      });

      assertCodeDetails(projectStateDetails, '1', newName + '2', newComment + '2', '', '', '');
    });
  });

  it('should cancel update of controlled vocabulary name and comment in the current language through the' +
    ' descriptions', () => {
    // This tests the binding between description and descriptions fields
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      let projectStateDetails = projectState.getDetails();
      projectState.edit();

      // initial values
      let oldName = 'Project state';
      let oldComment = 'The available project states';

      projectStateDetails.setName(oldName);
      projectStateDetails.setComment(oldComment);

      let descriptionsDialog = projectStateDetails.openDescriptions();
      descriptionsDialog.getDescriptions().then(descriptions => {
        assertCodeDescriptions(descriptions[2], 'EN', oldName, oldComment);

        descriptions[2].setName('Project state - canceled');
        descriptions[2].setComment('The available project states - canceled');
        descriptionsDialog.cancelChanges();
      });
      assertCodeDetails(projectStateDetails, '1', oldName, oldComment, '', '', '');
    });
  });

  it('should update controlled vocabulary value name & comment through the descriptions', () => {
    // Same as above but for code value descriptions
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      let newName = 'Completed - updated';
      let newComment = 'New comment for completed state';

      projectState.getValues().then(values => {
        let completedState = values[1];
        completedState.setName(newName);
        completedState.setComment(newComment);

        let descriptionsDialog = completedState.openDescriptions();
        descriptionsDialog.getDescriptions().then(descriptions => {
          assertCodeDescriptions(descriptions[2], 'EN', newName, newComment);

          descriptions[2].setName(newName + '2');
          descriptions[2].setComment(newComment + '2');

          descriptionsDialog.confirm();
        });

        assertCodeDetails(completedState, 'COMPLETED', newName + '2', newComment + '2', 'The extra one', 'The extra two', 'The extra three');
      });
    });
  });

  it('should ask for a confirmation before saving changes made to a controlled vocabulary', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      let projectStateDetails = projectState.getDetails();
      projectStateDetails.setName('Project state - updated');

      let confirmDialog = projectState.save();
      confirmDialog.ok();
      assertPreviewMode(projectStateDetails);

      assertCodeDetails(projectStateDetails, '1', 'Project state - updated', 'The available project states', '', '', '');
    });
  });

  it('should be able to cancel the confirmation', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      let projectStateDetails = projectState.getDetails();
      projectStateDetails.setName('Project state - updated');

      let confirmDialog = projectState.save();
      confirmDialog.cancel();
      assertEditMode(projectStateDetails);

      assertCodeDetails(projectStateDetails, '1', 'Project state - updated', 'The available project states', '', '', '');
    });
  });

  it('should cancel changes made to a controlled vocabulary and its values while being in edit mode via confirmation', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      let projectStateDetails = projectState.getDetails();
      projectStateDetails.setName('Project state - updated');

      projectState.cancel();
      projectState.getConfirmDialog().ok();
      assertPreviewMode(projectStateDetails);

      assertCodeDetails(projectStateDetails, '1', 'Project state', 'The available project states', '', '', '');
    });
  });

  it('should cancel edit mode for a controlled vocabulary and its values without any changes', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();
      projectState.cancel();
      // No confirmation is needed
      let projectStateDetails = projectState.getDetails();
      assertPreviewMode(projectStateDetails);
      assertCodeDetails(projectStateDetails, '1', 'Project state', 'The available project states', '', '', '');
    });
  });

  it('should save value state change when form is saved', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      projectState.getValues().then(values => {
        assertCodeValueActiveState(values[0], false);
        values[0].toggleActiveState();
        assertCodeValueActiveState(values[0], true);
      });

      let confirmDialog = projectState.save();
      confirmDialog.ok();

      // Save will reload the code lists and values and elements will be stale -> reselect the values
      projectState.getValues().then(values => {
        assertCodeValueActiveState(values[0], true);
      });
    });
  });

  it('should revert value state change when form is canceled', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();
      projectState.edit();

      projectState.getValues().then(values => {
        assertCodeValueActiveState(values[0], false);
        values[0].toggleActiveState();
        assertCodeValueActiveState(values[0], true);

        projectState.cancel();
        projectState.getConfirmDialog().ok();
        assertCodeValueActiveState(values[0], false);
      });
    });
  });

  it('should not allow value state change when not in edit mode', () => {
    management.getCodeLists().then(codeLists => {
      let projectState = codeLists[0].open();

      projectState.getValues().then(values => {
        assertCodeValueActiveState(values[0], false);
        values[0].toggleActiveState();
        assertCodeValueActiveState(values[0], false);
      });
    });
  });

  it('should allow only one code list to be edited at most', () => {
    management.getCodeLists().then(codeLists => {
      codeLists[0].open().edit();

      expect(codeLists.length > 1).to.be.true;
      // rest of code lists should not be editable
      for (let i = 1; i < codeLists.length; ++i) {
        expect(codeLists[i].open().canEdit()).to.eventually.be.false;
      }
    });
  });
});