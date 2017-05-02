import {Region} from 'form-builder/region/region';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {DefinitionModelProperty} from 'models/definition-model';

describe('Region', () => {

  describe('fieldViewModelSubscription', ()=> {
    it('should subscribe to fieldViewModel and hide/show depending on its configuration change', ()=> {
      let fakeElement = {hide: sinon.spy(), show: sinon.spy()};
      Region.prototype.fieldViewModel = new DefinitionModelProperty({rendered: true, preview: true});
      let region = new Region(fakeElement, IdocMocks.mockScope());
      region.fieldViewModel.rendered = false;
      region.fieldViewModel.rendered = true;
      // testing if different property might trigger show/hide also.
      region.fieldViewModel.preview = false;

      expect(fakeElement.hide.calledOnce).to.be.true;
      expect(fakeElement.show.calledOnce).to.be.true;
    });
  });

  describe('isRendered', () => {
    it('should resolve if the region should be rendered or not according to the viewMode.rendered value', () => {
      Region.prototype.widgetConfig = {};
      Region.prototype.fieldViewModel = {
        subscribe: sinon.spy()
      };

      let region = new Region({}, IdocMocks.mockScope());
      testData.forEach((data) => {
        region.fieldViewModel = data.viewModel;
        expect(region.isRendered()).to.equal(data.expected);
      });
    });
  });

  let testData = [
    {viewModel: {rendered: true}, expected: true},
    {viewModel: {rendered: false}, expected: false},
    {viewModel: {}, expected: true}
  ]
});