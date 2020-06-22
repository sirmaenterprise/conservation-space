'use strict';

let NewDialog = require('../new-dialog/new-dialog');

var EC = protractor.ExpectedConditions;

class MainMenu {

  adminMenuIsPresent() {
    let adminMenu = $('#adminMenu');
    browser.wait(EC.elementToBeClickable(adminMenu), DEFAULT_TIMEOUT);
    return adminMenu.isPresent();
  }

  openAdminMenu() {
    let adminMenu = $('#adminMenu');
    adminMenu.click();
    return new AdminMenu();
  }

  createButtonIsPresent() {
    let createButton = $('.create-new');
    browser.wait(EC.elementToBeClickable(createButton), DEFAULT_TIMEOUT);
    return createButton.isPresent();
  }

  openCreateDialog() {
    let createButton = $('.create-new');
    browser.wait(EC.elementToBeClickable(createButton), DEFAULT_TIMEOUT);
    createButton.click();
    let dialog = new NewDialog();
    dialog.waitUntilOpened();
    return dialog.getCreatePanel();
  }

  openCreateDialogWithExtensions() {
    let createButton = $('.create-new');
    browser.wait(EC.elementToBeClickable(createButton), DEFAULT_TIMEOUT);
    createButton.click();
    let dialog = new NewDialog();
    dialog.waitUntilOpened();
    return dialog.getCreatePanelWithExtensions();
  }

  openUploadPanel() {
    let createButton = $('.create-new');
    browser.wait(EC.elementToBeClickable(createButton), DEFAULT_TIMEOUT);
    createButton.click();
    let dialog = new NewDialog();
    dialog.waitUntilOpened();
    return dialog.getUploadPanel();
  }
}

class UserSettingsMenu {
  openUserSettingsMenu() {
    let userSettingsMenu = $('#userSettingsMenu');
    browser.wait(EC.elementToBeClickable(userSettingsMenu), DEFAULT_TIMEOUT);
    userSettingsMenu.click();
  }

  selectLogout() {
    let logoutAction = $('.logout-action');
    logoutAction.click();
  }

  selectChangePassword() {
    let changePassword = $('.change-password-action');
    changePassword.click();
  }
}

class AdminMenu {

  selectAdminOption(option) {
    let options = $$('.menu-item a').get(option);
    options.click();
  }
}

module.exports = MainMenu;
module.exports.UserSettingsMenu = UserSettingsMenu;