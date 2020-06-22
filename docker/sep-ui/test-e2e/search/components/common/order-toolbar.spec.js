var OrderToolbar = require('./order-toolbar.js').OrderToolbar;
var OrderToolbarSandbox = require('./order-toolbar.js').OrderToolbarSandbox;

describe('OrderToolbar', () => {

  var orderToolbar;
  var page = new OrderToolbarSandbox();

  beforeEach(() => {
    page.open();
    orderToolbar = page.getOrderToolbar();
  });

  it('should have the order by options present', () => {
    expect(orderToolbar.getOrderByElement().isPresent()).to.eventually.be.true;
  });

  it('should have the order by direction present', () => {
    expect(orderToolbar.getOrderDirectionButton().isPresent()).to.eventually.be.true;
  });

  it('should have default order by value present', () => {
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('zero');
  });

  it('should have default order by direction present', () => {
    expect(orderToolbar.getOrderDirection()).to.eventually.eq(OrderToolbar.DESCENDING);
  });

  it('should be able to change between order by options', () => {
    orderToolbar.selectOrderByOption(3);
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('three');
  });

  it('should be able to change between order by directions', () => {
    orderToolbar.toggleOrderDirection();
    expect(orderToolbar.getOrderDirection()).to.eventually.eq(OrderToolbar.ASCENDING);
  });

  it('should be able to disable the order toolbar', () => {
    page.toggleToolbarState();
    expect(orderToolbar.getOrderDirectionButton().isEnabled()).to.eventually.be.false;
    expect(orderToolbar.getOrderByDropdownButton().isEnabled()).to.eventually.be.false;
  });

  it('should deliver proper payload on order by option change', () => {
    orderToolbar.selectOrderByOption(3);
    expect(page.getOrderByValue()).to.eventually.eq('3');
  });

  it('should deliver proper payload on direction change', () => {
    orderToolbar.toggleOrderDirection();
    expect(page.getOrderDirectionValue()).to.eventually.eq('asc');
  });

  it('should not be able to select disabled option', () => {
    orderToolbar.selectOrderByOption(4);
    // default option is preserved and no selection has been made
    expect(orderToolbar.getOrderByOption()).to.eventually.eq('zero');
  });

  it('should have the final option present but disabled', () => {
    expect(orderToolbar.isOptionPresent(4)).to.eventually.be.true;
    expect(orderToolbar.isOptionDisabled(4)).to.eventually.be.true;
  });
});