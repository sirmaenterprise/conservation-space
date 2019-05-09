import {InstanceSuggestSelect} from 'components/select/instance/instance-suggest-select';
import {EventEmitter} from 'common/event-emitter';
import _ from 'lodash';

import {SelectMocks} from 'test/components/select-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('InstanceSuggestSelect', () => {
  let select;
  beforeEach(() => {
    InstanceSuggestSelect.prototype.config = {eventEmitter: new EventEmitter()};
    select = new InstanceSuggestSelect(SelectMocks.mockElement(), mock$scope(), SelectMocks.mockTimeout(), sinon.spy(() => {}));
  });

  it('should subscribe to select event', () => {
    let onSpy = select.$element.on;
    select.ngOnInit();

    // first tree calls are not for this test
    expect(onSpy.callCount).to.equal(6);
    expect(onSpy.getCall(3).args[0]).to.eq('select2:selecting');
    expect(_.isFunction(onSpy.getCall(3).args[1])).to.be.true;
    expect(onSpy.getCall(4).args[0]).to.eq('select2:unselecting');
    expect(_.isFunction(onSpy.getCall(4).args[1])).to.be.true;
    expect(onSpy.getCall(5).args[0]).to.eq('select2:unselect');
    expect(_.isFunction(onSpy.getCall(5).args[1])).to.be.true;
  });

  it('should create actual config and parse search results', () => {
    sinon.stub(select.$element, 'data').returns({
      selection: {
        $search: {
          val: () => {
            return 'alt'
          }
        }
      }
    });

    select.config.selectionPool = new Map();
    let response = {
      data: {
        values: [
          {id:'1', properties:{altTitle:'alt1'}, headers: {breadcrumb_header:'header1'}},
          {id:'2', properties:{altTitle:'alt2'}, headers: {breadcrumb_header:'header2'}},
          {id:'3', properties:{altTitle:'alt3'}, headers: {breadcrumb_header:'header3'}}
        ]
      },
      config: {
        data: {
          keywords: 'alt'
        }
      }
    };

    let expected = {
      results: [
        {id: '1', altTitle: 'alt1', disabled: false, text:'header1'},
        {id: '2', altTitle: 'alt2', disabled: false, text:'header2'},
        {id: '3', altTitle: 'alt3', disabled: false, text:'header3'}
      ]
    };
    select.createActualConfig();

    let data = select.actualConfig.ajax.processResults(response);
    expect(data).to.deep.equal(expected);
  });
});