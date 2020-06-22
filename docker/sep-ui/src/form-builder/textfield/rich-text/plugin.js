PluginRegistry.add('form-control', {
  'name': 'seip-rich-text',
  'component': 'seip-rich-text',
  'type': 'RICHTEXT',
  'module': 'form-builder/textfield/rich-text/rich-text'
});

PluginRegistry.add('richtext-field-editor-toolbar', {
  data: [
    [
      'FontSize', '-',
      'Bold', 'Italic', 'TextStyle', '-',
      'TextColor', 'BGColor', '-',
      'NumberedList', 'BulletedList', '-',
      'Link'
    ]
  ],
  order: 0
});

PluginRegistry.add('richtext-field-text-style-config', {
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