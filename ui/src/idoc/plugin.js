PluginRegistry.add('route', {
  'stateName': 'idoc',
  'url': '/idoc/{id}?mode&edit-permissions',
  'component': 'idoc-page',
  'module': 'idoc/idoc-page'

});

PluginRegistry.add('idoc-editor-plugins', {
  data: 'dropdownmenumanager,bootstrapalerts,layoutmanager,image,objectlink'
});

PluginRegistry.add('idoc-editor-toolbar-clipboard', {
  data: {
    items: [{
      name: 'Paste',
      command: 'paste'
    }, {
      name: 'PasteText',
      label: 'Paste as text',
      command: 'pastetext'
    }, {
      name: 'PasteFromWord',
      label: 'Paste from word',
      command: 'pastefromword'
    }, {
      name: 'PasteFromSep',
      label: 'Paste from another SEP',
      command: 'pastefromsep'
    }],
    label: {
      text: 'Clipboard',
      visible: false
    },
    iconPath: 'paste'
  }
});

PluginRegistry.add('idoc-editor-toolbar-text-style', {
  data: {
    items: [{
      name: 'Strike',
      command: 'strike'
    }, {
      name: 'Underline',
      command: 'underline'
    }, {
      name: 'Subscript',
      command: 'subscript'
    }, {
      name: 'Superscript',
      command: 'superscript'
    }, {
      name: 'RemoveFormat',
      label: 'Remove format',
      command: 'removeFormat'
    }],
    label: {
      text: 'TextStyle',
      visible: false
    },
    iconPath: 'strike'
  }
});

PluginRegistry.add('idoc-editor-toolbar-justify', {
  data: {
    items: [{
      name: 'Outdent',
      command: 'outdent'
    }, {
      name: 'Indent',
      command: 'indent'
    }, {
      name: 'JustifyLeft',
      label: 'Justify left',
      command: 'justifyleft'
    }, {
      name: 'JustifyRight',
      label: 'Justify right',
      command: 'justifyright'
    }, {
      name: 'JustifyCenter',
      label: 'Justify center',
      command: 'justifycenter'
    }],
    label: {
      text: 'Justify',
      visible: false
    },
    iconPath: 'justifyleft'
  }
});

PluginRegistry.add('idoc-editor-toolbar', {
  data: [
    ['Format', ' ', 'Font', ' ', 'FontSize', ' ',
      'Undo', 'Redo', '-',
      'Clipboard', 'Replace', '-',
      'Bold', 'Italic', 'TextStyle', '-', 'SimpleCheckBox',
      'TextColor', 'BGColor', '-',
      'NumberedList', 'BulletedList', 'HorizontalRule', 'Blockquote', 'Justify', '-',
      'Image', 'Table', 'SpecialChar', 'Info', '-',
      'AddLayout', '-',
      'Widgets', '-',
      'Link', '-', 'Objectlink']],
  order: 0
});

PluginRegistry.add('idoc-editor-format_tags', {
  data: 'p;h1;h2;h3;h4;h5;h6',
  order: 0
});

PluginRegistry.add('idoc-comment-editor-toolbar', {
  data: [
    ['FontSize', '-',
      'Undo', 'Redo', '-',
      'Clipboard', '-',
      'Bold', 'Italic', 'TextStyle', '-',
      'TextColor', 'BGColor', '-',
      'NumberedList', 'BulletedList', 'Checklist', 'Justify', '-',
      'AddLayout', 'Blockquote', '-',
      'Link'
    ]],
  order: 0
});

PluginRegistry.add('help-request-editor-toolbar', {
  data: [
    ['Format', ' ', 'Font', ' ', 'FontSize', ' ',
      'Undo', 'Redo', '-',
      'Clipboard', '-',
      'Bold', 'Italic', 'TextStyle', '-', 'SimpleCheckBox',
      'TextColor', 'BGColor', '-',
      'NumberedList', 'BulletedList', 'HorizontalRule', 'Blockquote', 'Justify', '-',
      'Image', 'Table', 'SpecialChar', 'Info', '-',
      'AddLayout', '-',
      'Widgets', '-',
      'Link', 'Replace']],
  order: 0
});