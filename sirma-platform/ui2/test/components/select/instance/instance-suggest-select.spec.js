import {InstanceSuggestSelect} from 'components/select/instance/instance-suggest-select';
import _ from 'lodash';

import {SelectMocks} from 'test/components/select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('InstanceSuggestSelect', () => {
  let select;
  beforeEach(() => {
    select = new InstanceSuggestSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), sinon.spy(() => {}));
  });

  it('should subscribe to select event', () => {
    let onSpy = select.$element.on;
    select.ngOnInit();

    // first tree calls are not for this test
    expect(onSpy.callCount).to.equal(4);
    expect(onSpy.getCall(3).args[0]).to.eq('select2:select');
    expect(_.isFunction(onSpy.getCall(3).args[1])).to.be.true;
  });

  it('should create actual config and parse search results', () => {
    select.config.selectionPool = new Map();
    let response = {
      data: {
        values: [
          {id:'1', properties:{altTitle:'alt1'}},
          {id:'2', properties:{altTitle:'alt2'}},
          {id:'3', properties:{altTitle:'alt3'}}
        ]
      }
    };

    let expected = {
      results: [
        {id: '1', text: 'alt1', disabled: false},
        {id: '2', text: 'alt2', disabled: false},
        {id: '3', text: 'alt3', disabled: false}
      ]
    };
    select.createActualConfig();

    let data = select.actualConfig.ajax.processResults(response);
    expect(data).to.deep.equal(expected);
  });
});