import {ModelContainerView} from 'administration/model-management/components/container/model-container-view';
import {ModelRegion} from 'administration/model-management/model/model-region';

describe('ModelContainerView', () => {

  let region;
  let modelContainerView;

  beforeEach(() => {
    region = new ModelRegion();
    modelContainerView = new ModelContainerView();
    modelContainerView.model = region;
    modelContainerView.onModelSelected = sinon.spy();
  });

  it('should notify when the region is selected', () => {
    let eventStub = {
      stopPropagation: sinon.spy()
    };
    modelContainerView.selectModel(eventStub);
    expect(modelContainerView.onModelSelected.calledWith({model: region})).to.be.true;
    expect(eventStub.stopPropagation.calledOnce).to.be.true;
  });
});