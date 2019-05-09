'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field value attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should allow editing of string value and restoring initial model value', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {

      let title = items[0];
      title.showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getValue()).to.eventually.equal('Status: ${get[state]}');
          expect(attr.isDirty()).to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.setValue('Media status: ${get[state]}');
          expect(field.getValue()).to.eventually.equal('Media status: ${get[state]}');
          expect(attr.isDirty()).to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.setValue('Status: ${get[state]}');
          expect(attr.isDirty()).to.eventually.be.false;
        });
      });

      // change and restore attribute which didn't have initial value
      let fixedLengthTitle = items[7];
      fixedLengthTitle.showAttributes();
      details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getValue()).to.eventually.equal('');
          expect(attr.isDirty()).to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.setValue('new title');
          expect(field.getValue()).to.eventually.equal('new title');
          expect(attr.isDirty()).to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.setValue('');
          expect(attr.isDirty()).to.eventually.be.false;
        });
      });
    });
  });

  it('should allow editing of date type value and restoring initial model value', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {

      let date = items[6];
      date.showAttributes();
      let details = fields.getModelDetails();
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getValue()).to.eventually.equal('');
          expect(attr.isDirty()).to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.setValue('${today}');
          expect(field.getValue()).to.eventually.equal('${today}');
          expect(attr.isDirty()).to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.setValue('');
          expect(attr.isDirty()).to.eventually.be.false;
        });
      });
    });
  });

  it('should allow editing of object property type value and restoring initial model value', () => {
    openPage('en', 'bg', 'MX1001');

    let resource = fields.getField('resource');
    resource.showAttributes();
    let details = fields.getModelDetails();
    details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
      attr.getField().then(field => {
        expect(field.getValue()).to.eventually.equal('');
        expect(attr.isDirty()).to.eventually.be.false;

        // change the value and expect the attribute to be dirty
        field.setValue('${currentUser.id}');
        expect(field.getValue()).to.eventually.equal('${currentUser.id}');
        expect(attr.isDirty()).to.eventually.be.true;

        // restore the value and expect the attribute to be pristine
        field.setValue('');
        expect(attr.isDirty()).to.eventually.be.false;
      });
    });
  });

  it('should allow editing of boolean values and restoring initial model value', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let checkbox = items[5];
      checkbox.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(attr.isDirty()).to.eventually.be.false;
          expect(field.isSelected()).to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.toggleCheckbox();
          expect(attr.isDirty()).to.eventually.be.true;
          expect(field.isSelected()).to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.toggleCheckbox();
          expect(attr.isDirty()).to.eventually.be.false;
          expect(field.isSelected()).to.eventually.be.false;
        });
      });
    });
  });

  it('should allow editing of codelist values and restoring initial model value', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // edit and restore attribute which already has value
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getSelectedValue()).to.eventually.equal('APPROVED');
          expect(attr.isDirty()).to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.selectOption('Completed');
          expect(field.getSelectedValue()).to.eventually.equal('COMPLETED');
          expect(attr.isDirty()).to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.selectOption('Approved');
          expect(field.getSelectedValue()).to.eventually.equal('APPROVED');
          expect(attr.isDirty()).to.eventually.be.false;
        });
      });

      // edit and restore attribute which doesn't have value
      let type = items[2];
      type.showAttributes();
      details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getSelectedValue()).to.eventually.equal(null);
          expect(attr.isDirty(), 'Value attribute should be pristine initially!').to.eventually.be.false;

          // change the value and expect the attribute to be dirty
          field.selectOption('Main Project');
          expect(field.getSelectedValue()).to.eventually.equal('PR0001');
          expect(attr.isDirty(), 'Value attribute should be dirty!').to.eventually.be.true;

          // restore the value and expect the attribute to be pristine
          field.clearField();
          expect(field.getSelectedValue()).to.eventually.equal(null);
          expect(attr.isDirty(), 'Value attribute should be pristine!').to.eventually.be.false;
        });
      });
    });
  });

  it('should reload codevalues after change of codelist number', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('codeList').then(attr => {
        let select = attr.getField();
        select.selectOption('3 - Level');
      });

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getSelectedValue()).to.eventually.equal(null);
          expect(field.getMenuValues()).to.eventually.eql(['LOW', 'MEDIUM', 'HIGH']);
        });
      });
    });
  });

  it('should switch the value editor properly after typeOption change', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        // select 'Alpha Numeric' option by index because option labels have similar labels which can cause wrong option
        // to be selected if option is selected by label's text
        attr.getField().selectFromMenu(null, '1', true);
      });

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getValue()).to.eventually.equal('');
        });
      });
    });
  });

  it('should reset initial values when cancel changes is clicked', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let state = items[1];
      state.showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('typeOption').then(attr => {
        // select 'Alpha Numeric' option by index
        attr.getField().selectFromMenu(null, '1', true);
      });
      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getValue()).to.eventually.equal('');
        });
      });

      fields.getModelControls().getModelCancel().click();

      details.getBehaviourAttributesPanel().getAttribute('value').then(attr => {
        attr.getField().then(field => {
          expect(field.getSelectedValue()).to.eventually.equal('APPROVED');
          expect(field.getMenuValues()).to.eventually.eql(['APPROVED', 'COMPLETED']);
        });
      });

    });
  });

  it('should not change parent attribute when child attribute is changed', () => {

    // firstly open the parent model and change an attribute without saving
    openPage('en', 'bg', 'EO1001');
    let tree = sandbox.getModelTree();

    let field = fields.getField('description');
    field.showAttributes();

    let details = fields.getModelDetails();

    details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
      expect(attr.isDirty()).to.eventually.be.false;
      let field = attr.getField();
      expect(field.getValue()).to.eventually.equal('');
      field.setValue(null, '1');

      // attribute should be dirty
      expect(attr.isDirty()).to.eventually.be.true;
    });

    // open Media model and remove the value from order attribute
    tree.toggleNode('Abstract');
    tree.getNode('Media').openObject();
    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();

    field = fields.getField('description');
    field.showAttributes();

    details = fields.getModelDetails();

    details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
      // attribute should be dirty
      expect(attr.isDirty()).to.eventually.be.true;
      let field = attr.getField();
      field.setValue(null, '');
      expect(modelData.isFieldsSectionModified()).to.eventually.be.true;
    });

    tree.getNode('entity').openObject();
    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();

    field = fields.getField('description');
    field.showAttributes();

    details = fields.getModelDetails();

    details.getBehaviourAttributesPanel().getAttribute('order').then(attr => {
      // attribute should be dirty and should not have the child`s value
      expect(attr.isDirty()).to.eventually.be.true;
      let field = attr.getField();
      expect(field.getValue()).to.eventually.equal('1');
    });
  });

  it('should mark attribute as inherited when the value is changed to the parent`s value', () => {

    // firstly open the parent model and change an attribute without saving
    openPage('en', 'bg', 'EO1001');
    let tree = sandbox.getModelTree();

    let field = fields.getField('description');
    field.showAttributes();

    let details = fields.getModelDetails();

    let panel = details.getBehaviourAttributesPanel();
    panel.getAttribute('order').then(attr => {

      expect(panel.canRestoreAttribute('order')).to.eventually.be.false;
      let field = attr.getField();
      expect(field.getValue()).to.eventually.equal('');
      field.setValue(null, '1');
    });

    // open Media model and change the value of order attribute
    tree.toggleNode('Abstract');
    tree.getNode('Media').openObject();
    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();

    field = fields.getField('description');
    field.showAttributes();

    details = fields.getModelDetails();

    panel = details.getBehaviourAttributesPanel();
    panel.getAttribute('order').then(attr => {
      // attribute should be inherited
      expect(panel.canRestoreAttribute('order')).to.eventually.be.false;
      let field = attr.getField();
      field.setValue(null, '12');
      expect(panel.canRestoreAttribute('order')).to.eventually.be.true;

      // now when the parent value is set, attribute should be inherited
      field.setValue(null, '1');
      expect(panel.canRestoreAttribute('order')).to.eventually.be.false;
    });
  });
});