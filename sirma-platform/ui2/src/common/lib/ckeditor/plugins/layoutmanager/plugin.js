/**
 *  This plugin adds various custom layouts using twitter bootstrap grid system.
 *  Author: Radoslav Petkov
 **/
'use strict';

CKEDITOR.plugins.add('layoutmanager', {
  requires: 'basewidget',
  icons: 'addlayout',
  lang: 'en,bg,de,fi,da',
  init: pluginInit
});

/**
 * Config variables
 * config.layoutmanager_loadbootstrap   By default is set to false, otherwise loads the embedded bootstrap style.
 * config.layoutmanager_allowedContent  By default is set to allow all tags.
 * config.layoutmanager_buttonBoxWidth  The width of each layout-preview button in the dialog.
 * config.layoutmanager_removeLayoutMsg The message displayed on the window for confirmation of the remove layout operation.
 */
function pluginInit(editor) {
  if (editor.config.layoutmanager_loadbootstrap) {
    if (typeof editor.config.contentsCss === 'object') {
      editor.config.contentsCss.push(CKEDITOR.getUrl(this.path + 'css/bootstrap.css'));
    } else {
      editor.config.contentsCss = [CKEDITOR.getUrl(this.path + 'css/bootstrap.css')];
    }
  }

  if (typeof editor.config.contentsCss === 'object') {
    editor.config.contentsCss.push(CKEDITOR.getUrl(this.path + 'css/style.css'));
  } else {
    editor.config.contentsCss = [CKEDITOR.getUrl(this.path + 'css/style.css')];
  }

  var layoutManager = new LayoutManager(editor);
  layoutManager.generateWidgets();

  //Creates local namespace for the plugin to share data.
  editor.layoutmanager = {};

  var allowedContent;
  if (editor.config.layoutmanager_allowedContent) {
    allowedContent = editor.config.layoutmanager_allowedContent();
  } else {
    allowedContent = 'p a div span h2 h3 h4 h5 h6 section article iframe object embed strong b i em cite pre blockquote small sub sup code ul ol li dl dt dd table thead tbody th tr td img caption mediawrapper br[href,src,target,width,height,colspan,span,alt,name,title,class,id,data-options]{text-align,float,margin}(*)';
  }
  layoutManager.setAllowedContent(allowedContent);

  var addLayoutDialog = layoutManager.addLayoutDialog.bind(layoutManager);
  var manageLayoutDialog = layoutManager.manageLayoutDialog.bind(layoutManager);

  CKEDITOR.dialog.add('addLayoutDialog', function (editor) {
    return addLayoutDialog(editor);
  });
  CKEDITOR.dialog.add('manageLayoutDialog', function (editor) {
    return manageLayoutDialog(editor);
  });

  editor.addCommand('addlayout', {
    exec: function (editor) {
      editor.openDialog('addLayoutDialog');
    }
  });

  editor.addCommand('managelayout', {
    exec: function (editor, data) {
      editor.openDialog('manageLayoutDialog');
    }
  });

  editor.ui.addButton('AddLayout', {
    title: editor.lang.layoutmanager.title,
    command: 'addlayout'
  });
}

/**
 *  LayoutManager class implementing all layout functionalities.
 *  Author: Radoslav Petkov
 *
 *  Variables stored into the editor's object:
 *  {ckeditor.dom.element} editor.layoutmanager.selectedLayout.wrappet The selected widget wrapper element.
 *  {ckeditor.dom.element} editor.layoutmanager.selectedLayout.widget The selected widget instance.
 */
function LayoutManager(editor) {
  this.editor = editor;
  this.layoutBuilder = new LayoutBuilder();
  if (!editor.config.layoutmanager_buildDefaultLayouts) {
    this.layoutBuilder.buildDefaultLayouts();
  }
}

LayoutManager.prototype.setAllowedContent = function (allowedContent) {
  this.allowedContent = allowedContent;
};

LayoutManager.prototype.insertLayoutAction = function (editor, name) {
  editor.execCommand(name);
  editor.fire('layoutmanager:layout-inserted');
};

LayoutManager.prototype.addLayoutDialog = function (editor) {
  return new DialogBuilder().addLayoutDialog(editor, this.insertLayoutAction.bind(this, editor));
};

LayoutManager.prototype.manageLayoutDialog = function (editor) {
  return new DialogBuilder().manageLayoutDialog(editor, this.changeLayoutAction.bind(this, editor));
};

