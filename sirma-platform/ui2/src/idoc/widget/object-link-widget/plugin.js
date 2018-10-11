PluginRegistry.add('idoc-widget', {
  name: 'object-link',
  class: 'idoc/widget/object-link-widget/object-link-widget/ObjectLinkWidget',
  label: 'editor.object.link.title',
  inline: true,
  // identify that current widget will be triggered outside default widgets location(widgets menu)
  external: true,
  skipConfig: true
});