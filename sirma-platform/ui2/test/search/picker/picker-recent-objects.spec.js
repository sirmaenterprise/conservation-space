import {PickerRecentObjects} from 'search/picker/picker-recent-objects';

describe('PickerRecentObjects', () => {

  var pickerRecentObjects;
  beforeEach(() => {
    pickerRecentObjects = new PickerRecentObjects();
  });

  it('should have default configuration & values for selection', () => {
    expect(pickerRecentObjects.config).to.exist;
    expect(pickerRecentObjects.config.predefinedTypes).to.deep.equal([]);
    expect(pickerRecentObjects.config.selectableItems).to.be.true;
    expect(pickerRecentObjects.config.singleSelection).to.be.true;
    expect(pickerRecentObjects.config.emptyListMessage).to.exist;
    expect(pickerRecentObjects.config.linkRedirectDialog).to.be.true;
  });

  it('should configure the selected items array', () => {
    var selectedItems = ['emf:123'];
    pickerRecentObjects.config.selectedItems = selectedItems;
    pickerRecentObjects.ngOnInit();
    // Should keep the reference!
    expect(pickerRecentObjects.selectedItems).to.equal(selectedItems);
  });

  it('should configure the internally used instance list component', () => {
    pickerRecentObjects.config = {
      selectableItems: true,
      singleSelection: false,
      linkRedirectDialog: true,
      emptyListMessage: 'message',
      selectionHandler: () => {
        return 'handler';
      },
      restrictionFilter: () => {
        return 'filter';
      }
    };
    pickerRecentObjects.ngOnInit();

    expect(pickerRecentObjects.recentObjectsListConfig.selectableItems).to.be.true;
    expect(pickerRecentObjects.recentObjectsListConfig.singleSelection).to.be.false;
    expect(pickerRecentObjects.recentObjectsListConfig.linkRedirectDialog).to.be.true;
    expect(pickerRecentObjects.recentObjectsListConfig.emptyListMessage).to.equal('message');
    expect(pickerRecentObjects.recentObjectsListConfig.selectionHandler()).to.equal('handler');
    expect(pickerRecentObjects.recentObjectsListConfig.identifiersFilter()).to.equal('filter');
  });

  it('should assign types filter with provided predefined types', () => {
    let predefinedTypes = ['emf:Document', 'chd:Book'];
    pickerRecentObjects.config.predefinedTypes = predefinedTypes;
    pickerRecentObjects.ngOnInit();
    expect(pickerRecentObjects.typesFilter).to.deep.equal(predefinedTypes);
  });
});