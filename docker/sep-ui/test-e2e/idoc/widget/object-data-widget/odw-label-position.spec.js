'use strict';

let IdocPage = require('../../idoc-page').IdocPage;
let ObjectDataWidget = require('./object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
let FormWrapper = require('./../../../form-builder/form-wrapper').FormWrapper;

const FIELDS = {
  FIELD_ONE: 'field1',
  FIELD_TWO: 'field2',
  FIELD_THREE: 'field3'
};

describe('Labels in ObjectDataWidget', function () {

  let idocPage = new IdocPage();

  describe('label position', () => {
    beforeEach(() => {
      idocPage.open(true);
    });

    // AC1, AC2
    it('panel should be visible', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.isLabelPositionPanelVisible()).to.eventually.be.true;
    });

    // AC3, AC4
    it('should have "Position label left from the field" selected by default', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      expect(displayOptionsTab.getLabelPosition()).to.eventually.equal('label-left');
    });

    // AC5
    it('should have radio buttons for selecting label text position if "Position label left from the field" is selected', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let displayOptionsTab = new ObjectDataWidgetConfig().selectDisplayOptionsTab();
      // Check that buttons exists. Otherwise test will fail with NoSuchElementError
      displayOptionsTab.toggleLabelTextLeft();
      displayOptionsTab.toggleLabelTextRight();
    });

    // AC5
    it('should display labels at correct position if "Position label left from the field" is selected', () => {
      let widgetElement = idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.save();

      let widget = new ObjectDataWidget($$('.object-data-widget').get(0));
      let form = widget.getForm();

      expect(form.getFieldsLabelPosition()).to.eventually.equal(FormWrapper.LABEL_POSITION.LABEL_LEFT_TEXT_LEFT);

      let fieldWrapper = widgetElement.element(by.id(FIELDS.FIELD_ONE + '-wrapper'));
      let getLabelInputSizeRatio = fieldWrapper.isPresent().then(() => {
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
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.selectDisplayOptionsTab().toggleLabelAbove();
      widgetConfig.save();

      let widget = new ObjectDataWidget($$('.object-data-widget').get(0));
      let form = widget.getForm();

      expect(form.getFieldsLabelPosition()).to.eventually.equal(FormWrapper.LABEL_POSITION.LABEL_ABOVE);
    });

    // AC7
    it('should not display labels if "Hide label" is selected', () => {
      idocPage.getTabEditor(1).insertWidget(ObjectDataWidget.WIDGET_NAME);
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      widgetConfig.selectObjectDetailsTab().selectProperties([FIELDS.FIELD_ONE, FIELDS.FIELD_TWO]);
      widgetConfig.selectDisplayOptionsTab().toggleLabelHidden();
      widgetConfig.save();

      let widget = new ObjectDataWidget($$('.object-data-widget').get(0));
      let form = widget.getForm();

      expect(form.getFieldsLabelPosition()).to.eventually.equal(FormWrapper.LABEL_POSITION.LABEL_HIDDEN);
    });
  });

});