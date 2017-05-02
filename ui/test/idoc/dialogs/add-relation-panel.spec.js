import {AddRelationPanel} from 'idoc/dialogs/add-relation-panel';

describe('AddRelationPanel', () => {
  var dialog;
  beforeEach(() => {
    dialog = new AddRelationPanel();
  });

  it('should not build identifiers based on selected items', () => {
    dialog.config.onObjectSelectorChanged({});
    expect(dialog.config.selectedItemsIds).to.not.exist;
    expect(dialog.config.selectedItems).to.be.empty;
  });

  it('should build identifiers based on selected items', () => {
    let payload = {
      selectedItems: [{
        default_header: 'default_header',
        id: 'emf:123456',
        type: 'type'
      }]
    };
    dialog.config.onObjectSelectorChanged(payload);
    expect(dialog.config.selectedItemsIds).to.exist;
    expect(dialog.config.selectedItemsIds.length).to.be.equal(1);
  });
});