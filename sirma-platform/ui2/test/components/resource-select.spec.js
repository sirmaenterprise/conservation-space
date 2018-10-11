import {ResourceSelect} from 'components/select/resource/resource-select';
import {ResourceRestService} from 'services/rest/resources-service';
import {SelectMocks} from 'test/components/select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('ResourceSelect', () => {

  let restService;
  let select;

  beforeEach(() => {
    restService = stubResourceService();
    select = new ResourceSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), restService, PromiseStub);
  });

  describe('#createActualConfig without external config', () => {
    it('should allow multiple selection by default', () => {
      expect(select.actualConfig.multiple).to.be.true;
    });

    it('should search only for users by default', () => {
      select.config.dataLoader(mockParams());

      expect(restService.getResources.calledOnce).to.be.true;
      let passedArguments = restService.getResources.getCall(0).args[0];
      expect(passedArguments.includeUsers).to.equal(true);
      expect(passedArguments.includeGroups).to.equal(false);
      expect(passedArguments.term).to.equal('term');
    });

    it('should not transform anything if the data is empty', () => {
      let expected = {
        results: []
      };
      let transformed = select.actualConfig.ajax.processResults({});
      expect(transformed).to.deep.equal(expected);

      transformed = select.actualConfig.ajax.processResults({data: {}});
      expect(transformed).to.deep.equal(expected);
    });

    it('should correctly transform the provided data', () => {
      let provided = {
        data: {
          items: [
            {
              id: 'id',
              label: 'title',
              type: 'user',
              value: 'val'
            }
          ]
        }
      };
      let expected = {
        results: [
          {
            id: 'id',
            text: 'title',
            type: 'user',
            value: 'val'
          }
        ]
      };
      let actual = select.actualConfig.ajax.processResults(provided);
      expect(actual).to.deep.equal(expected);
    });

    it('should correctly resolve resources in mapper', () => {
      let resolvedItems = [];
      let resolvedItemsPromise = select.config.mapper(['emf:1']);
      resolvedItemsPromise.then(result => resolvedItems = result);
      expect(resolvedItems[0].id).to.equal('emf:1');
    });
  });

  describe('#createActualConfig with external config', () => {
    beforeEach(() => {
      select.config.multiple = false;
      select.config.includeUsers = false;
      select.config.includeGroups = true;
      select.createActualConfig();
    });

    it('should use externally provided resource converter instead of the default one', () => {
      select.config.resourceConverter = () => {
        return {
          id: 'emf:123'
        };
      };
      let provided = {
        data: {
          items: [
            {
              id: 'id',
              label: 'title',
              type: 'user',
              value: 'val'
            }
          ]
        }
      };
      let expected = {
        results: [
          {
            id: 'emf:123'
          }
        ]
      };
      let actual = select.actualConfig.ajax.processResults(provided);
      expect(actual).to.deep.equal(expected);
    });

    it('should allow only one value.', () => {
      expect(select.actualConfig.multiple).to.be.false;
    });

    it('should search only for groups.', () => {
      select.config.dataLoader(mockParams());

      expect(restService.getResources.calledOnce).to.be.true;
      expect(restService.getResources.getCall(0).args[0].includeUsers).to.equal(false);
      expect(restService.getResources.getCall(0).args[0].includeGroups).to.equal(true);
    });
  });

  function stubResourceService() {
    let serviceStub = stub(ResourceRestService);
    serviceStub.getResource.returns(PromiseStub.resolve({
      data: {
        id: 'emf:1'
      }
    }));
    return serviceStub;
  }

  function mockParams() {
    return {
      data: {
        q: 'term'
      }
    };
  }
});
