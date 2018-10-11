PluginRegistry.add('route', {
  'stateName': 'idoc',
  'url': '/idoc/{id}?mode&edit-permissions',
  'component': 'idoc-page',
  'module': 'idoc/idoc-page'

});

PluginRegistry.add('idoc-editor-plugins', {
  data: 'dropdownmenumanager,bootstrapalerts,layoutmanager,image,objectlink,lineheight,undo'
});

PluginRegistry.add('idoc-editor-toolbar-clipboard', {
  data: {
    items: [{
      name: 'Paste',
      command: 'paste',
      label: 'idoc.editor.clipboard.paste'
    }, {
      name: 'PasteText',
      label: 'idoc.editor.clipboard.paste.text',
      command: 'pastetext'
    }, {
      name: 'PasteFromWord',
      label: 'idoc.editor.clipboard.paste.from.word',
      command: 'pastefromword'
    }],
    label: {
      text: 'idoc.editor.clipboard',
      visible: false
    },
    iconPath: 'paste'
  }
});

PluginRegistry.add('idoc-editor-toolbar-text-style', {
  data: {
    items: [{
      name: 'Strike',
      label: 'idoc.editor.text.strike',
      command: 'strike'
    }, {
      name: 'Underline',
      label: 'idoc.editor.text.underline',
      command: 'underline'
    }, {
      name: 'Subscript',
      label: 'idoc.editor.text.subscript',
      command: 'subscript'
    }, {
      name: 'Superscript',
      label: 'idoc.editor.text.superscript',
      command: 'superscript'
    }, {
      name: 'RemoveFormat',
      label: 'idoc.editor.text.remove',
      command: 'removeFormat'
    }],
    label: {
      text: 'idoc.editor.text',
      visible: false
    },
    iconPath: 'strike'
  }
});

PluginRegistry.add('idoc-editor-toolbar-justify', {
  data: {
    items: [{
      name: 'Outdent',
      label: 'idoc.editor.outdent',
      command: 'outdent'
    }, {
      name: 'Indent',
      label: 'idoc.editor.indent',
      command: 'indent'
    }, {
      name: 'JustifyLeft',
      label: 'idoc.editor.justify.left',
      command: 'justifyleft'
    }, {
      name: 'JustifyRight',
      label: 'idoc.editor.justify.right',
      command: 'justifyright'
    }, {
      name: 'JustifyCenter',
      label: 'idoc.editor.justify.center',
      command: 'justifycenter'
    }],
    label: {
      text: 'idoc.editor.justify',
      visible: false
    },
    iconPath: 'justifyleft'
  }
});

PluginRegistry.add('idoc-editor-toolbar', {
  data: [
    ['Format', ' ', 'Font', ' ', 'FontSize', 'lineheight', ' ',
      'Undo', 'Redo', '-',
      'Clipboard', 'Replace', '-',
      'Bold', 'Italic', 'TextStyle', '-', 'SimpleCheckBox',
      'TextColor', 'BGColor', '-',
      'NumberedList', 'BulletedList', 'HorizontalRule', 'Blockquote', 'Justify', '-',
      'Image', 'Table', 'PageBreak', 'Info', '-',
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
