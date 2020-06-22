var IdocPage = require('../idoc-page').IdocPage;

var DebugWidget = require('../widget/debug-widget').DebugWidget;
var DebugWidgetConfigDialog = require('../widget/debug-widget').DebugWidgetConfigDialog;
var IdocCommentsToolbar = require('../idoc-comments/idoc-comments-toolbar');

var ObjectDataWidget = require('../widget/object-data-widget/object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('../widget/object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;

var ObjectSelector = require('../widget/object-selector/object-selector.js').ObjectSelector;

var TEMPLATE_INSTANCE_ID = 'emf:template1';

describe('Modeling mode in idoc editor', () => {

  var page = new IdocPage();

  describe('in modeling mode', function () {

    it('should substitute the template model with the model of type under modeling', function () {
      // Given I have opened idoc page for existing template instance
      page.open(true, TEMPLATE_INSTANCE_ID);

      // When I add debug info widget
      var contentArea = page.getTabEditor(1);
      contentArea.insertWidget(DebugWidget.WIDGET_NAME);
      new DebugWidgetConfigDialog().save();

      var widgetElement = page.getTabEditor(1).getWidget(DebugWidget.WIDGET_NAME);
      var widget = new DebugWidget(widgetElement);
      widget.waitToAppear();

      // Then I should see that the definition of the current object is the type under modeling
      expect(widget.isModeling()).to.eventually.be.true;
      expect(widget.getCurrentObjectType()).to.eventually.equal('documentinstance');

      // And When I insert Object Data Widget
      contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
      var configDialog = new ObjectDataWidgetConfig();

      // And Select the current bbject
      configDialog.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);

      // Then I should see the properties from the definition of the type under modeling
      var propertySelectionTab = configDialog.selectObjectDetailsTab();

      // When I configure the widget to show three specific fields
      propertySelectionTab.selectProperty('title');
      propertySelectionTab.selectProperty('description');
      propertySelectionTab.selectProperty('name');

      // And save the widget
      configDialog.save();

      // Then I should see these fields displayed in the widget
      widgetElement = page.getTabEditor(1).getWidget(ObjectDataWidget.WIDGET_NAME);
      var objectDataWidget = new ObjectDataWidget(widgetElement);

      var form = objectDataWidget.getForm();
      form.waitUntilVisible();

      expect(form.getAllFields().then(fields => {
        return fields.length;
      })).to.eventually.equal(3);
    });

    it('should not set modeling mode when not under modeling', function () {
      // Given I have opened idoc page for existing template instance
      page.open(true, 'emf:123456');

      // When I add debug info widget
      var contentArea = page.getTabEditor(1);
      contentArea.insertWidget(DebugWidget.WIDGET_NAME);
      new DebugWidgetConfigDialog().save();

      var widgetElement = page.getTabEditor(1).getWidget(DebugWidget.WIDGET_NAME);
      var widget = new DebugWidget(widgetElement);
      widget.waitToAppear();

      // I should see that the definition of the current object is the type under modeling
      expect(widget.isModeling()).to.eventually.be.false;
      expect(widget.getCurrentObjectType()).to.eventually.equal('documentInstance');
    });

    it('should display the template comments', function() {
      // Given I have opened idoc page for existing template instance
      page.open(true, TEMPLATE_INSTANCE_ID);

      // I should see the comments section
      var tab = page.getIdocTabs().getTabByIndex(0);
      var commentSection = tab.getContent().getCommentsSection();

      // And the comment
      var commentsToolbar = new IdocCommentsToolbar(commentSection);
      expect(commentsToolbar.getCommentButton().isPresent()).to.eventually.be.true;
    });
  });


});