import {TreeSelect} from 'components/select/tree-select/tree-select';
import {SelectMocks} from 'test/components/select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('Tree select', function () {

  var select;

  beforeEach(function() {
    var data = [{
      id: 'concrete', text: 'Concrete', children: [{
          id: 'reinforced', text: 'Reinforced', children: [{
            id: 'fiber', text: 'Fiber'
          },
          {
            id: 'rebar', text: 'Rebar'
          }]
        },
        {
          id: 'prestressed', text: 'Prestressed'
        }
      ]
    },
    {
      id: 'metal', text: 'Metal', children: [{
        id: 'steel', text: 'Steel', children: [{
            id: 'hot_rolled', text: 'Hot rolled'
          },
          {
            id: 'cold_formed', text: 'Cold formed'
          }]
        },
        {
          id: 'aluminium', text: 'Aluminium'
        }
      ]
    }];

    let config =  {
      data: data
    };

    TreeSelect.prototype.config = config;

    let $element = SelectMocks.mockElement();

    let originalFind = $element.find;

    $element.find = function () {
      let result = originalFind();
      result.select2ToTree = function () {};
      return result
    };

    select = new TreeSelect($element, mock$scope(), SelectMocks.mockTimeout());
  });

  it('should filter items based on a filter criteria while showing all the parents of options that match the filter criteria', function() {
    const FILTER = 'ibe';

    expect(callFilter('concrete', FILTER).id).to.be.equal('concrete');
    expect(callFilter('reinforced', FILTER).id).to.be.equal('reinforced');
    expect(callFilter('fiber', FILTER).id).to.be.equal('fiber');

    expect(callFilter('rebar', FILTER)).to.be.null;
    expect(callFilter('metal', FILTER)).to.be.null;
  });

  function callFilter(nodeId, filterText) {
    return select.actualConfig.matcher({
      term: filterText
    }, {
      id: nodeId
    });
  }

});