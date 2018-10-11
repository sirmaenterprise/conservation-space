import {Pagination} from 'search/components/common/pagination';

var pagination;
describe('Pagination', () => {

  beforeEach(()=> {
    pagination = new Pagination(mockScope());
    pagination.callback = sinon.spy();
  });

  it('could be used without external configuration', ()=> {
    expect(pagination.config.pageSize).to.equal(25);
    expect(pagination.config.pageRotationStep).to.equal(2);
  });

  it('should hide pagination if results are not above the page size', () => {
    pagination.config.pageSize = 25;
    pagination.total = 0;
    expect(pagination.showPagination()).to.be.false;
    pagination.total = 5;
    expect(pagination.showPagination()).to.be.false;
    pagination.total = 25;
    expect(pagination.showPagination()).to.be.false;
  });

  it('should show pagination if results are above page size', () => {
    pagination.config.pageSize = 25;
    pagination.total = 26;
    expect(pagination.showPagination()).to.be.true;
  });

  it('should disable first page button if currently at first page', () => {
    pagination.page = 1;
    expect(pagination.disableFirstPageButton()).to.be.true;
  });

  it('should disable first page button if the component is disabled', () => {
    pagination.page = 2;
    pagination.config.disabled = true;
    expect(pagination.disableFirstPageButton()).to.be.true;
  });

  it('should enable first page button if not disabled & not at 1 page', () => {
    pagination.page = 2;
    pagination.config.disabled = false;
    expect(pagination.disableFirstPageButton()).to.be.false;
  });

  it('should disable last page button if currently at last page', () => {
    pagination.total = 26;
    pagination.page = 2;
    expect(pagination.disableLastPageButton()).to.be.true;
  });

  it('should disable last page button if currently the component is disabled', () => {
    pagination.total = 26;
    pagination.config.disabled = true;
    expect(pagination.disableLastPageButton()).to.be.true;
  });

  it('should enable last page button if not disabled & not at last page', () => {
    pagination.total = 26;
    pagination.page = 1;
    pagination.config.disabled = false;
    expect(pagination.disableLastPageButton()).to.be.false;
  });

  it('should hide first and last page buttons if below max visible pages', () => {
    pagination.config.pageRotationStep = 2;
    pagination.total = 26;
    expect(pagination.showFirstLastButtons()).to.be.false;
  });

  it('should show first and last page buttons if above max visible pages', () => {
    pagination.config.pageRotationStep = 2;
    pagination.total = 375;
    expect(pagination.showFirstLastButtons()).to.be.true;
  });

  it('should show first and last page buttons if configured even if below max visible pages', () => {
    pagination.config.showFirstLastButtons = true;
    pagination.config.pageRotationStep = 2;
    pagination.total = 26;
    expect(pagination.showFirstLastButtons()).to.be.true;
  });

  it('should go to specific page', () => {
    expect(pagination.page).to.equal(1);
    pagination.goToPage(2);
    expect(pagination.page).to.equal(2);
  });

  it('should go to the last page', () => {
    pagination.total = 26;
    pagination.page = 1;
    pagination.goToLastPage();
    expect(pagination.page).to.equal(2);
  });

  it('should trigger search on page change', () => {
    pagination.goToPage(5);
    var expected = {pageNumber: 5};
    expect(pagination.callback.callCount).to.equal(1);
    expect(pagination.callback.getCall(0).args[0]).to.deep.equal(expected);
  });

  it('should not trigger search if the page is the same', () => {
    pagination.page = 1;
    pagination.goToPage(1);
    expect(pagination.callback.callCount).to.equal(0);
  });

  it('should not trigger search if currently disabled', () => {
    pagination.config.disabled = true;
    pagination.goToPage(2);
    expect(pagination.callback.callCount).to.equal(0);
  });

  it('should not rotate pages at the beginning', () => {
    pagination.total = 375;
    var expected = [1, 2, 3, 4, 5];

    pagination.page = 1;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);

    pagination.page = 2;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);

    pagination.page = 3;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);
  });

  it('should rotate visible pages', () => {
    pagination.total = 375;
    var expected = [5, 6, 7, 8, 9];

    pagination.page = 7;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);
  });

  it('should not rotate pages at the end', () => {
    pagination.total = 375;
    var expected = [11, 12, 13, 14, 15];

    pagination.page = 13;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);

    pagination.page = 14;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);

    pagination.page = 15;
    expect(pagination.getPagesSlice()).to.deep.equal(expected);
  });

});

function mockScope() {
  return {
    $watch: () => {
    },
    $watchCollection: () => {
    }
  };
}