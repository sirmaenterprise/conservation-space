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
      'NumberedList', 'BulletedList'
    ]
  ],
  order: 0
});