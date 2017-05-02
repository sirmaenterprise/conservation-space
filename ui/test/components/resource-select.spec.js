import {ResourceSelect} from 'components/select/resource/resource-select';
import {SelectMocks} from './select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('ResourceSelect', () => {

  describe('#createActualConfig without external config', () => {
    let restService = {
      getResource: sinon.spy(() => {
        return new Promise((resolve) => {
          resolve({
            data: {
              id:'emf:1'
            }
          });
        });
      })
    };
    let select = new ResourceSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), restService);

    it('should allow multiple selection by default', () => {
      expect(select.actualConfig.multiple).to.be.true;
    });

    it('should search only for users by default', () => {
      restService.getResources = sinon.spy();
      select.config.dataLoader(mockParams());

      expect(restService.getResources.calledOnce).to.be.true;
      let passedArguments = restService.getResources.getCall(0).args[0];
      expect(passedArguments.includeUsers).to.equal(true);
      expect(passedArguments.includeGroups).to.equal(false);
      expect(passedArguments.term).to.equal('term');
    });

    it('should not transform anything if the data is empty', () => {
      var expected = {
        results: []
      };
      var transformed = select.actualConfig.ajax.processResults({});
      expect(transformed).to.deep.equal(expected);

      transformed = select.actualConfig.ajax.processResults({data:{}});
      expect(transformed).to.deep.equal(expected);
    });

    it('should correctly transform the provided data', ()=> {
      var provided = {
        data : {
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
      var expected = {
        results: [
          {
            id: 'id',
            text: 'title',
            type: 'user',
            value: 'val'
          }
        ]
      };
      var actual = select.actualConfig.ajax.processResults(provided);
      expect(actual).to.deep.equal(expected);
    });

    it('should correctly resolve resources in mapper', (done) => {
      var resolvedItems = select.config.mapper(['emf:1']);
      resolvedItems.then((result) => {
        expect(result[0].id).to.equal('emf:1');
        done();
      }).catch(done);
    });
  });

  describe('#createActualConfig with external config for single group selection.', () => {
    let restService = {};
    let select = new ResourceSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), restService);
    select.config.multiple = false;
    select.config.includeUsers = false;
    select.config.includeGroups = true;
    select.createActualConfig();

    it('should allow only one value.', () => {
      expect(select.actualConfig.multiple).to.be.false;
    });

    it('should search only for groups.', () => {
      restService.getResources = sinon.spy();
      select.config.dataLoader(mockParams());

      expect(restService.getResources.calledOnce).to.be.true;
      expect(restService.getResources.getCall(0).args[0].includeUsers).to.equal(false);
      expect(restService.getResources.getCall(0).args[0].includeGroups).to.equal(true);
    });
  });

  function mockParams() {
    return {
      data: {
        q: 'term'
      }
    };
  }
});
