var InstanceListSandboxPage = require('./instance-list').InstanceListSandboxPage;
var InstanceList = require('./instance-list').InstanceList;

describe('InstanceList', function () {
  var page = new InstanceListSandboxPage();

  beforeEach(function () {
    page.open();
  });

  it('should list provided instances', function () {
    var list = page.getNoSelectionList();
    expect(list.getItemsCount()).to.eventually.eq(4);
  });

  it('should support single selection', function () {
    var list = page.getSingleSelectionList();

    list.getItems().then((items) => {
      items.forEach((item) => {
        expect(item.getSelectType()).to.eventually.eq('single');
      });

      items[0].select();

      page.getItemsInputValue().then((value) => {
        expect(value).to.deep.eq([1]);
      });
    });
  });

  it('should support milti selection', function () {
    var list = page.getMultipleSelectionList();

    list.getItems().then((items) => {
      items.forEach((item) => {
        expect(item.getSelectType()).to.eventually.eq('multiple');
      });

      items[0].select();
      items[1].select();

      page.getItemsInputValue().then((value) => {
        expect(value).to.deep.eq([1, 2]);
      });
    });
  });

  it('should have the select all button enabled and visible in multi selection', function () {
    var list = page.getMultipleSelectionList();
    expect(list.getSelectAllButton().isDisplayed()).to.eventually.be.true;
  });

  it('should have the deselect all button enabled and visible in multi selection', function () {
    var list = page.getMultipleSelectionList();
    expect(list.getDeselectAllButton().isDisplayed()).to.eventually.be.true;
  });

  it('should be able to batch deselect all instances that are selected', function () {
    var list = page.getMultipleSelectionList();

    list.getItems().then((items) => {
      items[0].select();
      items[1].select();

      list.getDeselectAllButton().click().then(() => {
        page.getItemsInputValue().then((value) => {
          expect(value).to.deep.eq([]);
        });
      });
    });
  });

  it('should be able to batch deselect all instances at once', function () {
    var list = page.getMultipleSelectionList();

    list.getDeselectAllButton().click().then(() => {
      page.getItemsInputValue().then((value) => {
        expect(value).to.deep.eq([]);
      });
    });
  });

  it('should be able to batch select all instances at once', function () {
    var list = page.getMultipleSelectionList();

    list.getSelectAllButton().click().then(() => {
      page.getItemsInputValue().then((value) => {
        expect(value).to.deep.eq([1,2,3,4]);
      });
    });
  });

  it('should support exclusion by istance id', function () {
    var list = page.getExclusionsList();
    list.getItems().then((items) => {
      expect(items[0].isDisabled()).to.eventually.eq(true);
    });
  });
});