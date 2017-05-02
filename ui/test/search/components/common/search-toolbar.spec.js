import {SearchToolbar, ORDER_ASC, ORDER_DESC} from 'search/components/common/search-toolbar';

describe('SearchToolbar', () => {
  var toolbar;

  beforeEach(() => {
    toolbar = new SearchToolbar(mockScope());
  });

  it('should build order by select configuration based on the provided component configuration', () => {
    toolbar.orderByConfig = undefined;
    toolbar.config.orderByData = [{id: 'title'}];
    toolbar.config.orderBy = 'default';
    toolbar.createOrderByConfig();
    var expected = {
      width: '200px',
      hideSearchBox: true,
      data: [{id: 'title'}],
      defaultValue: 'default'
    };
    expect(toolbar.orderByConfig).to.deep.equal(expected);
  });

  it('should support data loader', () => {
    var loader = () => [];
    toolbar.orderByConfig = undefined;
    toolbar.config.orderByData = undefined;
    toolbar.config.orderByDataLoader = loader;
    toolbar.createOrderByConfig();
    expect(toolbar.orderByConfig.dataLoader).to.equal(loader);
  });

  it('should order by with correct arguments', () => {
    toolbar.callback = sinon.spy();
    toolbar.config.disabled = false;

    toolbar.config.orderBy = {id: 'title'};
    toolbar.onOrderByChanged();

    var expectedCriteria = {id: 'title'};
    expect(toolbar.callback.callCount).to.equal(1);
    expect(toolbar.callback.getCall(0).args[0].orderBy).to.deep.equal(expectedCriteria);
  });

  it('should order ascending', () => {
    toolbar.callback = sinon.spy();
    toolbar.config.orderDirection = ORDER_DESC;
    toolbar.toggleOrderDirection();

    expect(toolbar.callback.callCount).to.equal(1);
    expect(toolbar.callback.getCall(0).args[0].orderDirection).to.equal(ORDER_ASC);
  });

  it('should order descending', () => {
    toolbar.callback = sinon.spy();
    toolbar.config.orderDirection = ORDER_ASC;
    toolbar.toggleOrderDirection();

    expect(toolbar.callback.callCount).to.equal(1);
    expect(toolbar.callback.getCall(0).args[0].orderDirection).to.equal(ORDER_DESC);
  });

  it('should not trigger search callback if the order is not defined', () => {
    toolbar.config.orderBy = undefined;
    toolbar.callback = sinon.spy();
    toolbar.onOrderByChanged();
    expect(toolbar.callback.called).to.be.false;
  });
});

function mockScope() {
  return {
    $watch: ()=> {
    }
  };
}