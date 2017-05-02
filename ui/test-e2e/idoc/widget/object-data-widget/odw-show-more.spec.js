var IdocPage = require('../../idoc-page');
var Widget = require('./../widget').Widget;
var ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var SaveIdocDialog = require('./../../save-idoc-dialog').SaveIdocDialog;

const TAB_NUMBER = 1;
const FIELDS = {
  FIELD_ONE: 'field1',
  FIELD_TWO: 'field2',
  FIELD_TREE: 'field3'
};

describe('ObjectDataWidget', function () {

  describe('show more', () => {
    var idocPage = new IdocPage();

    beforeEach(() => {
      idocPage.open(true);
    });

    // AC1
    it('configuration should be present in configuration dialog', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.getShowMoreButton().isPresent()).to.become(true);
    });

    // AC2
    it('configuration should be selected by default', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.isSelectedShowMore()).to.eventually.be.true;
    });

    it('button should not be present when show more configuration is not selected', () => {
      var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      // given show more config is deselected
      widgetConfig.selectDisplayOptionsTab().toggleDisplayShowMore();
      widgetConfig.save();
      // when idoc is in preview mode
      idocPage.getActionsToolbar().saveIdoc();
      // show more button should not be present
      expect(new ObjectDataWidget(widgetElement).getShowMoreButton().isPresent()).to.become(false);
    });

    // AC5
    it('button should be present only in preview mode', () => {
      var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      var propertiesSelector = widgetConfig.selectObjectDetailsTab();
      propertiesSelector.selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      // when idoc is in edit mode
      // then show more button should not be present
      var widget = new ObjectDataWidget(widgetElement);
      expect(widget.getShowMoreButton().isPresent()).to.become(false);
      // when idoc is in preview mode
      idocPage.getActionsToolbar().saveIdoc();
      // then show more button should be present
      expect(widget.getShowMoreButton().isPresent()).to.become(true);
    });

    // AC6
    it('should be reset to default when idoc is turned in edit mode', () => {
      var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      var actionsToolbar = idocPage.getActionsToolbar();
      actionsToolbar.saveIdoc();
      // given show more is selected in preview mode
      var widget = new ObjectDataWidget(widgetElement);
      widget.toggleShowMoreButton();
      // when switch to edit mode
      actionsToolbar.editIdoc();

      widget.waitToAppear();

      // then only the properties selected in widget configuration should be displayed
      checkFields('isDisplayed', [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO], [true, true]);
      checkFields('isPresent', [FIELDS.FIELD_TREE], [true]);
    });

    // AC3, AC4
    it('button should toggle visibility of the fields that are not selected from the widget configuration', () => {
      var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      idocPage.getActionsToolbar().saveIdoc();
      var widget = new ObjectDataWidget(widgetElement);
      // when show more is selected
      widget.toggleShowMoreButton();
      // then all fields should be visible
      checkFields('isPresent', allFields, [true, true, true]);
      // when show less is selected
      widget.toggleShowMoreButton();
      // then only selected fields should be visible
      checkFields('isPresent', [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO], [true, true]);
      checkFields('isPresent', [FIELDS.FIELD_TREE], [true]);
    });

    it('button should not be present if no object is selected to be displayed', () => {
      var widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.save();
      // when idoc is in preview mode
      idocPage.getActionsToolbar().saveIdoc();
      // show more button should not be present
      expect(new ObjectDataWidget(widgetElement).getShowMoreButton().isPresent()).to.become(false);
    });
  });

  var allFields = [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO, FIELDS.FIELD_TREE];

  function checkFields(func, fields, conditions) {
    fields.forEach(function (field, index) {
      expect(element(by.id(field))[func]()).to.become(conditions[index]);
    });
  }
});
