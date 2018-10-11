import {PickerBasket} from 'search/picker/picker-basket';

describe('PickerBasket', () => {

  var pickerBasket;
  beforeEach(() => {
    PickerBasket.prototype.config = undefined;
  });
  afterEach(() => {
    PickerBasket.prototype.config = undefined;
  });

  it('should have default configuration & values for selection', () => {
    pickerBasket = new PickerBasket();
    expect(pickerBasket.config).to.exist;
    expect(pickerBasket.config.selectAll).to.be.false;
    expect(pickerBasket.config.selectableItems).to.be.true;
    expect(pickerBasket.config.singleSelection).to.be.true;
    expect(pickerBasket.config.emptyListMessage).to.exist;
    expect(pickerBasket.config.linkRedirectDialog).to.be.true;
  });

  it('should configure the selected items array', () => {
    var selectedItems = ['emf:123'];
    PickerBasket.prototype.config = {
      selectedItems: selectedItems
    };
    pickerBasket = new PickerBasket();
    // Should keep the reference!
    expect(pickerBasket.selectedItems).to.equal(selectedItems);
  });

  it('should configure the internal instance list component', () => {
    PickerBasket.prototype.config = {
      singleSelection: false,
      emptyListMessage: 'message',
      selectionHandler: () => {
        return 'handler';
      }
    };
    pickerBasket = new PickerBasket();

    expect(pickerBasket.basketListConfig).to.exist;
    expect(pickerBasket.basketListConfig.selectableItems).to.be.true;
    expect(pickerBasket.basketListConfig.singleSelection).to.be.false;
    expect(pickerBasket.basketListConfig.linkRedirectDialog).to.be.true;
    expect(pickerBasket.basketListConfig.emptyListMessage).to.equal('message');

    expect(pickerBasket.basketListConfig.selectionHandler).to.exist;
    expect(pickerBasket.basketListConfig.selectionHandler()).to.equal('handler')
  });

});