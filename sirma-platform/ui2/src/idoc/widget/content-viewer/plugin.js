PluginRegistry.add('idoc-widget', {
  'name': 'content-viewer',
  'class': 'idoc/widget/content-viewer/content-viewer/ContentViewer',
  'config': 'idoc/widget/content-viewer/content-viewer-config/ContentViewerConfig',
  'label': 'content.viewer.widget.name'
});

PluginRegistry.add('media-viewers', {
  'name': 'pdf-viewer',
  'component': 'seip-pdf-viewer',
  'module': 'components/media/pdf-viewer/pdf-viewer',
  'mimetypes': ['application/pdf']
});

PluginRegistry.add('media-viewers', {
  'name': 'video-player',
  'component': 'seip-video-player',
  'module': 'components/media/video-player/video-player',
  'mimetypes': ['video/*']
});

PluginRegistry.add('media-viewers', {
  'name': 'audio-player',
  'component': 'seip-audio-player',
  'module': 'components/media/audio-player/audio-player',
  'mimetypes': ['audio/*']
});

PluginRegistry.add('media-viewers', {
  'name': 'image-viewer',
  'component': 'seip-image-viewer',
  'module': 'components/media/image-viewer/image-viewer',
  'mimetypes': ['image/*']
});
