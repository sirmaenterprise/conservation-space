'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Models management field code value attribute', () => {

  let fields;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    fields = modelData.getFieldsSection();
  }

  it('should have code value attribute present and initialized from provided data', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[2].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('multiValued').then(attr => {
        // should be marked as multi valued field by default
        expect(attr.getField().isSelected()).to.eventually.be.true;
      });

      details.getBehaviourAttributesPanel().getAttribute('codeValue').then(attr => {
        // since field is marked as multi valued extract the data through multi select
        expect(attr.getField().getSelectedValue()).to.eventually.deep.eq('PR0002');
      });
    });
  });

  it('should change the available value options when the source code list is changed', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      items[2].showAttributes();
      let details = fields.getModelDetails();

      details.getBehaviourAttributesPanel().getAttribute('codeValue').then(attr => {
        expect(attr.getField().getSelectedValue()).to.eventually.deep.eq('PR0002');
        expect(attr.getField().getMenuValues()).to.eventually.deep.eq([
          'PR0001', 'PR0002', 'PR0003', 'PR0004', 'PR0005', 'PR0006', 'PR0007'
        ]);

        return details.getBehaviourAttributesPanel().getAttribute('codeList');
      }).then(attr => {
        attr.getField().selectOption(1);

        expect(attr.isDirty()).to.eventually.be.true;
        expect(modelData.isFieldsSectionModified()).to.eventually.be.true;

        return details.getBehaviourAttributesPanel().getAttribute('codeValue');
      }).then(attr => {
        expect(attr.isDirty()).to.eventually.be.true;
        expect(attr.getField().getSelectedValue()).to.eventually.deep.eq(null);
        expect(attr.getField().getMenuValues()).to.eventually.deep.eq(['APPROVED', 'COMPLETED']);
      });
    });
  });
});