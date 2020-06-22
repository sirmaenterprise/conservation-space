'use strict';

class ConfirmationPopup {

  getConfirmationPopup() {
    return $('.confirmation');
  }

  clickConfirmButton() {
    let btnConfirm = $('.modal .seip-btn-confirm');
    browser.wait(EC.elementToBeClickable(btnConfirm), DEFAULT_TIMEOUT);
    btnConfirm.click();
  }

  clickYesButton() {
    let btnYes = $('.modal .seip-btn-yes');
    browser.wait(EC.elementToBeClickable(btnYes), DEFAULT_TIMEOUT);
    btnYes.click();
  }

  clickResumeButton() {
    let btnResume = $('.seip-btn-resume');
    browser.wait(EC.elementToBeClickable(btnResume), DEFAULT_TIMEOUT);
    btnResume.click();
  }

  clickCancelButton() {
    let btnCancel = $('.modal .seip-btn-cancel');
    browser.wait(EC.elementToBeClickable(btnCancel), DEFAULT_TIMEOUT);
    btnCancel.click();
  }
}

module.exports = ConfirmationPopup;
