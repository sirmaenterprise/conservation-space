PluginRegistry.add('editor-content-processors', [{
  'name':'IdocEditorContentProcessor',
  'component': 'IdocEditorContentProcessor',
  'module': 'idoc/editor/content/idoc-editor-content-processor'
}, {
  'name': 'EditorContentImageProcessor',
  'component': 'EditorContentImageProcessor',
  'module': 'idoc/editor/content/editor-content-image-processor'
}, {
  'name': 'EditorContentWidgetProcessor',
  'component': 'EditorContentWidgetProcessor',
  'module': 'idoc/editor/content/editor-content-widget-processor'
}]);
