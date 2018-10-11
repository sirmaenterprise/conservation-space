'use strict';

let FormWrapper = require('../form-wrapper').FormWrapper;
let SandboxPage = require('../../page-object').SandboxPage;
let ConceptPicker = require('../../components/concept-picker/concept-picker').ConceptPicker;
let ObjectControl = require('../form-control.js').ObjectControl;

let page = new SandboxPage();

describe('Concept form builder control', function () {

  let formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    page.open('/sandbox/form-builder/concept');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  it('should support single-value concept selection', function () {
    // Given I have opened a form with concept picker for single selection
    let conceptPicker = new ConceptPicker(formWrapper.findField('concept-wrapper'));

    // When I select new option
    conceptPicker.selectOption('Concrete');

    // And switch to preview mode
    formWrapper.togglePreviewMode();

    // Then I should see the header of the new option
    let objectControl = new ObjectControl($('#concept-wrapper'));
    objectControl.isHeaderVisible(0, 'Concrete header');
  });

  it('should support multi-value concept selection', function () {
    // Given I have opened a form with concept picker for multiple selection
    let conceptPicker = new ConceptPicker(formWrapper.findField('conceptMultiple-wrapper'));

    // It should have a preset value
    conceptPicker.getSelectedValue().then(value => expect(value).to.eql(['metal']));

    // When I select additional option
    conceptPicker.selectOption('Concrete');

    // And switch to preview mode
    formWrapper.togglePreviewMode();

    // Then I should see the headers of the selected options
    let objectControl = new ObjectControl($('#conceptMultiple-wrapper'));
    objectControl.isHeaderVisible(0, 'Concrete header');
    objectControl.isHeaderVisible(1, 'Metal header');
  });

  it('should render show more/less button in preview mode when there are more than 3 concepts selected', () => {
    // Given I have form with concept picker with 8 concepts selected
    // When I open the form in edit mode
    let conceptPicker = new ConceptPicker(formWrapper.findField('conceptMultiple2-wrapper'));

    // Then I should see a concept picker with 8 selected concepts
    conceptPicker.getSelectedValue().then(value => expect(value).to.eql(['concrete', 'reinforced', 'fiber', 'rebar', 'metal', 'hot_rolled', 'cold_formed', 'aluminium']));

    // When I switch to preview mode
    formWrapper.togglePreviewMode();

    // Then I should see 3 concept headers
    let objectControl = new ObjectControl($('#conceptMultiple2-wrapper'));
    objectControl.isHeaderVisible(0, 'Concrete header');
    objectControl.isHeaderVisible(1, 'Reinforced header');
    objectControl.isHeaderVisible(2, 'Fiber header');

    // And I should see show all button with 5 more concepts hidden
    objectControl.isShowAllButtonVisible();
    objectControl.verifyHiddenObjectsCountIs(5);

    // When I select the show all button
    objectControl.showAll();

    // Then I should see all 8 concept headers
    objectControl.isHeaderVisible(0, 'Concrete header');
    objectControl.isHeaderVisible(1, 'Reinforced header');
    objectControl.isHeaderVisible(2, 'Fiber header');
    objectControl.isHeaderVisible(3, 'Rebar header');
    objectControl.isHeaderVisible(4, 'Metal header');
    objectControl.isHeaderVisible(5, 'Hot rolled header');
    objectControl.isHeaderVisible(6, 'Cold formed header');
    objectControl.isHeaderVisible(7, 'Aluminium header');

    // And I should see show less button
    objectControl.isShowLessButtonVisible();

    // When I select show less
    objectControl.showLess();

    // Then I should see the 3 initial concept headers
    objectControl.isHeaderVisible(0, 'Concrete header');
    objectControl.isHeaderVisible(1, 'Reinforced header');
    objectControl.isHeaderVisible(2, 'Fiber header');

    // And I should see the show all button
    objectControl.isShowAllButtonVisible();
    objectControl.verifyHiddenObjectsCountIs(5);
  });

});