'use strict';

var ObjectDataWidgetConfig = require('./object-data-widget.js').ObjectDataWidgetConfig;
var ObjectDataWidgetSandboxPage = require('./object-data-widget.js').ObjectDataWidgetSandboxPage;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

describe('ObjectDataWidget in modeling mode', () => {

  var page = new ObjectDataWidgetSandboxPage();

  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  describe('when current object is selected', () => {

    it('should display current object properties without values', () => {
      // When I inserted ODW
      page.insertWidget();
      // When I configured object selection to be current object
      let widgetConfig = new ObjectDataWidgetConfig();
      widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
      // When I selected two properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectProperty('textareaEdit');
      objectDetailsTab.selectProperty('inputTextEdit');
      // When I save config
      widgetConfig.save();
      // Then I expect DTW widget to be visible
      let widget = page.getWidget();
      // Then I expect all selected properties labels to be be visible in widget
      let formWrapper = widget.getForm();
      let inputText = formWrapper.getInputText('textareaEdit');
      let inputText2 = formWrapper.getInputText('inputTextEdit');
      expect(inputText.isVisible()).to.eventually.be.true;
      expect(inputText2.isVisible()).to.eventually.be.true;
      // And no values to be present
      expect(inputText.getPreviewValue()).to.eventually.equal('');
      expect(inputText2.getPreviewValue()).to.eventually.equal('');
    });
  });

  describe('when in automatic object selection', () => {

    it('should render selected properties from the matched object without values when search has returned single object', () => {
      // When I inserted ODW
      page.insertWidget();
      // When I configured object selection to be current object
      let widgetConfig = new ObjectDataWidgetConfig();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      // When I selected two properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectProperty('textareaEdit');
      objectDetailsTab.selectProperty('inputTextEdit');
      // When I save config
      widgetConfig.save();
      // Then I expect DTW widget to be visible
      let widget = page.getWidget();
      // Then I expect all selected properties labels to be be visible in widget
      let formWrapper = widget.getForm();
      let inputText = formWrapper.getInputText('textareaEdit');
      let inputText2 = formWrapper.getInputText('inputTextEdit');
      expect(inputText.isVisible()).to.eventually.be.true;
      expect(inputText2.isVisible()).to.eventually.be.true;
      // And values to be empty
      expect(inputText.getPreviewValue()).to.eventually.equal('');
      expect(inputText2.getPreviewValue()).to.eventually.equal('');
    });

    it('should render properties without values if search has returned empty resultset', () => {
      // configure search to return empty result
      page.changeSearchDataset('empty');
      // When I inserted ODW
      page.insertWidget();
      // When I configured object selection to be current object
      let widgetConfig = new ObjectDataWidgetConfig();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      // When I selected two properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectProperty('textareaEdit');
      objectDetailsTab.selectProperty('inputTextEdit');
      // When I save config
      widgetConfig.save();
      // Then I expect DTW widget to be visible
      let widget = page.getWidget();
      // Then I expect all selected properties labels to be be visible in widget
      let formWrapper = widget.getForm();
      let inputText = formWrapper.getInputText('textareaEdit');
      let inputText2 = formWrapper.getInputText('inputTextEdit');
      expect(inputText.isVisible()).to.eventually.be.true;
      expect(inputText2.isVisible()).to.eventually.be.true;
      // And values to be empty
      expect(inputText.getPreviewValue()).to.eventually.equal('');
      expect(inputText2.getPreviewValue()).to.eventually.equal('');
    });

    it('should render properties without values if search has returned more than one object', () => {
      // configure search to return multivalue result
      page.changeSearchDataset('multiple');
      // When I inserted ODW
      page.insertWidget();
      // When I configured object selection to be current object
      let widgetConfig = new ObjectDataWidgetConfig();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      // When I selected two properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectProperty('textareaEdit');
      objectDetailsTab.selectProperty('inputTextEdit');
      // When I save config
      widgetConfig.save();
      // Then I expect DTW widget to be visible
      let widget = page.getWidget();
      // Then I expect all selected properties labels to be be visible in widget
      let formWrapper = widget.getForm();
      let inputText = formWrapper.getInputText('textareaEdit');
      let inputText2 = formWrapper.getInputText('inputTextEdit');
      expect(inputText.isVisible()).to.eventually.be.true;
      expect(inputText2.isVisible()).to.eventually.be.true;
      // And values to be empty
      expect(inputText.getPreviewValue()).to.eventually.equal('');
      expect(inputText2.getPreviewValue()).to.eventually.equal('');
    });
  });

  describe('when in manual object selection', () => {

    it('should render properties with their values of the selected object', () => {
      // When I inserted ODW
      page.insertWidget();
      // When I configured object selection to be current object
      let widgetConfig = new ObjectDataWidgetConfig();
      let objectSelectorTab = widgetConfig.selectObjectSelectTab();
      objectSelectorTab.selectObjectSelectionMode(ObjectSelector.MANUALLY);
      let search = objectSelectorTab.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().clickResultItem(0);
      // When I selected two properties
      let objectDetailsTab = widgetConfig.selectObjectDetailsTab();
      objectDetailsTab.selectProperty('textareaEdit');
      objectDetailsTab.selectProperty('inputTextEdit');
      // When I save config
      widgetConfig.save();
      // Then I expect DTW widget to be visible
      let widget = page.getWidget();
      // Then I expect all selected properties labels to be be visible in widget
      let formWrapper = widget.getForm();
      let inputText = formWrapper.getInputText('textareaEdit');
      let inputText2 = formWrapper.getInputText('inputTextEdit');
      expect(inputText.isVisible()).to.eventually.be.true;
      expect(inputText2.isVisible()).to.eventually.be.true;
      // And values to be present
      expect(inputText.getPreviewValue()).to.eventually.equal('textareaEdit');
      expect(inputText2.getPreviewValue()).to.eventually.equal('inputTextEdit');
    });
  });

});