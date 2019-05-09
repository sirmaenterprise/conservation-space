let Pagination = require('./pagination.js');
let SandboxPage = require('../../../page-object').SandboxPage;
let hasClass = require('../../../test-utils').hasClass;

const PAGINATION_URL = '/sandbox/search/components/common/pagination';

const NO_BUTTONS_PAGINATION = '#two-pages-no-buttons';
const TWO_PAGES_PAGINATION = '#two-pages';
const MULTIPLE_PAGES_PAGINATION = '#multiple-pages';

describe('Pagination', () => {

  let page = new SandboxPage();
  let pagination;

  beforeEach(() => {
    page.open(PAGINATION_URL);
  });

  function expectVisiblePages(pagination, pages) {
    pagination.getPages().then((items) => {
      expect(items.length).to.equal(pages.length);
    });
    pages.forEach((page) => {
      browser.wait(EC.visibilityOf(pagination.getPageButtonLink(page)), DEFAULT_TIMEOUT);
    });
  }

  describe('When configured to hide first and last buttons', () => {
    it('Then it should not show first and last buttons', () => {
      pagination = new Pagination($(NO_BUTTONS_PAGINATION));
      pagination.waitUntilOpened();
      expect(pagination.getFirstPageButton().isPresent()).to.eventually.be.false;
      expect(pagination.getLastPageButton().isPresent()).to.eventually.be.false;
    });
  });

  describe('When configured to show first and last buttons', () => {
    it('Then it should show first and last buttons if configured', () => {
      pagination = new Pagination($(TWO_PAGES_PAGINATION));
      pagination.waitUntilOpened();

      let firstPage = pagination.getFirstPageButton();
      expect(firstPage.isDisplayed()).to.eventually.be.true;

      let lastPage = pagination.getLastPageButton();
      expect(lastPage.isDisplayed()).to.eventually.be.true;
    });
  });

  describe('When there are available pages', () => {

    beforeEach(() => {
      pagination = new Pagination($(MULTIPLE_PAGES_PAGINATION));
      pagination.waitUntilVisible();
      pagination.waitForActiveButton();
    });

    it('Then it should disable first page button if it is the current one', () => {
      expect(pagination.isFirstPageButtonDisabled()).to.eventually.be.true;
      expect(pagination.istLastPageButtonDisabled()).to.eventually.be.false;
      pagination.goToPage(2);
      expect(pagination.isFirstPageButtonDisabled()).to.eventually.be.false;
      expect(pagination.istLastPageButtonDisabled()).to.eventually.be.false;
    });

    it('Then it should disable last page button if it is the current one', () => {
      expect(pagination.isFirstPageButtonDisabled()).to.eventually.be.true;
      expect(pagination.istLastPageButtonDisabled()).to.eventually.be.false;
      pagination.goToLastPage();
      expect(pagination.isFirstPageButtonDisabled()).to.eventually.be.false;
      expect(pagination.istLastPageButtonDisabled()).to.eventually.be.true;
    });

    it('Then it should show the active page button', () => {
      let activeButton = pagination.getPageButton(1);
      expect(hasClass(activeButton, 'active')).to.eventually.be.true;
    });

    it('Then it should display only the last 5 pages if the current page is at the end', () => {
      pagination.goToLastPage();
      expectVisiblePages(pagination, [4, 5, 6, 7, 8]);
    });

    it('Then it should correctly rotate pages', () => {
      expectVisiblePages(pagination, [1, 2, 3, 4, 5]);

      pagination.goToPage(2);
      expectVisiblePages(pagination, [1, 2, 3, 4, 5]);

      pagination.goToPage(3);
      expectVisiblePages(pagination, [1, 2, 3, 4, 5]);

      pagination.goToPage(4);
      expectVisiblePages(pagination, [2, 3, 4, 5, 6]);

      pagination.goToPage(5);
      expectVisiblePages(pagination, [3, 4, 5, 6, 7]);

      pagination.goToPage(6);
      expectVisiblePages(pagination, [4, 5, 6, 7, 8]);

      pagination.goToPage(7);
      expectVisiblePages(pagination, [4, 5, 6, 7, 8]);

      pagination.goToPage(8);
      expectVisiblePages(pagination, [4, 5, 6, 7, 8]);
    });
  });

});