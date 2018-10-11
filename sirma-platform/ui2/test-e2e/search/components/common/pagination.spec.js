var Pagination = require('./pagination.js');
var SandboxPage = require('../../../page-object').SandboxPage;

const PAGINATION_URL = '/sandbox/search/components/common/pagination';

const NO_BUTTONS_PAGINATION = '#two-pages-no-buttons';
const TWO_PAGES_PAGINATION = '#two-pages';
const MULTIPLE_PAGES_PAGINATION = '#multiple-pages';

describe('Pagination', () => {

  var page = new SandboxPage();
  var pagination;

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

      element.all(by.css(NO_BUTTONS_PAGINATION + pagination.getFirstPageButtonSelector())).then((items) => {
        expect(items.length).to.equal(0);
      });
      element.all(by.css(NO_BUTTONS_PAGINATION + pagination.getLastPageButtonSelector())).then((items) => {
        expect(items.length).to.equal(0);
      });
    });
  });

  describe('When configured to show first and last buttons', () => {
    it('Then it should show first and last buttons if configured', () => {
      pagination = new Pagination($(TWO_PAGES_PAGINATION));
      pagination.waitUntilOpened();

      var firstPage = pagination.getFirstPageButton();
      expect(firstPage.isDisplayed()).to.eventually.be.true;

      var lastPage = pagination.getLastPageButton();
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
      var firstPage = pagination.getFirstPageButton();
      return firstPage.getAttribute('class').then((classes) => {
        expect(classes).to.contains(pagination.getDisabledClass());
        return pagination.goToPage(2);
      }).then(() => {
        pagination.waitForActiveButton();
        return firstPage.getAttribute('class');
      }).then((classes) => {
        expect(classes).to.not.contains(pagination.getDisabledClass());
      });
    });

    it('Then it should disable last page button if it is the current one', () => {
      var lastPage = pagination.getLastPageButton();
      expect(lastPage.getAttribute('class')).to.eventually.not.have.string(pagination.getDisabledClass());

      pagination.goToLastPage();
      expect(lastPage.getAttribute('class')).to.eventually.have.string(pagination.getDisabledClass());
    });

    it('Then it should show the active page button', () => {
      var activeButton = pagination.getPageButton(1);
      expect(activeButton.getAttribute('class')).to.eventually.have.string('active');
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