import {OrderToolbar} from 'search/components/common/order-toolbar';
import {ORDER_ASC, ORDER_DESC} from 'search/order-constants';

describe('OrderToolbar', () => {

  var orderToolbar;

  beforeEach(() => {
    orderToolbar = new OrderToolbar();
  });

  it('should resolve the default specified order by', () => {
    orderToolbar.orderBy = 1;
    orderToolbar.orderByData = getOrderByData();

    let result = orderToolbar.getOrderByValue();
    expect(orderToolbar.order).to.eq('First');
    expect(orderToolbar.order).to.eq(result);
  });

  it('should properly resolve a given order by value', () => {
    orderToolbar.orderByData = getOrderByData();
    orderToolbar.orderBy = 3;

    let result = orderToolbar.getOrderByValue();
    expect(orderToolbar.order).to.eq('Third');
    expect(orderToolbar.order).to.eq(result);
  });

  it('should not resolve the specified order if it is non existent', () => {
    orderToolbar.orderBy = 10;
    orderToolbar.order = 'ConstOrder';
    orderToolbar.orderByData = getOrderByData();

    let result = orderToolbar.getOrderByValue();
    expect(orderToolbar.order).to.eq('ConstOrder');
    expect(orderToolbar.order).to.eq(result);
  });

  it('should correctly set order by value & call the component event when order is enabled', () => {
    let order = getOrderByData()[0];
    orderToolbar.orderDirection = ORDER_ASC;
    orderToolbar.onOrderChanged = sinon.spy();

    orderToolbar.onOrderSelected(order);
    expect(orderToolbar.orderBy).to.eq(order.id);
    expect(orderToolbar.onOrderChanged.calledOnce).to.be.true;
    expect(orderToolbar.onOrderChanged.getCall(0).args[0]).to.deep.eq({
      params: {
        orderBy: order.id,
        orderDirection: ORDER_ASC
      }
    });
  });

  it('should not set order by value & call the component event when order is disabled', () => {
    let order = getOrderByData()[0];
    order.disabled = true;
    orderToolbar.onOrderChanged = sinon.spy();

    orderToolbar.onOrderSelected(order);
    expect(orderToolbar.orderBy).to.not.eq(order.id);
    expect(orderToolbar.onOrderChanged.calledOnce).to.be.false;
  });

  it('should order ascending', () => {
    orderToolbar.onOrderChanged = sinon.spy();
    orderToolbar.orderDirection = ORDER_DESC;
    orderToolbar.orderBy = 'Test';
    orderToolbar.onOrderToggled();

    expect(orderToolbar.onOrderChanged.calledOnce).to.be.true;
    expect(orderToolbar.orderDirection).to.equal(ORDER_ASC);
    expect(orderToolbar.onOrderChanged.getCall(0).args[0]).to.deep.eq({
      params: {
        orderBy: 'Test',
        orderDirection: ORDER_ASC
      }
    });
  });

  it('should order descending', () => {
    orderToolbar.onOrderChanged = sinon.spy();
    orderToolbar.orderDirection = ORDER_ASC;
    orderToolbar.orderBy = 'Test';
    orderToolbar.onOrderToggled();

    expect(orderToolbar.onOrderChanged.calledOnce).to.be.true;
    expect(orderToolbar.orderDirection).to.equal(ORDER_DESC);
    expect(orderToolbar.onOrderChanged.getCall(0).args[0]).to.deep.eq({
      params: {
        orderBy: 'Test',
        orderDirection: ORDER_DESC
      }
    });
  });
});

function getOrderByData() {
  return [
    {
      id: 1,
      text: 'First',
      disabled: false
    }, {
      id: 2,
      text: 'Second',
      disabled: false
    }, {
      id: 3,
      text: 'Third',
      disabled: false
    }, {
      id: 4,
      text: 'Fourth',
      disabled: false
    },
  ];
}