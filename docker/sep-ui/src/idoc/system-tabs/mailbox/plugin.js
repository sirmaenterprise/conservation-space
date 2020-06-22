PluginRegistry.add('idoc-system-tabs', {
  'id': 'mailbox-tab',
  'name': 'system.tab.mailbox',
  'component': 'seip-mailbox',
  'module': 'idoc/system-tabs/mailbox/mailbox',
  filter: function(data) {
    // Allow email box tab to be included only in objects instances which support emails or in the current logged in
    // user dashboard.
    if (!data.currentUser.mailboxSupportable) {
      return false;
    }
    if (data.currentObject) {
      var objectModel = data.currentObject.getModels();
      var emailAddressProp = objectModel.validationModel['emailAddress'];
      if(!emailAddressProp) {
        return false;
      }
      if (data.currentObject.isUser() && data.currentUser.emailAddress !== emailAddressProp.value) {
        return false;
      }
      return !!emailAddressProp.value;
    }
    return false;
  },
  'tabNotificationExtensions': 'mailbox-tab-notification-extensions'
});
