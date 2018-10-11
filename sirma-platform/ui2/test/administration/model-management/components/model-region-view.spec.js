import {ModelRegionView} from 'administration/model-management/components/model-region-view';
import {ModelRegion} from 'administration/model-management/model/model-region';

describe('ModelRegionView', () => {

  let region;
  let modelRegionView;

  beforeEach(() => {
    region = new ModelRegion();
    modelRegionView = new ModelRegionView();
    modelRegionView.model = region;
  });

  it('should resolve properly region visibility', () => {
    region.getView().setVisible(false);
    expect(modelRegionView.isRegionVisible()).to.be.false;
  });

  it('should toggle region visibility', () => {
    region.getView().setVisible(false);
    modelRegionView.onRegionToggle();

    expect(modelRegionView.isRegionVisible()).to.be.true;
  });

});