/**
 *  This plugin adds various custom layouts using twitter bootstrap.
 *  Author: Radoslav Petkov
 **/
'use strict';
CKEDITOR.plugins.add('bootstrapalerts', {
  requires: 'basewidget',
  icons: 'bootstrapalerts',
  init: pluginInit
});

function pluginInit(editor) {
  var bootstapAlerts = new BootstrapAlerts(editor);
  editor.bootstrapalerts = {};
  var name = 'info';
  var def = {
    extend: {
      init: function () {

      }
    },
    upcast: function (element) {
      if (element.hasClass('info-widget')) {
        return true;
      }
    },
    allowedContent: 'p a div span h2 h3 h4 h5 h6 section article iframe object embed strong b i em cite pre blockquote small sub sup code ul ol li dl dt dd table thead tbody th tr td img caption mediawrapper br[href,src,target,width,height,colspan,span,alt,name,title,class,id,data-options]{text-align,float,margin}(*)',
    configuration: {
      init: {
        blockEvents: false,
        configToolbar: {
          defaultButtons: {
            edit: {
              onClick: function () {
                bootstapAlerts.editor.bootstrapalerts.selectedWidget = this;
                bootstapAlerts.editor.execCommand('bootstrapalertsEditDialog');
              },
              buttonLabel: CKEDITOR.lang[editor.langCode].edit
            }
          }
        },
        onDestroy: function () {

        }
      }
    },
    editables: {
      contentfield: {
        selector: '.bootstrapalert-widget-editable',
        allowedContent: ''
      }
    }
  };

  CKEDITOR.basewidget.addWidget(editor, name, def);

  editor.ui.addButton('Info', {
    icon: 'bootstrapalerts',
    title: CKEDITOR.lang[editor.langCode].infoWidget,
    command: 'bootstrapalertsAddDialog'
  });


  editor.addCommand('bootstrapalertsAddDialog', new CKEDITOR.dialogCommand(bootstapAlerts.addDialog()));
  editor.addCommand('bootstrapalertsEditDialog', new CKEDITOR.dialogCommand(bootstapAlerts.editDialog()));

}

var alertTypes = ['alert-info', 'alert-warning', 'alert-success', 'alert-danger'];
var glyphicon = {
  'alert-info': 'glyphicon glyphicon-info-sign',
  'alert-warning': 'glyphicon glyphicon-warning-sign',
  'alert-success': 'glyphicon glyphicon-ok-sign',
  'alert-danger': 'glyphicon glyphicon-remove-sign'
};
var classes = {
  'alert-info': 'info-widget-info',
  'alert-warning': 'info-widget-warning',
  'alert-success': 'info-widget-success',
  'alert-danger': 'info-widget-danger'
};

function BootstrapAlerts(editor) {
  this.editor = editor;
}


BootstrapAlerts.prototype.createButton = function (type, width) {
  return {
    type: 'html',
    id: type,
    html: '<div class="alert ' + type + ' bootstrapalerts-dialog-typebutton" role="button" style=width:50px;"></div>', //todo name of class selector
    alertType: type
  }
};

BootstrapAlerts.prototype.editDialogContent = function (editor) {
  var typeButtons = [];
  for (var i = 0; i < alertTypes.length; i++) {
    typeButtons.push(this.createButton(alertTypes[i]));
  }
  for (var i = 0; i < typeButtons.length; i++) {
    typeButtons[i].onClick = function () {
      var widget = editor.bootstrapalerts.selectedWidget;
      //change alert type
      widget.element.$.className = widget.element.$.className.replace(/info-widget-.+?\b/g, classes[this.alertType]);
      widget.element.$.setAttribute('data-cke-widget-data', widget.element.$.getAttribute('data-cke-widget-data').replace(/alert-.+?\b/g, this.alertType));
      //change glyphicon
      widget.element.$.firstChild.className = glyphicon[this.alertType] + ' info-widget-icon';

      this._.dialog.hide();
    };
  }

  return typeButtons;
};

BootstrapAlerts.prototype.editDialog = function () {
  var _this = this;
  // CKEditor stores all dialog in the dom despite the editor instance using it.
  // This might result it collision when calling the right dialog for the coresponding editor instance.
  var id = 'editDialog';
  CKEDITOR.dialog.add(id, function (editor) {
    return {
      title: CKEDITOR.lang[editor.langCode].infoWidgetEdit,
      id: id,
      buttons: [CKEDITOR.dialog.cancelButton],
      width: '100',
      resizable: CKEDITOR.DIALOG_RESIZE_NONE,
      contents: [{
        elements: [{
          type: 'hbox',
          children: _this.editDialogContent(editor)
        }]
      }],
      nockereset: true
    };
  });
  return id;
};

BootstrapAlerts.prototype.addDialogContent = function (editor) {
  var typeButtons = [];
  for (var i = 0; i < alertTypes.length; i++) {
    typeButtons.push(this.createButton(alertTypes[i]));
  }
  for (var i = 0; i < typeButtons.length; i++) {
    typeButtons[i].onClick = function () {
      var widgetHtml = ''
        .concat('<div class=" ')
        .concat(classes[this.alertType])
        .concat(' info-widget">')
        .concat('<span class="info-widget-icon ')
        .concat(glyphicon[this.alertType])
        .concat(' ">&nbsp;')
        .concat('</span><div class="bootstrapalert-widget-editable"></div></div>');
      editor.insertHtml(widgetHtml);
      this._.dialog.hide();
    };
  }
  return typeButtons;
};

BootstrapAlerts.prototype.addDialog = function () {
  var _this = this;
  var id = 'addDialog';
  CKEDITOR.dialog.add(id, function (editor) {
    return {
      title: CKEDITOR.lang[editor.langCode].infoWidgetSelect,
      id: id,
      buttons: [CKEDITOR.dialog.cancelButton],
      width: '100',
      resizable: CKEDITOR.DIALOG_RESIZE_NONE,
      contents: [{
        elements: [{
          type: 'hbox',
          id: ('row1'),
          children: _this.addDialogContent(editor)
        }]
      }],
      nockereset: true
    };
  });
  return id;
};
