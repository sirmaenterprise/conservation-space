'use strict';

let SandboxPage = require('../../../../../page-object').SandboxPage;
let UserAvatar = require('../../../../../user/avatar/user-avatar').UserAvatar;

let page = new SandboxPage();

const URL = 'sandbox/layout/top-header/main-menu/user-menu/settings';

describe('Settings" ', () => {
  beforeEach(() => {
    page.open(URL);
  });

  it('should show user settings menu', () => {
    let menu = element(by.id('userSettingsMenu'));
    expect(menu.isPresent()).to.eventually.be.true;
  });

  it('should show default avatar if there is no thumbnail available for the user', () => {
    let userAvatar = new UserAvatar($('.user-avatar'));
    userAvatar.waitForDefaultIcon();
    expect(userAvatar.isDefaultIcon()).to.eventually.be.true;
  });
});