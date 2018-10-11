/**
 * Created by radoslav on 7/30/2015.
 * Extended version of @link https://github.com/ckeditor/ckeditor-dev/blob/master/plugins/format/plugin.js
 * All operations go through commands.
 * Disabled commands are not rendered when the menu is opened.
 */
CKEDITOR.plugins.add('formatcommand', {
  requires: 'richcombo',
  lang: 'af,ar,bg,bn,bs,ca,cs,cy,da,de,el,en,en-au,en-ca,en-gb,eo,es,et,eu,fa,fi,fo,fr,fr-ca,gl,gu,he,hi,hr,hu,id,is,it,ja,ka,km,ko,ku,lt,lv,mk,mn,ms,nb,nl,no,pl,pt,pt-br,ro,ru,si,sk,sl,sq,sr,sr-latn,sv,th,tr,tt,ug,uk,vi,zh,zh-cn', // %REMOVE_LINE_CORE%
  init: pluginInit
});

function createStyleCommand(name, style, editor) {
  var tokens = name.split('_');
  if (tokens[0] == 'apply') {
    var styleCommand = new CKEDITOR.styleCommand(style);
    editor.addCommand(name, styleCommand);
  } else {
    editor.addCommand(name, {
      exec: function () {
        editor.removeStyle(style);
      }
    })
  }
}

function pluginInit(editor) {
  if (editor.blockless)
    return;

  var config = editor.config,
    lang = editor.lang.format;

  // Gets the list of tags from the settings.
  var tags = config.format_tags.split(';');

  // Create style objects for all defined styles.
  var styles = {},
    stylesCount = 0,
    allowedContent = [];
  for (var i = 0; i < tags.length; i++) {
    var tag = tags[i];
    var style = new CKEDITOR.style(config['format_' + tag]);
    createStyleCommand('apply_' + tag, style, editor);
    createStyleCommand('remove_' + tag, style, editor);
    if (!editor.filter.customConfig || editor.filter.check(style)) {
      stylesCount++;
      styles[tag] = style;
      styles[tag]._.enterMode = editor.config.enterMode;
      allowedContent.push(style);
    }
  }

  // Hide entire combo when all formats are rejected.
  if (stylesCount === 0)
    return;

  editor.ui.addRichCombo('Format', {
    label: lang.label,
    title: lang.panelTitle,
    toolbar: 'styles,20',
    allowedContent: allowedContent,

    panel: {
      css: [CKEDITOR.skin.getPath('editor')].concat(config.contentsCss),
      multiSelect: false,
      attributes: {'aria-label': lang.panelTitle}
    },

    init: function () {
      this.startGroup(lang.panelTitle);

      for (var tag in styles) {
        var label = lang['tag_' + tag];

        // Add the tag entry to the panel list.
        this.add(tag, styles[tag].buildPreview(label), label);
      }
    },

    onClick: function (value) {
      editor.focus();
      editor.fire('saveSnapshot');
      var elementPath = editor.elementPath();
      var style = styles[value];

      if (style.checkActive(elementPath, editor)) {
        editor.execCommand('remove_' + value);
      } else {
        editor.execCommand('apply_' + value);
      }

      // Save the undo snapshot after all changes are affected. (#4899)
      setTimeout(function () {
        editor.fire('saveSnapshot');
      }, 0);
    },

    onRender: function () {
      editor.on('selectionChange', function (ev) {
        var currentTag = this.getValue(),
          elementPath = ev.data.path;

        this.refresh();

        for (var tag in styles) {
          if (styles[tag].checkActive(elementPath, editor)) {
            if (tag != currentTag)
              this.setValue(tag, editor.lang.format['tag_' + tag]);
            return;
          }
        }

        // If no styles match, just empty it.
        this.setValue('');

      }, this);
    },

    onOpen: function () {
      this.showAll();
      for (var name in styles) {
        var style = styles[name];

        // Check if that style is enabled in activeFilter or the command is active.
        if (!editor.activeFilter.check(style) || editor.getCommand('apply_' + name).state == CKEDITOR.TRISTATE_DISABLED || editor.getCommand('remove_' + name).state == CKEDITOR.TRISTATE_DISABLED) {
          this.hideItem(name);
        }
      }
    },

    refresh: function () {
      var elementPath = editor.elementPath();

      if (!elementPath)
        return;

      // Check if element path contains 'p' element.
      if (!elementPath.isContextFor('p')) {
        this.setState(CKEDITOR.TRISTATE_DISABLED);
        return;
      }

      // Check if there is any available style.
      for (var name in styles) {
        if (editor.activeFilter.check(styles[name]))
          return;
      }
      this.setState(CKEDITOR.TRISTATE_DISABLED);
    }
  });

}