LayoutManager.prototype.createWidgetDefinition = function (_template, _editables, _upcast, _allowedContent) {
  return {
    template: _template,
    extend: {
      init: function () {

      }
    },
    configuration: {
      init: {
        blockEvents: false,
        configToolbar: {
          defaultButtons: {
            edit: {
              onClick: function () {
                this.editor.layoutmanager.selectedWidget = this;

                this.editor.execCommand('managelayout', this);
              },
              buttonLabel: CKEDITOR.lang[this.editor.langCode].edit
            }
          }
        },
        onDestroy: function () {

        }
      }
    },
    editables: _editables,
    upcast: _upcast,
    allowedContent: _allowedContent
  };
};

LayoutManager.prototype.generateWidgets = function () {
  var layouts = this.layoutBuilder.getLayouts();
  this.editables = {
    layoutColumn1: {
      selector: '.layout-column-one',
      allowedContent: this.allowedContent
    },
    layoutColumn2: {
      selector: '.layout-column-two',
      allowedContent: this.allowedContent
    },
    layoutColumn3: {
      selector: '.layout-column-three',
      allowedContent: this.allowedContent
    }
  };
  var upcast = function (element) {
    return (element.hasClass('layout-container'));
  };

  for (var type in layouts) {
    if (layouts.hasOwnProperty(type)) {
      var widgetDefinition = this.createWidgetDefinition(layouts[type].template, this.editables, upcast, this.allowedContent);
      this.createWidget(type, widgetDefinition);
    }
  }
};

LayoutManager.prototype.removeLayoutWidget = function () {
  this.editor.layoutmanager.selectedLayout.widget.repository.del(this.editor.layoutmanager.selectedLayout.widget);
};

LayoutManager.prototype.changeLayoutAction = function (editor, newLayoutName) {
  var selectedWidget = editor.layoutmanager.selectedWidget;
  selectedWidget.name = newLayoutName;

  var currentlySelectedLayoutWidgetElement = selectedWidget.element;
  var $row = $(selectedWidget.element.$).find('.layout-row');
  var row = currentlySelectedLayoutWidgetElement.getChildren().getItem(0).getChildren().getItem(0);
  var $columns = $row.find('.layout-column');
  var columns = row.getChildren();
  var columnsCount = $columns.length;

  var newColumns = newLayoutName.split('/');
  var newColumnsCount = newColumns.length;

  var attributeTemplate = [];
  attributeTemplate.push('col-xs-{size}');
  attributeTemplate.push('col-sm-{size}');
  attributeTemplate.push('col-md-{size}');
  attributeTemplate.push('col-lg-{size}');
  var pattern = /col-(xs|sm|md|lg)-/;
  var dict = ['one', 'two', 'three'];

  if (newColumnsCount <= columnsCount) {
    for (var i = 0; i < newColumnsCount; i++) {
      var $currentColumn = $($columns[i]);
      var attributeString = $currentColumn.attr('class');
      var attributes = attributeString.split(' ');
      for (var j = 0; j < attributes.length; j++) {
        if (pattern.test(attributes[j])) {
          $currentColumn.removeClass(attributes[j]);
        }
      }
      for (var j = 0; j < attributeTemplate.length; j++) {
        $currentColumn.addClass(attributeTemplate[j].replace('{size}', newColumns[i]));
      }
    }
    for (var i = columnsCount - 1; i >= newColumnsCount; i--) {
      var lastColumn = $($columns[i]);
      var newLast = $($columns[i - 1]);
      var editableLast = lastColumn.find('.layout-column-editable');
      editableLast.removeAttr('class');
      editableLast.removeAttr('contentEditable');
      editableLast.removeAttr('data-cke-widget-editable');
      editableLast.removeAttr('data-enter-mode');
      editableLast.appendTo($(newLast.find('.layout-column-' + dict[i - 1])));
      $(document).ready(function () {
        lastColumn.remove();
      });

    }
  } else {
    for (var i = 0; i < columnsCount; i++) {
      var $currentColumn = $($columns[i]);
      var attributeString = $currentColumn.attr('class');
      var attributes = attributeString.split(' ');
      for (var j = 0; j < attributes.length; j++) {
        if (pattern.test(attributes[j])) {
          $currentColumn.removeClass(attributes[j]);
        }
      }
      for (var j = 0; j < attributeTemplate.length; j++) {
        $currentColumn.addClass(attributeTemplate[j].replace('{size}', newColumns[i]));
      }
    }


    for (var i = columnsCount; i < newColumnsCount; i++) {
      var $newCol = $('<div>').addClass('layout-column');
      for (var j = 0; j < attributeTemplate.length; j++) {
        $newCol.addClass(attributeTemplate[j].replace('{size}', newColumns[i]));
      }
      // Generate unique id for the editable
      // Due to this issue http://dev.ckeditor.com/ticket/12524
      var uniqueId = new Date().getTime() + Math.floor((Math.random() * 100 ) + 1);
      var insertedColEditable = $('<div>').addClass('layout-column-editable').addClass('layout-column-' + dict[i]);
      $newCol.append(insertedColEditable);
      $row.append($newCol);
      selectedWidget.initEditable(uniqueId, {
        selector: '.layout-column-' + dict[i]
      });
    }
  }
};

