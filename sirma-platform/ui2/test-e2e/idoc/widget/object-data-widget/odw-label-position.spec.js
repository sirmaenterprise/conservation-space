var IdocPage = require('../../idoc-page').IdocPage;
var ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

const FIELDS = {
  FIELD_ONE: 'field1',
  FIELD_TWO: 'field2',
  FIELD_THREE: 'field3'
};

describe('ObjectDataWidget', function () {

  var idocPage = new IdocPage();

  describe('label position', () => {
    beforeEach(() => {
      idocPage.open(true);
    });

    // AC1, AC2
    it('panel should be visible', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.isLabelPositionPanelVisible()).to.eventually.be.true;
    });

    // AC3, AC4
    it('should have "Position label left from the field" selected by default', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.getLabelPosition()).to.eventually.equal('label-left');
    });

    // AC5
    it('should have radio buttons for selecting label text position if "Position label left from the field" is selected', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      // Check that buttons exists. Otherwise test will fail with NoSuchElementError
      displayOptionsTab.toggleLabelTextLeft();
      displayOptionsTab.toggleLabelTextRight();
    });

    // AC5
    it('should display labels at correct position if "Position label left from the field" is selected', () => {
      var widget = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();
      browser.wait(EC.presenceOf(widget.$('form > div.label-left')), DEFAULT_TIMEOUT);
      var fieldWrapper = widget.element(by.id(FIELDS.FIELD_ONE + '-wrapper'));
      var getLabelInputSizeRatio = fieldWrapper.isPresent().then(() => {
        return protractor.promise.all([fieldWrapper.$('label'), fieldWrapper.$('div > input')]).then((elements) => {
          return protractor.promise.all([elements[0].getSize(), elements[1].getSize()]).then((sizes) => {
            return sizes[1].width / sizes[0].width;
          });
        });
      });
      expect(getLabelInputSizeRatio).to.eventually.be.closeTo(3, 0.1);
    });

    // AC6
    it('should display labels at correct position if "Position label above the field" is selected', () => {
      var widget = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.selectDisplayOptionsTab().toggleLabelAbove();
      widgetConfig.save();
      browser.wait(EC.presenceOf(widget.$('form > div.label-above')), DEFAULT_TIMEOUT);
    });

    // AC7
    it('should not display labels if "Hide label" is selected', () => {
      var widget = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      var widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.selectDisplayOptionsTab().toggleLabelHidden();
      widgetConfig.save();
      expect(widget.$('form > div.label-hidden').isPresent()).to.eventually.be.true;

      var fieldLabel = widget.$('label[for="' + FIELDS.FIELD_ONE + '"]');
      expect(fieldLabel.isPresent()).to.eventually.be.true;
      expect(fieldLabel.isDisplayed()).to.eventually.be.false;
    });
  });

});