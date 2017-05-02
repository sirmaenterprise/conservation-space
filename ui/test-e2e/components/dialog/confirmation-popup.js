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

  clickCancelButton() {
    let btnCancel = $('.modal .seip-btn-cancel');
    browser.wait(EC.elementToBeClickable(btnCancel), DEFAULT_TIMEOUT);
    btnCancel.click();
  }
}

module.exports = ConfirmationPopup;
