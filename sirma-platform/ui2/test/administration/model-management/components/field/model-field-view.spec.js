import {ModelFieldView} from 'administration/model-management/components/field/model-field-view';
import {ModelField} from 'administration/model-management/model/model-field';

describe('ModelFieldView', () => {

  let field;
  let modelFieldView;

  beforeEach(() => {
    field = new ModelField();
    modelFieldView = new ModelFieldView();
    modelFieldView.model = field;
    modelFieldView.onModelSelected = sinon.spy();
  });

  it('should notify when the field is selected', () => {
    modelFieldView.selectModel();
    expect(modelFieldView.onModelSelected.calledWith({model: field})).to.be.true;
  });
});