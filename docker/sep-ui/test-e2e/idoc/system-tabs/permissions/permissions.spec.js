var PermissionsSandboxPage = require('./permissions').PermissionsSandboxPage;

describe('Permissions', function () {
  var page = new PermissionsSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should add new user', ()=> {
    // Given I have opened the permissions for edit
    var permissionsPanel = page.getPermissionsPanel();
    permissionsPanel.edit();

    // When I add new entry
    var entry = permissionsPanel.addEntry();
    entry.getAuthority().selectOption('John Doe');
    entry.getSpecialPermissions().selectOption('Manager');

    // And save
    permissionsPanel.save();

    // Then I should see the new entry along with the others
    expect(permissionsPanel.getCount()).to.eventually.equal(3);
  });

  it('should cancel adding new user', ()=> {
    // Given I have opened the permissions for edit
    var permissionsPanel = page.getPermissionsPanel();
    permissionsPanel.edit();

    // When I add new entry
    var entry = permissionsPanel.addEntry();

    // And Cancel the save

    permissionsPanel.cancel();

    // Then the new entry should be removed
    expect(permissionsPanel.getCount()).to.eventually.equal(2);
  });

  it('should provide buttons for sorting', ()=> {
    // Given I have opened the permissions for edit
    var permissionsPanel = page.getPermissionsPanel();

    // And when I sort descending
    permissionsPanel.sortDescending();

    // Then I should be able to sort ascending
    permissionsPanel.sortAscending();
  });

  it('should restore parent permissions', () => {
    // Given I have opened the permissions for edit
    var permissionsPanel = page.getPermissionsPanel();
    permissionsPanel.edit();

    // When i click 'Restore children permissions'
    permissionsPanel.restoreChildrenPermissions();

    browser.wait(EC.textToBePresentInElement($('body'), 'Parent permissions restored for emf:5e15053a-563c-4cab-aa8f-4edbd4db70a1'), DEFAULT_TIMEOUT);
  });

});