LayoutManager.prototype.createWidget = function (name, definition) {
  CKEDITOR.basewidget.addWidget(this.editor, name, definition);
};


var DialogBuilder = function () {
  this.layoutBuilder = new LayoutBuilder();
  this.layoutBuilder.buildDefaultLayouts();
};


DialogBuilder.prototype.createButton = function (editor, type, action) {
  var cols = type.split("/");
  var button = this.createButtonView(editor, cols.length, cols);

  return {
    type: 'html',
    id: type,
    html: button.html,
    className: " ",
    style: 'width:' + button.width + 'px;',
    onClick: function (evt) {
      action(type);
      this._.dialog.hide();
    }
  };
};

/**
 *  The button's view should be small representation of the actual layout.
 *  In order to accomplish it ckeditor's styles should be overrided by adding hardcoded styles to the elements
 *  such as width,height,border and position.
 *
 *  @param {integer} columns The count of the columns.
 *  @param {integer array}  columnsSizes Holds the size of each column in ratio columnsSizes[i] : 12.
 */
DialogBuilder.prototype.createButtonView = function (editor, columns, columnsSizes) {
  var colWidth = [];
  var boxWidth = 58;
  if (editor.config.layoutmanager_buttonBoxWidth) {
    boxWidth = editor.config.layoutmanager_buttonBoxWidth;
  }
  var seedWidth = ((boxWidth - 2) / 12); // Substracts two pixels for the left and right border
  for (var i = 1; i <= 12; i++) {
    colWidth[i] = (seedWidth * i);
  }
  var html = '<div class="container-fluid">';
  for (var i = 0; i < columns; i++) {
    // If the column is not in the beginning set the border-left to 0.
    // The height of the button is set on 30px.
    html = html.concat('<div style="cursor:pointer;border:1px solid #778899;float:left;position:relative;background:#B0C4DE;text-align:center;height:30px;line-height: 30px;width:' + (colWidth[columnsSizes[i]] - 1) + 'px;' + ((i != 0) ? 'border-left:none' : '') + ' "> ' + '</div>');
  }
  html = html.concat('</div>');

  return {
    html: html,
    width: boxWidth
  }
};

DialogBuilder.prototype.generateButtonObjects = function (editor, action) {
  var layouts = this.layoutBuilder.getLayouts();
  var rows = {};

  for (var type in layouts) {
    if (layouts.hasOwnProperty(type)) {
      var button = this.createButton(editor, type, action);
      if (!rows.hasOwnProperty(layouts[type].rowPosition)) {
        rows[layouts[type].rowPosition] = [];
      }
      rows[layouts[type].rowPosition].push(button);
    }
  }
  return rows;
};

DialogBuilder.prototype.addLayoutDialog = function (editor, action) {
  var width = 200;
  var height = 100;
  var layouts = this.generateButtonObjects(editor, action);

  return this.createDialogDefinition(editor.lang.layoutmanager.addLayoutDialogTitle, width, height, layouts);
};


DialogBuilder.prototype.manageLayoutDialog = function (editor, action) {
  var width = 200;
  var height = 100;
  var layouts = this.generateButtonObjects(editor, action);

  return this.createDialogDefinition(editor.lang.layoutmanager.manageLayoutDialogTitle, width, height, layouts);
};

DialogBuilder.prototype.createButtonObject = function () {
  var layouts = this.layoutBuilder.getLayouts();
  var rows = {};
  for (var type in layouts) {
    if (layouts.hasOwnProperty(type)) {
      var button = this.createButton(editor, type, action);
      if (!rows.hasOwnProperty(layouts[type].rowPosition)) {
        rows[layouts[type].rowPosition] = [];
      }
      rows[layouts[type].rowPosition].push(button);
    }
  }
  return rows;
};

DialogBuilder.prototype.createDialogDefinition = function (title, minWidth, minHeight, buttonsObjects) {
  var elementsRows = [];
  for (var row in buttonsObjects) {
    if (buttonsObjects.hasOwnProperty(row)) {
      elementsRows.push({
        type: 'hbox',
        id: ('row' + row),
        children: buttonsObjects[row]
      });
    }
  }

  return {
    title: title,
    minWidth: minWidth,
    minHeight: minHeight,
    resizable: CKEDITOR.DIALOG_RESIZE_NONE,
    buttons: [CKEDITOR.dialog.cancelButton],
    contents: [{
      elements: elementsRows
    }]
  };
};


