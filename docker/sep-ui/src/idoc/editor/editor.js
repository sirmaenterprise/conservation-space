import application from 'app/app';
import {Injectable, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {WidgetRemovedEvent, NEW_ATTRIBUTE} from 'idoc/widget/widget';
import {PluginsService} from 'services/plugin/plugins-service';
import {NavigatorAdapter} from 'adapters/navigator-adapter';

import 'common/lib/ckeditor/ckeditor';
import $ from 'jquery';
import './editor.css!css';
import 'idoc/widget/info-widget/info-widget.css!css';
import 'idoc/widget/layout-widget/layout-widget.css!css';

// no config.js
CKEDITOR.config.customConfig = false;
// no styles.js
CKEDITOR.config.stylesSet = false;
// default language
CKEDITOR.config.defaultLanguage = 'en';
// enables native browser spell check(until we find the problem with ours)
CKEDITOR.config.disableNativeSpellChecker = false;
// set custom skin
CKEDITOR.config.skin = 'seip';
// set max size of inserted images to FullHD (1920x1080 px)
CKEDITOR.config.imageResize = {
  maxWidth: 1920,
  maxHeight: 1080
};
CKEDITOR.config.magicline_triggerOffset = 5;
// prevent auto-init of CKEDITOR on contenteditable fields
CKEDITOR.disableAutoInline = true;

// Set font name and size labels. This do NOT affect real font and size just sets the default display value of the style box
let fontFamily = $('body').css('font-family');
CKEDITOR.config.font_defaultLabel = fontFamily.substr(0, fontFamily.indexOf(',')).replace(/"/g, '');
// Detect what is the global font-size, remove the px suffix and add it as default label
CKEDITOR.config.fontSize_defaultLabel = $('body').css('font-size').slice(0, -2);
CKEDITOR.config.font_names = 'Open Sans*/Open Sans;Arimo*/Arimo;Caladea*/Caladea;Carlito*/Carlito;Cousine*/Cousine;Tinos*/Tinos;Arial/Arial, Arimo, Helvetica, sans-serif;Comic Sans MS/Comic Sans MS, cursive;Courier New/Courier New, Cousine, Courier, monospace;Georgia/Georgia, serif;Lucida Sans Unicode/Lucida Sans Unicode, Lucida Grande, sans-serif;Tahoma/Tahoma, Geneva, sans-serif;Times New Roman/Times New Roman, Tinos, Times, serif;Trebuchet MS/Trebuchet MS, Helvetica, sans-serif;Verdana/Verdana, Geneva, sans-serif';
CKEDITOR.lang.languages = {'bg': 1, 'de': 1, 'en': 1, 'da': 1};
CKEDITOR.filter.allowedContent = 'span[!style]';
CKEDITOR.dtd.$removeEmpty['span'] = 0;

if (NavigatorAdapter.isFirefox()) {
  CKEDITOR.plugins.clipboard.isCustomCopyCutSupported = true;
}

var widgets = PluginRegistry.get('idoc-widget') || [];

registerWidgets(CKEDITOR, widgets);

@Injectable()
@Inject(TranslateService, PluginsService)
export class IdocEditorFactory {

  constructor(translateService, pluginsService) {
    this.translateService = translateService;
    this.pluginsLoader = pluginsService.loadPluginServiceModules('editor-plugins', 'name');
  }

  init(element, editMode) {
    let toolbarConfig = PluginRegistry.get('idoc-editor-toolbar')[0].data;
    let clipboardMenu = PluginRegistry.get('idoc-editor-toolbar-clipboard')[0].data;
    let textStyleMenu = PluginRegistry.get('idoc-editor-toolbar-text-style')[0].data;
    let justifyMenu = PluginRegistry.get('idoc-editor-toolbar-justify')[0].data;
    let formatTags = PluginRegistry.get('idoc-editor-format_tags')[0].data;
    let widgetCommands = [];

    var translateService = this.translateService;
    this.translatePlugin(clipboardMenu);
    this.translatePlugin(justifyMenu);
    this.translatePlugin(textStyleMenu);

    widgets.sort((firstWidget, secondWidget) => {
      let firstWidgetName = this.translateService.translateInstant(firstWidget.label);
      let secondWidgetName = this.translateService.translateInstant(secondWidget.label);
      return firstWidgetName > secondWidgetName;
    });

    widgets.forEach(function (widget, index) {
      widgetCommands.push({
        name: widget.name,
        label() {
          return translateService.translate(widget.label);
        },
        command: widget.name,
        order: index,
        external: widget.external
      });
    });

    return this.pluginsLoader.then(() => {
      return CKEDITOR.inline(element, {
        language: this.translateService.getCurrentLanguage(),
        line_height: `(${this.translateService.translateInstant('editor.line.height.default')})/1.42857;1.15/1.64286;1.5/2.14286;2/2.85714;`,
        colorButton_foreStyle: {
          element: 'span',
          styles: {'color': '#(color)', '-webkit-text-fill-color': '#(color)'},
          overrides: [{element: 'font', attributes: {'color': null}}]
        },
        colorButton_backStyle: {
          element: 'span',
          styles: {'background-color': '#(color) !important'},
          overrides: {element: 'span'}
        },
        image_previewText: ' ',
        title: false,
        allowedContent: true,
        pasteFromWordFilters: getPasteFromWordFilters(),
        removePlugins: getEditorExcludedPlugins(editMode),
        extraPlugins: getEditorPlugins(editMode),
        toolbar: toolbarConfig,
        format_tags: formatTags,
        magicline_tabuList: ['magic-line-disabled'],
        qtWidth: '95%',
        qtStyle: {'table-layout': 'fixed'},
        enableTabKeyTools: true,
        //the native browser spell checker is better, because it supports more languages
        disableNativeSpellChecker: false,
        sharedSpaces: {
          top: 'idoc-editor-toolbar'
        },
        dropdownmenumanager: {
          'Checklist': {
            items: [],
            iconPath: '/images/editor-toolbar-icons/checklist.png'
          },
          'ObjectPicker': {
            items: [],
            iconPath: '/images/editor-toolbar-icons/objectpicker.png'
          },
          'Widgets': {
            items: widgetCommands.filter((command) => {
              return !command.external;
            }),
            label: {
              text: this.translateService.translateInstant('topmenu.insert.widget.label')
            },
            iconPath: window.CKEDITOR_BASEPATH.replace('common/lib/ckeditor/', '') + 'images/editor-toolbar-icons/widgets.png'
          },
          'Clipboard': clipboardMenu,
          'TextStyle': textStyleMenu,
          'Justify': justifyMenu
        }
      });
    });
  }

  translatePlugin(plugin) {
    plugin.items.forEach((item) => {
      item.label = this.translateService.translateInstant(item.label);
    });
    plugin.label.text = this.translateService.translateInstant(plugin.label.text);
  }

  destroy(editor) {
    $(editor.element.$).off();
    $(editor.element.$).contents().off();
    $(editor.element.getElementsByTag('img').$).each(() => {
      let img = $(this);
      if (img.lazyload) {
        img.lazyload.destroy();
      }
    });

    // Indicates that the editor instance has been deactivated by the specified element which has just lost focus.
    // Used the boolean parameter to deactivate immediately.
    if (editor.focusManager) {
      editor.focusManager.blur(true);
    }

    // manually remove listeners and custom data as they are not properly destroyed by CKEditor
    editor.removeAllListeners();
    editor.element.removeAllListeners();
    if (editor.document) {
      editor.document.clearCustomData();
    }

    if (editor.window) {
      editor.window.clearCustomData();
    }

    if (editor.container) {
      editor.container.clearCustomData();
    }

    if (editor.filter) {
      editor.filter.destroy();
    }

    // manually cleanup dropdownmenumanager objects as they retain in memory
    if (editor.plugins && editor.plugins.dropdownmenumanager && (typeof editor.plugins.dropdownmenumanager.destroy === 'function')) {
      editor.plugins.dropdownmenumanager.destroy();
    }

    if (editor.plugins && editor.plugins.pastebase64 && (typeof editor.plugins.pastebase64.destroy === 'function')) {
      editor.plugins.pastebase64.destroy();
    }

    // manually cleanup widget as they retain dom objects
    if (editor.widgets) {
      // Deregister deleted widgets from the editor
      editor.widgets.checkWidgets();
      editor.widgets.removeAllListeners();
      editor.widgets.destroyAll();
    }

    if (editor.commands) {
      for (let command in editor.commands) {
        if (editor.commands.hasOwnProperty(command)) {
          editor.commands[command].removeAllListeners();
        }
      }

    }

    // Because the instance is replacing a DOM element, this parameter indicates not to update the element with the instance content.
    editor.destroy(false);

    // prevents a lot of memory retention
    // manually cleanup toolbar shared space
    $(`#cke_${editor.name}`).remove();

    // prevents a lot of memory retention
    Object.keys(editor).forEach(function (key) {
      delete editor[key];
    });

    editor = null;
  }
}

/**
 *  Creates a string of the plugins to be excluded by the idoc editor.
 *
 *  @param editMode
 *                a boolean indicating of the editor is currently in edit mode
 */
function getEditorExcludedPlugins(editMode) {
  //when in preview mode, we want the native browser context menu, so we exclude the CKeditor's plugins overriding it
  return editMode ? 'resize,elementspath,format,tableresize' : 'resize,elementspath,format,liststyle,tabletools,contextmenu,sesTableresize,tableresize,pastebase64,tableselection';
}

/**
 * Creates a CSV string of the plugins to be loaded by the idoc editor. The list is created by: <default plugins> +
 * <extra plugins added by extension point> + <all widgets>.
 */
function getEditorPlugins(editMode) {
  var plugins = editMode ? 'sharedspace,magicline,justify,formatcommand,quicktable,sesTableresize,tableselection,pastebase64,draganddroptable,tablearrowsmovement' : '';

  var extraPlugins = PluginRegistry.get('idoc-editor-plugins');
  if (extraPlugins && extraPlugins[0].data) {
    plugins += ',' + extraPlugins[0].data;
  }

  widgets.forEach(function (widget) {
    plugins = plugins + ',' + widget.name;
  });

  return plugins;
}

function registerWidgets(CKEDITOR, widgets) {

  widgets.forEach(function (widget) {
    var inline = widget.inline ? widget.inline : false;

    var wrapperType = inline ? 'span' : 'div';
    CKEDITOR.plugins.add(widget.name, {
      requires: 'basewidget',

      init(editor) {
        CKEDITOR.basewidget.addWidget(editor, widget.name, {
          template: '<' + wrapperType + ' widget="' + widget.name + '" class="widget ' + widget.name + '" ' + NEW_ATTRIBUTE + '="true" editor="::editor">' + '</' + wrapperType + '>',

          inline,
          draggable: !inline,

          // enables the editor to recognize that the element is a widget and to process and compile it
          upcast(element) {
            return element.attributes['widget'] === widget.name;
          },

          extend: {
            init() {
              var element = this.element.$;

              element.parentNode.classList.add('widget-wrapper');

              // temporarily remove data-widget attribute sets by CKEditor before $compile()
              // because angular thinks that data-widget is the same directive as widget
              // and applies the directive twice
              var dataWidget = element.getAttribute('data-widget');
              element.removeAttribute('data-widget');
              // use the scope of the closest parent that has a scope
              var editorScope = $(editor.container.$).closest('.idoc-editor').isolateScope();
              var compiledElement = application.$compile(element)(editorScope);
              // $compile replaces the compiled element (instead of only decorating it)
              // and this breaks the base CKEditor widget (this.element doesn't point to the
              // correct element after $compile
              this.element = new CKEDITOR.dom.node(compiledElement[0]);
              this.element.$.setAttribute('data-widget', dataWidget);
            }
          },
          configuration: {
            init: {
              blockEvents: true,
              configToolbar: false,
              onDestroy() {
                var element = $(this.element.$);
                // onDestroy doesn't get called when the widget is removed using DOM operation (i.e. physically
                // removing the element) but gets called later when CKEditor.checkWidgets() gets called (i.e. when
                // a widget DnD is performed) and at this point the element doesn't exist and doesn't have an associated
                // angular scope
                var elementScope = element.scope();
                if (elementScope) {
                  elementScope.$broadcast(WidgetRemovedEvent.EVENT_NAME);
                }
              }
            }
          }
        });
      }
    });
  });
}

function getPasteFromWordFilters() {
  return {
    'table': {
      'width': '100%',
      'table-layout': 'fixed',
      'background': null,
      'background-color': null,
      'padding': null
    }
  };
}
