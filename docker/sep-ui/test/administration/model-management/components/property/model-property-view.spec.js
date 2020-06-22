import {ModelPropertyView} from 'administration/model-management/components/property/model-property-view';
import {ModelProperty} from 'administration/model-management/model/model-property';

describe('ModelPropertyView', () => {

  let property;
  let modelPropertyView;

  beforeEach(() => {
    property = new ModelProperty();
    modelPropertyView = new ModelPropertyView();
    modelPropertyView.model = property;
    modelPropertyView.onModelSelected = sinon.spy();
  });

  it('should notify when the property is selected', () => {
    modelPropertyView.selectModel();
    expect(modelPropertyView.onModelSelected.calledWith({model: property})).to.be.true;
  });
});