/**
 *  Class that builds the default templates of the layouts. It is also used to hold all available
 *  layout templates and provide them for use in the widget creation.
 */
function LayoutBuilder() {
  var defaultLayoutTemplates = [];
  defaultLayoutTemplates.push(new CKEDITOR.template('<div class="layoutmanager">\
        <div class="container-fluid layout-container">\
             <div class="row layout-row" >\
                 <div class="col-xs-{size1} col-sm-{size1} col-md-{size1} col-lg-{size1} layout-column">\
                    <div class="layout-column-one layout-column-editable"><p></p></div>\
                </div>\
            </div>\
        </div>\
    </div>'));
  defaultLayoutTemplates.push(new CKEDITOR.template('<div class="layoutmanager">\
        <div class="container-fluid layout-container">\
            <div class="row layout-row">\
                 <div class="col-xs-{size1} col-sm-{size1} col-md-{size1} col-lg-{size1} layout-column ">\
                     <div class="layout-column-one layout-column-editable"><p></p></div>\
                </div>\
                 <div class="col-xs-{size2} col-sm-{size2} col-md-{size2} col-lg-{size2} layout-column">\
                    <div class="layout-column-two layout-column-editable"><p></p></div>\
                 </div>\
            </div>\
        </div>\
    </div>'));
  defaultLayoutTemplates.push(new CKEDITOR.template('<div class="layoutmanager">\
         <div class="container-fluid layout-container">\
            <div class="row layout-row">\
                <div class="col-xs-{size1} col-sm-{size1} col-md-{size1} col-lg-{size1} layout-column">\
                    <div class="layout-column-one layout-column-editable"><p></p></div>\
                </div>\
                <div class="col-xs-{size2} col-sm-{size2} col-md-{size2} col-lg-{size2} layout-column">\
                    <div class="layout-column-two layout-column-editable"><p></p></div>\
                </div>\
                <div class="col-xs-{size3} col-sm-{size3} col-md-{size3} col-lg-{size3} layout-column">\
                    <div class="layout-column-three layout-column-editable"><p></p></div>\
                </div>\
            </div>\
        </div>\
    </div>'));

  var defaultLayoutTypes = [];
  defaultLayoutTypes.push("12");
  defaultLayoutTypes.push("6/6");
  defaultLayoutTypes.push("9/3");
  defaultLayoutTypes.push("3/9");
  defaultLayoutTypes.push("8/4");
  defaultLayoutTypes.push("4/8");
  defaultLayoutTypes.push("7/5");
  defaultLayoutTypes.push("5/7");
  defaultLayoutTypes.push("4/4/4");
  defaultLayoutTypes.push("6/3/3");
  defaultLayoutTypes.push("3/6/3");
  defaultLayoutTypes.push("3/3/6");

  var layouts = {};

  var trim = function (str) {
    // removes newline / carriage return
    str = str.replace(/\n/g, "");
    // removes whitespace (space and tabs) before tags
    str = str.replace(/[\t ]+\</g, "<");
    // removes whitespace between tags
    str = str.replace(/\>[\t ]+\</g, "><");
    // removes whitespace after tags
    str = str.replace(/\>[\t ]+$/g, ">");
    return str;
  };

  /**
   *  Injects columns sizes taken from the type of the layout.
   *  @param {CKEDITOR.template} template - The template that will be injected with values.
   */
  var getTemplateWithValue = function (template, type) {
    var cols = type.split("/");
    var injectTemplate = {};

    for (var i = 0; i < cols.length; i++) {
      injectTemplate["size" + (i + 1)] = cols[i];
    }
    return trim(template.output(injectTemplate));
  };

  var addLayout = function (template, type, buttonRowPosition) {
    layouts[type] = {
      template: getTemplateWithValue(template, type),
      rowPosition: buttonRowPosition
    };
  };

  this.addLayout = function (type, buttonRowPosition) {
    var columnCount = type.split('/').length;
    addLayout(defaultLayoutTemplates[columnCount - 1], type, buttonRowPosition);
  };

  this.buildDefaultLayouts = function () {
    var row = 0;
    for (var i = 0; i < defaultLayoutTypes.length; i++) {
      // This count to which row in the choose dialog should the layout button be added
      if (i % 4 == 0) {
        row += 1;
      }
      var columnCount = defaultLayoutTypes[i].split('/').length;
      addLayout(defaultLayoutTemplates[columnCount - 1], defaultLayoutTypes[i], row);
    }
  };

  this.getLayout = function (type) {
    return layouts[type];
  };

  this.getLayouts = function () {
    return layouts;
  };
}