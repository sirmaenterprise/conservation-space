'use strict';

let ObjectControl = require('../form-control.js').ObjectControl;
let ObjectControlItem = require('../form-control.js').ObjectControlItem;
let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;
let Search = require('../../search/components/search.js').Search;
let SEARCH_EXTENSION = require('../../picker/object-picker').SEARCH_EXTENSION;
let ObjectPickerDialog = require('../../picker/object-picker').ObjectPickerDialog;

const TOOLTIP = 'seip-hint';

let page = new SandboxPage();

describe('Object control', () => {
  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/object-control');
    formWrapper.waitUntilVisible();
  });

  it('should mark field as invalid when it is mandatory and has no value', () => {
    let objectControl = new ObjectControl($('#singleObjectProperty-wrapper'));
    // should be valid initially as it has value
    expect(objectControl.hasError()).to.eventually.be.false;
    // should be invalid when there is no value selected
    objectControl.removeInstance(0);
    expect(objectControl.hasError()).to.eventually.be.true;
  });

  describe('tooltips', () => {
    it('should be displayed correctly in edit mode and hidden in preview mode', () => {
      let fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'single value').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'multi-value').to.eventually.be.true;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed()).to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed()).to.eventually.be.false;
    });
  });

  describe('select button', () => {
    it('should be visible in edit mode', () => {
      new ObjectControl($('#singleObjectProperty-wrapper')).isSelectInstanceButtonVisible();
    });

    it('should be hidden in preview mode', () => {
      formWrapper.togglePreviewMode();
      new ObjectControl($('#singleObjectProperty-wrapper')).isSelectInstanceButtonHidden();
    });

    it('should be visible when the property doesn`t have value', () => {
      new ObjectControl($('#multiObjectProperty2-wrapper')).isSelectInstanceButtonVisible();
    });
  });

  describe('remove instance button', () => {
    it('should be visible in edit mode', () => {
      let objectControl =  new ObjectControl($('#singleObjectProperty-wrapper'));

      objectControl.getInstance(0).then((instance) => {
        browser.actions().mouseMove(instance).perform();
        objectControl.isRemoveInstanceButtonVisible();
      });
    });

    it('should be hidden in preview mode', () => {
      formWrapper.togglePreviewMode();
      new ObjectControl($('#singleObjectProperty-wrapper')).isRemoveInstanceButtonHidden();
    });
  });

  describe('show all button', () => {
    it('should be visible in edit mode when total is greater than results', () => {
      new ObjectControl($('#multiObjectProperty-wrapper')).isShowAllButtonVisible();
    });

    it('should be visible in preview mode when total is greater than results', () => {
      formWrapper.togglePreviewMode();
      new ObjectControl($('#multiObjectProperty-wrapper')).isShowAllButtonVisible();
    });

    it('should be visible in edit mode when the visible objects are removed and total is greater than result count', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty-wrapper'));
      objectControl.isShowAllButtonVisible();
      objectControl.removeInstance(2);
      objectControl.isShowAllButtonVisible();
      objectControl.removeInstance(1);
      objectControl.isShowAllButtonHidden();
      objectControl.removeInstance(0);
      objectControl.isShowAllButtonHidden();
    });

    it('should be hidden in edit mode if total is equal to result count', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty4-wrapper'));
      objectControl.isShowAllButtonHidden();
    });

    it('should be hidden in preview mode if total is equal to result count', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty4-wrapper'));
      formWrapper.togglePreviewMode();
      objectControl.isShowAllButtonHidden();
    });

    it('should be hidden when the property doesn`t have value', () => {
      new ObjectControl($('#multiObjectProperty1-wrapper')).isShowAllButtonHidden();
    });
  });

  // Initial items count config is set to 3
  describe('show less button', () => {
    it('should be hidden in edit mode when result count is equal to total but more than config', () => {
      new ObjectControl($('#multiObjectProperty3-wrapper')).isShowLessButtonHidden();
    });

    it('should be hidden in preview mode when result count is equal to total but more than config', () => {
      formWrapper.togglePreviewMode();
      new ObjectControl($('#multiObjectProperty3-wrapper')).isShowLessButtonHidden();
    });

    it('should be hidden in edit mode when result count is less than or equal to config', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty3-wrapper'));
      objectControl.showAll();
      objectControl.removeInstance(4);
      objectControl.isShowLessButtonVisible();
      objectControl.removeInstance(3);
      objectControl.isShowLessButtonHidden();
      objectControl.removeInstance(2);
      objectControl.isShowLessButtonHidden();
      objectControl.removeInstance(1);
      objectControl.isShowLessButtonHidden();
      objectControl.removeInstance(0);
      objectControl.isShowLessButtonHidden();
    });

    it('should not be visible when the property doesn`t have value', () => {
      new ObjectControl($('#multiObjectProperty1-wrapper')).isShowLessButtonHidden();
    });
  });

  // Given I have an object property of type multy
  describe('object property of type multiselect', () => {
    it('should allow multiple objects to be selected through the picker', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty2-wrapper'));
      // When I click select button
      // And I select 3 objects from the search result
      // And I confirm the selection
      toggleObjectSelection(objectControl, [0, 1, 2]);
      // Then I want to see 3 objects in the object control
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(3);
    });

    it('should allow all objects to be deselected through the picker', () => {
      let objectControl = new ObjectControl($('#multiObjectProperty2-wrapper'));
      // And I select 3 objects from the search result
      // And I confirm the selection
      toggleObjectSelection(objectControl, [0, 1, 2]);
      // And I click select button
      // And I deselect all objects
      // And I confirm the selection
      toggleObjectSelection(objectControl, [0, 1, 2]);
      // Then I want object control to have no values
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
    });
  });

  describe('show more / show less buttons', () => {
    it('should be visible or hidden according configured initial objects count', () => {
      // Given There is a multy selection object property in current object.
      // And Configured initial object count is 3.
      // When There is no selected objects.
      let objectControl = new ObjectControl($('#multiObjectProperty2-wrapper'));
      // Then I expect show more and show less buttons to be hidden.
      objectControl.isShowAllButtonHidden();
      objectControl.isShowLessButtonHidden();
      // When I select three objects 1, 2, 3.
      toggleObjectSelection(objectControl, [0, 1, 2]);
      // Then I expect show more and show less buttons to be hidden.
      objectControl.isShowAllButtonHidden();
      objectControl.isShowLessButtonHidden();
      // When I select objects 4, 5.
      toggleObjectSelection(objectControl, [3, 4]);
      // Then I expect show more button to be visible.
      // And I expect show less button to be hidden.
      objectControl.isShowAllButtonVisible();
      objectControl.isShowLessButtonHidden();
      // When I select show more button.
      objectControl.showAll();
      // Then I expect all five selected objects (1, 2, 3, 4, 5) to be visible.
      // And I expect show less button to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #1');
      checkObjectHeader(objectControl, 1, 'Object compact header #2');
      checkObjectHeader(objectControl, 2, 'Object compact header #3');
      checkObjectHeader(objectControl, 3, 'Object compact header #4');
      checkObjectHeader(objectControl, 4, 'Object compact header #5');
      // When I select show less button.
      objectControl.showLess();
      // Then I expect show more button to be visible.
      // And I expect only three objects (1, 2, 3) to be visible.
      objectControl.isShowAllButtonVisible();
      checkObjectHeader(objectControl, 0, 'Object compact header #1');
      checkObjectHeader(objectControl, 1, 'Object compact header #2');
      checkObjectHeader(objectControl, 2, 'Object compact header #3');
      // When I remove object 1.
      objectControl.removeInstance(0);
      // Then I expect objects 2, 3, 4 to be visible.
      // And I expect show more button to be visible.
      // And I expect show less button to be hidden.
      checkObjectHeader(objectControl, 0, 'Object compact header #2');
      checkObjectHeader(objectControl, 1, 'Object compact header #3');
      checkObjectHeader(objectControl, 2, 'Object compact header #4');
      objectControl.isShowAllButtonVisible();
      objectControl.isShowLessButtonHidden();
      // When I remove object 2.
      objectControl.removeInstance(0);
      // Then I expect objects 3, 4, 5 to be visible.
      // And I expect show more button to be hidden.
      // And I expect show less button to be hidden.
      checkObjectHeader(objectControl, 0, 'Object compact header #3');
      checkObjectHeader(objectControl, 1, 'Object compact header #4');
      checkObjectHeader(objectControl, 2, 'Object compact header #5');
      objectControl.isShowAllButtonHidden();
      objectControl.isShowLessButtonHidden();

      // When I have 6 objects selected (more than the configured initial count).
      toggleObjectSelection(objectControl, [0, 1, 5]);
      // And I select show more button.
      objectControl.showAll();

      // And I remove object 1.
      objectControl.removeInstance(0);

      // TODO: Picker should somehow return objects sorted by mod.date

      // Then I expect objects 4, 5, 1, 2, 6 to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #4');
      checkObjectHeader(objectControl, 1, 'Object compact header #5');
      checkObjectHeader(objectControl, 2, 'Object compact header #1');
      checkObjectHeader(objectControl, 3, 'Object compact header #2');
      checkObjectHeader(objectControl, 4, 'Object compact header #6');
      // And I expect show less button to be visible.
      objectControl.isShowLessButtonVisible();

      // When I remove object 2.
      objectControl.removeInstance(0);
      // Then I expect objects 5, 1, 2, 6 to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #5');
      checkObjectHeader(objectControl, 1, 'Object compact header #1');
      checkObjectHeader(objectControl, 2, 'Object compact header #2');
      checkObjectHeader(objectControl, 3, 'Object compact header #6');
      // And I expect show less button to be visible.
      objectControl.isShowLessButtonVisible();

      // When I remove object 3.
      objectControl.removeInstance(0);
      // Then I expect objects 1, 2, 6 to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #1');
      checkObjectHeader(objectControl, 1, 'Object compact header #2');
      checkObjectHeader(objectControl, 2, 'Object compact header #6');
      // And I expect show less button to be hidden.
      objectControl.isShowLessButtonHidden();

      // When I remove object 4.
      objectControl.removeInstance(0);
      // Then I expect objects 2, 6 to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #2');
      checkObjectHeader(objectControl, 1, 'Object compact header #6');
      // And I expect show less button to be hidden.
      objectControl.isShowLessButtonHidden();

      // When I remove object 5.
      objectControl.removeInstance(0);
      // Then I expect objects 6 to be visible.
      checkObjectHeader(objectControl, 0, 'Object compact header #6');
      // And I expect show less button to be hidden.
      objectControl.isShowLessButtonHidden();

      // When I remove object 6.
      objectControl.removeInstance(0);
      // Then I expect object control to be empty.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
      // And I expect show less button to be hidden.
      objectControl.isShowLessButtonHidden();
    });
  });

  describe('multy selection object property', () => {
    it('should allow select, deselect and change operations (maintain the changeset 1)', () => {
      // Given There is a multy selection object property in current object.
      let objectControl = new ObjectControl($('#multiObjectProperty2-wrapper'));
      // When There is no selection initially.
      // Then I expect the control to be empty.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
      // When I select one object from the picker.
      toggleObjectSelection(objectControl, [0]);
      // Then I expect object header to be visible in the control.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      checkObjectHeader(objectControl, 0, 'Object compact header #1');
      // When I select two more objects from the picker.
      toggleObjectSelection(objectControl, [1, 2]);
      // Then I expect three objects to be visible in the control.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(3);
      checkObjectHeader(objectControl, 1, 'Object compact header #2');
      checkObjectHeader(objectControl, 2, 'Object compact header #3');
      // When I deselect first object from the picker.
      toggleObjectSelection(objectControl, [0]);
      // Then I expect object 2 and 3 to be visible in control.
      checkObjectHeader(objectControl, 0, 'Object compact header #2');
      checkObjectHeader(objectControl, 1, 'Object compact header #3');
      // When I deselect all objects from the picker.
      toggleObjectSelection(objectControl, [1, 2]);
      // Then I expect control to be empty.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
      // When I select three objects from the picker.
      toggleObjectSelection(objectControl, [0, 1, 2]);
      // And I deselect all three objects from the control using their remove button.
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      // Then I expect control to be empty.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
    });

    it('should allow select, deselect and change operations (maintain the changeset 2)', () => {
      // Given There is a multy selection object property in current object.
      let objectControl = new ObjectControl($('#multiObjectProperty4-wrapper'));
      // When There are 2 objects selected initially.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(2);
      checkObjectHeader(objectControl, 0, 'Header-1');
      checkObjectHeader(objectControl, 1, 'Header-2');
      // When I select 3 object from the picker.
      toggleObjectSelection(objectControl, [2]);
      // Then I expect 3 objects to be selected
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(3);
      checkObjectHeader(objectControl, 0, 'Header-1');
      checkObjectHeader(objectControl, 1, 'Header-2');
      checkObjectHeader(objectControl, 2, 'Object compact header #3');
      // When I remove 2, 3 from the picker
      toggleObjectSelection(objectControl, [1, 2]);
      // Then I expect 1 object to be selected
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      checkObjectHeader(objectControl, 0, 'Header-1');
      // When I select 2, 3 from the picker
      toggleObjectSelection(objectControl, [1, 2]);
      // Then I expect 3 objects to be selected
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(3);
      checkObjectHeader(objectControl, 0, 'Header-1');
      checkObjectHeader(objectControl, 1, 'Header-2');
      checkObjectHeader(objectControl, 2, 'Object compact header #3');
      // When I deselect 2, 3 from the picker again
      toggleObjectSelection(objectControl, [1, 2]);
      // Then I expect 1 object to be selected
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      checkObjectHeader(objectControl, 0, 'Header-1');
    });

    // Test following scenario: When object gets loaded initially, the model is populated with default values which
    // are copy of the actual values returned from the backend. In case for object properties where the results are
    // limited it appears that the value could be partially loaded which also affects and the defaultValues.
    // For example when limit=3 and there are 4 relations of which 3 are returned initially and those 3 are set as
    // defaultValue, then the change set should be build properly.
    it('should build proper change set after all objects are removed and new are added above the initial limit', () => {
      // Given I have loaded instance containing object property with 5 selected related objects
      // And The initial count is set to 3
      let objectControl = new ObjectControl($('#multiObjectProperty-wrapper'));
      // When I remove all 5 selected objects
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      objectControl.removeInstance(0);
      // Then I expect property model value to be empty and changeset to contains all removed objects
      formWrapper.getPropertyValue('multiObjectProperty').then(value => {
        expect(value).to.equal('{"total":0,"offset":0,"limit":5,"results":[],"add":[],"remove":["1","2","3","4","5"],"headers":{"1":{"id":"1","compact_header":"<a href=\'#\'>Header-1</a>","breadcrumb_header":"<a href=\'#\'>Header-1</a>"},"2":{"id":"2","compact_header":"<a href=\'#\'>Header-2</a>","breadcrumb_header":"<a href=\'#\'>Header-2</a>"},"3":{"id":"3","compact_header":"<a href=\'#\'>Header-3</a>","breadcrumb_header":"<a href=\'#\'>Header-3</a>"},"4":{"id":"4","compact_header":"<a href=\'#\'>Header-4</a>","breadcrumb_header":"<a href=\'#\'>Header-4</a>"},"5":{"id":"5","compact_header":"<a href=\'#\'>Header-5</a>","breadcrumb_header":"<a href=\'#\'>Header-5</a>"}}}');
      });
      // When I add a new relation
      toggleObjectSelection(objectControl, [5]);
      // Then I expect the value to contain the new related object and the changeset to contain all removed and the added objects
      formWrapper.getPropertyValue('multiObjectProperty').then(value => {
        expect(value).to.equal('{"total":1,"offset":0,"limit":5,"results":["6"],"add":["6"],"remove":["1","2","3","4","5"],"headers":{"1":{"id":"1","compact_header":"<a href=\'#\'>Header-1</a>","breadcrumb_header":"<a href=\'#\'>Header-1</a>"},"2":{"id":"2","compact_header":"<a href=\'#\'>Header-2</a>","breadcrumb_header":"<a href=\'#\'>Header-2</a>"},"3":{"id":"3","compact_header":"<a href=\'#\'>Header-3</a>","breadcrumb_header":"<a href=\'#\'>Header-3</a>"},"4":{"id":"4","compact_header":"<a href=\'#\'>Header-4</a>","breadcrumb_header":"<a href=\'#\'>Header-4</a>"},"5":{"id":"5","compact_header":"<a href=\'#\'>Header-5</a>","breadcrumb_header":"<a href=\'#\'>Header-5</a>"},"6":{"id":"6","compact_header":"<span>Object compact header #6</span>"}}}');
      });
    });
  });

  describe('single selection object property', () => {
    it('should allow deselect, select, change operations', () => {
      // Given There is as single selection object property in current object.
      let objectControl = new ObjectControl($('#singleObjectProperty-wrapper'));
      // When There is selected object initially.
      // Then I expect selected object header to be visible in the control.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      // When I remove the selected instance.
      objectControl.removeInstance(0);
      // Then I expect the control should be empty
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(0);
      // When I select the third object from the picker.
      toggleObjectSelection(objectControl, [2]);
      // Then I expect the selected object to be visible in the control.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      // And I expect the selected object to be the one I chose from the picker.
      checkObjectHeader(objectControl, 0, 'Object compact header #3');
      // When I select the first object from the picker.
      toggleObjectSelection(objectControl, [0]);
      // Then I expect the selected object to be visible in the control.
      expect(objectControl.getSelectedObjectsCount()).to.eventually.equal(1);
      // And I expect the selected object to be the one I chose from the picker.
      checkObjectHeader(objectControl, 0, 'Header-1');
    });

    it('should keep its default value as "remove" property for a consistent changeset', () => {
      let objectControl = new ObjectControl($('#singleObjectProperty-wrapper'));
      toggleObjectSelection(objectControl, [1]);
      formWrapper.getPropertyValue('singleObjectProperty').then(value => {
        expect(value).to.equal(`{"total":1,"offset":0,"limit":5,"results":["2"],"add":["2"],"remove":["1"],"headers":{"1":{"id":"1","compact_header":"<a href='#'>Header-1</a>","breadcrumb_header":"<a href='#'>Header-1</a>"},"2":{"id":"2","compact_header":"<span>Object compact header #2</span>"}}}`);
      });
      toggleObjectSelection(objectControl, [2]);
      formWrapper.getPropertyValue('singleObjectProperty').then(value => {
        expect(value).to.equal(`{"total":1,"offset":0,"limit":5,"results":["3"],"add":["3"],"remove":["1"],"headers":{"1":{"id":"1","compact_header":"<a href='#'>Header-1</a>","breadcrumb_header":"<a href='#'>Header-1</a>"},"2":{"id":"2","compact_header":"<span>Object compact header #2</span>"},"3":{"id":"3","compact_header":"<span>Object compact header #3</span>"}}}`);
      });
      objectControl.removeInstance(0);
      formWrapper.getPropertyValue('singleObjectProperty').then(value => {
        expect(value).to.equal(`{"total":0,"offset":0,"limit":5,"results":[],"add":[],"remove":["1"],"headers":{"1":{"id":"1","compact_header":"<a href='#'>Header-1</a>","breadcrumb_header":"<a href='#'>Header-1</a>"},"2":{"id":"2","compact_header":"<span>Object compact header #2</span>"},"3":{"id":"3","compact_header":"<span>Object compact header #3</span>"}}}`);
      });
      toggleObjectSelection(objectControl, [3]);
      formWrapper.getPropertyValue('singleObjectProperty').then(value => {
        expect(value).to.equal(`{"total":1,"offset":0,"limit":5,"results":["4"],"add":["4"],"remove":["1"],"headers":{"1":{"id":"1","compact_header":"<a href='#'>Header-1</a>","breadcrumb_header":"<a href='#'>Header-1</a>"},"2":{"id":"2","compact_header":"<span>Object compact header #2</span>"},"3":{"id":"3","compact_header":"<span>Object compact header #3</span>"},"4":{"id":"4","compact_header":"<span>Object compact header #4</span>"}}}`);
      });
    });
  });

  /**
   * @param objectControl
   * @param ind In object control, visible objects indexes begins from 0.
   * @param header The header text to compare against.
   */
  function checkObjectHeader(objectControl, ind, header) {
    objectControl.getSelectedObjects().then((selectedObjects) => {
      expect(selectedObjects[ind].getHeader()).to.eventually.equal(header);
    });
  }

  function toggleObjectSelection(objectControl, indexes) {
    objectControl.selectInstance();
    let objectPickerDialog = new ObjectPickerDialog();
    let search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();
    let results = search.getResults();
    // zero based, so item 2 is the third element in result list
    indexes.forEach((ind) => {
      results.clickResultItem(ind);
    });
    objectPickerDialog.ok();
    objectPickerDialog.waitUntilClosed();
  }

});
