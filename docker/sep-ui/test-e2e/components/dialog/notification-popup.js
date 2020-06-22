"use strict";

class NotificationPopup {

  notificationPopup() {
    return element(by.className('notification'));
  }

  clickOkButton() {
    var btnOK = element(by.className('seip-btn-ok'));
    browser.wait(EC.elementToBeClickable(btnOK), DEFAULT_TIMEOUT);
    btnOK.click();
  }
}

module.exports = NotificationPopup;
