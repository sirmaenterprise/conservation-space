'use strict';

let CloneActionSandboxPage = require('./clone-action-sandbox-page').CloneActionSandboxPage;

describe('CloneAction', () => {
  let cloneActionSandboxPage;

  beforeEach(() => {
    cloneActionSandboxPage = new CloneActionSandboxPage();
    cloneActionSandboxPage.open();
  });

  it('should "Clone" button be enabled when definition of instance have not configured property existingInContext', () =>{
    // Executes "Clone" action on instance which definition have not configured property existingInContext.
    // Opens Clone action dialog.
    let cloneActionDialog = cloneActionSandboxPage.getCloneDialog('default');
    // Given:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    // When:
    // Add parent.
    cloneActionDialog.chooseContext();

    // Then:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    // When:
    // Parent is removed.
    cloneActionDialog.clearContext();

    // Then:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;
  });

  it('should "Clone" button be enabled when instance is marked both', () =>{
    // Executes "Clone" action on instance which is marked as existing without or in context.
    // Opens Clone action dialog.
    let cloneActionDialog = cloneActionSandboxPage.getCloneDialog('both');
    // Given:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    // When:
    // Add parent.
    cloneActionDialog.chooseContext();

    // Then:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    // When:
    // Parent is removed.
    cloneActionDialog.clearContext();

    // Then:
    // error message should not be displayed.
    expect(cloneActionDialog.hasError()).to.eventually.be.false;
    // 'Clone' button should be enabled.
    expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;
  });

  describe('withoutContext', () => {
    let cloneActionDialog;
    beforeEach(() => {
      // Executes "Clone" action on instance which is marked as existing without context but it have parent
      // Opens Clone action dialog.
      cloneActionDialog = cloneActionSandboxPage.getCloneDialog('withoutContext');
    });

    // Given:
    // Clone action dialog is opened.
    it('should clone action button be disabled when dialog is opened', () => {
      // Then:
      // error message should be displayed.
      expect(cloneActionDialog.hasError()).to.eventually.be.true;
      expect(cloneActionDialog.getErrorMessage()).to.eventually.be.equal('The object cannot exist in the selected context. Please, remove the context!');
      // // 'Select' context button should be disabled.
      expect(cloneActionDialog.isSelectContextButtonEnabled()).to.eventually.be.false;
      // // 'Clear' context button should be enabled.
      expect(cloneActionDialog.isClearContextButtonEnabled()).to.eventually.be.true;
      // 'Clone' button should be disabled
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.true;
    });

    // Given:
    // Clone action dialog is open.
    it('should remove error message when parent is removed', () => {
      // Error message should be displayed.
      expect(cloneActionDialog.hasError()).to.eventually.be.true;

      //When:
      // Remove parent.
      cloneActionDialog.clearContext();

      // Then:
      // The error message have to be removed.
      expect(cloneActionDialog.hasError()).to.eventually.be.false;
      // 'Clear' context button should be removed.
      expect(cloneActionDialog.isClearContextButtonVisible()).to.be.false;
      // 'Select' context button should be disabled.
      expect(cloneActionDialog.isSelectContextButtonEnabled()).to.eventually.be.false;
    });

    // Given:
    // Clone action dialog is open.
    it('should "Clone" button be enabled when all mandatory properties are filed and parent is removed', () => {
      // 'Clone' button should be disabled.
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.true;

      // When:
      // Parent is removed.
      cloneActionDialog.clearContext();
      // Mandatory property is filled.
      cloneActionDialog.fillDescription('Some description');

      // Then:
      // 'Clone' button should be enabled.
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    });
  });

  describe('inContext', () => {
    let cloneActionDialog;
    beforeEach(() => {
      // Executes "Clone" action on instance which is marked as existing in context but it have not parent
      // Opens Clone action dialog.
      cloneActionDialog = cloneActionSandboxPage.getCloneDialog('inContext');
    });

    // Given:
    // Clone action dialog is opened.
    it('should clone action button be disabled when dialog is opened', () => {
      // Then:
      // error message should be displayed.
      expect(cloneActionDialog.hasError()).to.eventually.be.true;
      expect(cloneActionDialog.getErrorMessage()).to.eventually.be.equal('The object cannot exist without context. Please, select context!');
      // 'Select' context button should be enabled.
      expect(cloneActionDialog.isSelectContextButtonEnabled()).to.eventually.be.true;
      // 'Clear' context button should be not visible.
      expect(cloneActionDialog.isClearContextButtonVisible()).to.be.false;
      // 'Clone' button should be disabled
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.true;
    });

    // Given:
    // Clone action dialog is open.
    it('should remove error message when select a parent', () => {
      // Error message should be displayed.
      expect(cloneActionDialog.hasError()).to.eventually.be.true;

      //When:
      // Add parent.
      cloneActionDialog.chooseContext();

      // Then:
      // The error message have to be removed.
      expect(cloneActionDialog.hasError()).to.eventually.be.false;
      // 'Clear' context button should be disabled.
      expect(cloneActionDialog.isClearContextButtonEnabled()).to.eventually.be.false;
      // // 'Select' context button should be enabled.
      expect(cloneActionDialog.isSelectContextButtonEnabled()).to.eventually.be.true;
    });

    // Given:
    // Clone action dialog is open.
    it('should Clone button be enabled when all mandatory are filed and parent is selected', () => {
      // 'Clone' button should be disabled.
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.true;

      // When:
      // Parent is removed.
      cloneActionDialog.chooseContext();
      // Mandatory property is filled.
      cloneActionDialog.fillDescription('Some description');

      // Then:
      // 'Clone' button should be enabled.
      expect(cloneActionDialog.isCloneButtonDisabled()).to.eventually.be.false;

    });
  });
});