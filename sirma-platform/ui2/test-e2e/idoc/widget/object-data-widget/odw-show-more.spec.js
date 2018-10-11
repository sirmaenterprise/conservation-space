'use strict';

let IdocPage = require('../../idoc-page').IdocPage;
let ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

const FIELDS = {
  FIELD_ONE: 'field1',
  FIELD_TWO: 'field2',
  FIELD_THREE: 'field3'
};

describe('Show more option in ODW', () => {

  describe('show more', () => {
    let idocPage = new IdocPage();

    beforeEach(() => {
      idocPage.open(true);
    });

    // AC1
    it('configuration should be present in configuration dialog', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.getShowMoreButton().isPresent()).to.become(true);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.save();
    });

    // AC2
    it('configuration should be selected by default', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.isSelectedShowMore()).to.eventually.be.true;
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.save();
    });

    it('button should not be present when show more configuration is not selected', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
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
    it('button should be present in preview and edit mode', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      let propertiesSelector = widgetConfig.selectObjectDetailsTab();
      propertiesSelector.selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      // when idoc is in edit mode
      // then show more button should not be present
      let widget = new ObjectDataWidget(widgetElement);
      expect(widget.getShowMoreButton().isPresent()).to.become(true);
      // when idoc is in preview mode
      idocPage.getActionsToolbar().saveIdoc();
      // then show more button should be present
      expect(widget.getShowMoreButton().isPresent()).to.become(true);
    });

    // AC6
    it('should be reset to default when idoc is turned in edit mode', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      let actionsToolbar = idocPage.getActionsToolbar();
      actionsToolbar.saveIdoc();
      // given show more is selected in preview mode
      let widget = new ObjectDataWidget(widgetElement);
      widget.toggleShowMoreButton();
      // when switch to edit mode
      actionsToolbar.getActionsMenu().editIdoc();

      idocPage.waitForEditMode();

      widget.waitToAppear();

      browser.wait(EC.presenceOf(element(by.id(FIELDS.FIELD_ONE))), DEFAULT_TIMEOUT);

      // then only the properties selected in widget configuration should be displayed
      checkFields('isDisplayed', [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO], [true, true]);
      checkFields('isPresent', [FIELDS.FIELD_THREE], [false]);
    });

    // AC3, AC4
    it('button should toggle visibility of the fields that are not selected from the widget configuration', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      idocPage.getActionsToolbar().saveIdoc();
      let widget = new ObjectDataWidget(widgetElement);
      // when show more is selected
      widget.toggleShowMoreButton();

      browser.wait(EC.visibilityOf(element(by.id(`${FIELDS.FIELD_ONE}-wrapper`))), DEFAULT_TIMEOUT);

      // then all fields should be visible
      checkFields('isPresent', allFields, [true, true, true]);
      // when show less is selected
      widget.toggleShowMoreButton();
      // then only selected fields should be visible
      checkFields('isPresent', allFields, [true, true, false]);
    });

    it('button should not be present if no object is selected to be displayed', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.save();
      // when idoc is in preview mode
      idocPage.getActionsToolbar().saveIdoc();
      // show more button should not be present
      expect(new ObjectDataWidget(widgetElement).getShowMoreButton().isPresent()).to.become(false);
    });
  });

  let allFields = [FIELDS.FIELD_ONE, FIELDS.FIELD_TWO, FIELDS.FIELD_THREE];

  function checkFields(func, fields, conditions) {
    fields.forEach(function (field, index) {
      expect(element(by.id(field))[func]()).to.become(conditions[index]);
    });
  }
});
