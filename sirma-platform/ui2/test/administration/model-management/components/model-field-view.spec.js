import {ModelFieldView} from 'administration/model-management/components/model-field-view';
import {ModelField} from 'administration/model-management/model/model-field';

describe('ModelFieldView', () => {

  let field;
  let modelFieldView;

  beforeEach(() => {
    field = new ModelField();
    modelFieldView = new ModelFieldView();
    modelFieldView.model = field;
  });

  it('should resolve properly field visibility', () => {
    field.getView().setVisible(false);
    expect(modelFieldView.isFieldVisible()).to.be.false;
  });

  it('should resolve properly parent visibility', () => {
    field.getView().setShowParent(false);
    expect(modelFieldView.isParentVisible()).to.be.false;
  });

});