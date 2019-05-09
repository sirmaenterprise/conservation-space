class ModelTestUtils {

  static canSaveOrCancelFieldsSection(modelData, fields) {
    expect(modelData.isFieldsSectionModified(), 'Fields section should be modified!').to.eventually.be.true;
    expect(fields.getModelControls().getModelSave().isDisabled(), 'Save button should be disabled!').to.eventually.be.false;
    expect(fields.getModelControls().getModelCancel().isDisabled(), 'Cancel button should be disabled!').to.eventually.be.false;
  }

  static cannotSaveOrCancelFieldsSection(modelData, fields) {
    expect(modelData.isFieldsSectionModified(), 'Fields section should not be modified!').to.eventually.be.false;
    expect(fields.getModelControls().getModelSave().isDisabled(), 'Save button should be enabled!').to.eventually.be.true;
    expect(fields.getModelControls().getModelCancel().isDisabled(), 'Cancel button should be enabled!').to.eventually.be.true;
  }

  static saveSection(section) {
    // save the field and add that to the definition
    let save = section.getModelControls().getModelSave();
    browser.wait(save.isEnabled(), DEFAULT_TIMEOUT);
    save.click();

    // make sure that notification has appeared
    let notification = save.getNotification();
    browser.wait(notification.isSuccess(), DEFAULT_TIMEOUT);

    // close the notification
    return notification.close();
  }
}

module.exports = {
  ModelTestUtils
};