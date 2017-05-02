import {RelationshipsSelect} from 'components/select/relationships/relationships-select';
import {SelectMocks} from '../../select-mocks';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('Relationships Select', ()=> {
  var scope = mock$scope();
  var relationshipsSelect;
  var data = [
    {
      domainClass: 'ptop:Test', id: 'emf:Test1',
      name: 'emf:Test1', title: 'Test1'
    },
    {
      domainClass: 'ptop:Test', id: 'emf:Test2',
      name: 'emf:Test2', title: 'Test'
    },
    {
      domainClass: 'ptop:Test', id: 'emf:Test3',
      name: 'emf:Test3', title: 'Test3'
    }];

  var response = {
    data: data
  };

  beforeEach(()=> {
    // Fixes scope issues in Karma
    RelationshipsSelect.prototype.config = undefined;
    relationshipsSelect = new RelationshipsSelect(SelectMocks.mockElement(), scope, {}, relationServiceMock(response));
  });

  afterEach(() => {
    // Fixes scope issues in Karma
    RelationshipsSelect.prototype.config = undefined;
  });

  it('should be configured properly', ()=> {
    relationshipsSelect.createActualConfig();
    expect(relationshipsSelect.config.multiple).to.be.true;
    expect(relationshipsSelect.config.delay).to.equal(250);
  });

  describe('data converter', ()=> {
    it('should convert data correctly', ()=> {
      let convertedData = relationshipsSelect.config.dataConverter(response);
      expect(convertedData).to.eql(trimData(data));
    });

    it('Should return an empty array if there is nothing to convert', ()=> {
      expect(relationshipsSelect.config.dataConverter(([]))).to.eql([]);
    });
  });
});

function trimData(values) {
  return values.map((item)=> {
    return {id: item.id, text: item.title};
  });
}
function relationServiceMock(response) {
  return {
    find: (data) => {
      return PromiseStub.resolve(response);
    }
  }
}