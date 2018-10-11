import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {EntryItem} from 'layout/breadcrumb/breadcrumb-entry/entry-item';

describe('BreadcrumbEntryManager', function () {

  function mockEntry(objectId, persisted, header = 'header', instanceType = 'instanceType') {
    return new EntryItem(objectId, header, instanceType, persisted, 'stateUrl');
  }

  describe('getPreviousEntry()', () => {
    it('should return the entry before the last one in the entries list', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries.push(mockEntry(1));
      entryManager.entries.push(mockEntry(2));
      let previousEntry = entryManager.getPreviousEntry();
      expect(previousEntry.getId()).to.equal(1);
    });

    it('should return undefined if there is only one entry in the list', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries.push(mockEntry(1));
      let previousEntry = entryManager.getPreviousEntry();
      expect(previousEntry).to.be.undefined;
    });

    it('should return undefined if there is no entries in the list', () => {
      let entryManager = new BreadcrumbEntryManager();
      let previousEntry = entryManager.getPreviousEntry();
      expect(previousEntry).to.be.undefined;
    });
  });

  describe('removeEntry()', () => {
    it('should remove all entries with same objectId', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries = [
        new EntryItem(1, 'header1', 'documentinstance', true, 'url'),
        new EntryItem(2, 'header2', 'documentinstance', true, 'url'),
        new EntryItem(3, 'header3', 'documentinstance', true, 'url'),
        new EntryItem(2, 'header4', 'documentinstance', true, 'url')
      ];
      entryManager.removeEntry(2);
      expect(entryManager.entries.length).to.equal(2);
      expect(entryManager.entries[0].getId()).to.equal(1);
      expect(entryManager.entries[1].getId()).to.equal(3);
    });

    it('should remove an entry by provided objectId', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries = [
        new EntryItem(1, 'header', 'documentinstance', true, 'url'),
        new EntryItem(2, 'header', 'documentinstance', true, 'url'),
        new EntryItem(3, 'header', 'documentinstance', true, 'url'),
        new EntryItem(4, 'header', 'documentinstance', true, 'url')
      ];
      entryManager.removeEntry(2);
      expect(entryManager.entries.length).to.equal(3);
      expect(entryManager.entries[0].getId()).to.equal(1);
      expect(entryManager.entries[1].getId()).to.equal(3);
      expect(entryManager.entries[2].getId()).to.equal(4);
    });

    it('should not modify the entries list if there is no entry with provided id', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries = [
        new EntryItem(1, 'header', 'documentinstance', true, 'url'),
        new EntryItem(2, 'header', 'documentinstance', true, 'url'),
        new EntryItem(3, 'header', 'documentinstance', true, 'url'),
        new EntryItem(4, 'header', 'documentinstance', true, 'url')
      ];
      entryManager.removeEntry(10);
      expect(entryManager.entries.length).to.equal(4);
    });
  });

  describe('compactEntries', () => {
    it('should remove duplicated successive entries from the breadcrumb', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries = [
        new EntryItem(1, 'header', 'documentinstance', true, 'url'),
        new EntryItem(1, 'header', 'documentinstance', true, 'url'),
        new EntryItem(2, 'header', 'documentinstance', true, 'url'),
        new EntryItem(1, 'header', 'documentinstance', true, 'url')
      ];
      entryManager.compactEntries();
      expect(entryManager.entries.length).to.equal(3);
      expect(entryManager.entries[0].getId()).to.equal(1);
      expect(entryManager.entries[1].getId()).to.equal(2);
      expect(entryManager.entries[2].getId()).to.equal(1);
    });
  });

  describe('add()', () => {
    it('should remove entry when not persisted', function () {
      let entry = mockEntry(1, false);
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries.push(entry);
      let testEntry = mockEntry(2, true);

      expect(entryManager.add(testEntry)).to.be.true;
      expect(entryManager.getEntries().length).to.be.one;
      expect(entryManager.getLastEntry()).to.deep.equal(testEntry);
    });

    it('should update last entry when they have the same id', function () {
      let entry = mockEntry(1, true, 'Old Header', 'Old Type');
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries.push(entry);
      let testEntry = mockEntry(1, true, 'New Header', 'New Type');

      expect(entryManager.add(testEntry)).to.be.false;
      expect(entryManager.getEntries().length).to.be.one;
      expect(entryManager.getLastEntry()).to.deep.equal(testEntry);
    });

    it('should add new entity', function () {
      let entry = mockEntry(1);
      let entryManager = new BreadcrumbEntryManager();
      entryManager.entries.push(entry);
      let testEntry = mockEntry(2);

      expect(entryManager.add(testEntry)).to.be.true;
      expect(entryManager.getLastEntry()).to.deep.equal(testEntry);
    });

    it('should clear the last not persisted entry', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.add(mockEntry(1, false));
      entryManager.add(mockEntry(2, true));
      expect(entryManager.entries.length).to.equal(1);
      expect(entryManager.entries[0].getId()).to.equal(2);
    });

    it('should compact entries to avoid consecutive equal entries to appear in breadcrumb', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.add(mockEntry(1, true));
      entryManager.add(mockEntry(1, true));
      entryManager.add(mockEntry(2, true));
      entryManager.add(mockEntry(2, true));
      expect(entryManager.entries.length).to.equal(2);
      expect(entryManager.entries[0].getId()).to.equal(1);
      expect(entryManager.entries[1].getId()).to.equal(2);
    });
  });

  describe('back', () => {
    it('should clear the last entry from the breadcrumb', () => {
      let entryManager = new BreadcrumbEntryManager();
      entryManager.add(mockEntry(1, true));
      entryManager.add(mockEntry(2, true));
      entryManager.back();
      expect(entryManager.entries.length).to.equal(1);
    });
  });

  it('should clear all entities both in the viewModel and the model inside the breadcrumbEntryManager when clear() is called', function () {
    let entry = mockEntry(1);
    let entryManager = new BreadcrumbEntryManager();
    entryManager.entries.push(entry);
    entryManager.clear();

    expect(entryManager.getEntries().length).to.be.zero;
  });

  it('should clear all entities after the index passed to clear()', function () {
    let entry1 = mockEntry(1);
    let entry2 = mockEntry(2);
    let entryManager = new BreadcrumbEntryManager();
    entryManager.entries.push(entry1);
    entryManager.entries.push(entry2);
    entryManager.clear(1);

    expect(entryManager.getLastEntry()).to.deep.equal(entry1);
    expect(entryManager.getEntries().length).to.be.one;
  });
});