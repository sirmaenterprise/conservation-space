/**
 * Plugin that decorates all a to have target attribute set to _blank
 */
CKEDITOR.plugins.add('default_target', {
  requires: ['link'],
  init: function (editor) {
    editor.filter.addTransformations([
      [
        {
          element: 'a',
          left: function (el) {
            return !el.attributes.target;
          },
          right: function (el, tools) {
            el.attributes.target = '_blank';
          }
        }
      ]
    ]);
  }
});

/* Functionality to have urls in comments to open in new browser window by default*/
CKEDITOR.on('dialogDefinition', function (ev) {
  var dialogName = ev.data.name;
  var dialogDefinition = ev.data.definition;

  /* Make sure that the dialog opened is the link plugin ... otherwise do nothing */
  if (dialogName === 'link') {
    /* Getting the contents of the Target tab */
    var informationTab = dialogDefinition.getContents('target');
    /* Getting the contents of the dropdown field "Target" so we can set it */
    var targetField = informationTab.get('linkTargetType');

    /* Now that we have the field, we just set the default to _blank
     A good modification would be to check the value of the URL field
     and if the field does not start with "mailto:" or a relative path,
     then set the value to "_blank" */
    targetField['default'] = '_blank';
  }
});