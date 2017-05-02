import {SearchResponse} from 'services/rest/response/search-response';

const LINK_NEXT = 'https://api.github.com/search?offset=5&limit=5';
const LINK_LAST = 'https://api.github.com/search?offset=10&limit=5';
const LINK_LAST_UNLIMITED = 'https://api.github.com/search?offset=10&limit=all';
const LINK_REL_NEXT = `<${LINK_NEXT}>; rel="next"`;
const LINK_REL_LAST = `<${LINK_LAST}>; rel="last"`;

function mockHeaders(map) {
  return function(header) {
    return map[header];
  };
}

describe('SearchResponse', function() {

  it('should parse link header and create mapping', function() {
    var map = {'Link': LINK_REL_NEXT + ', ' + LINK_REL_LAST};
    var links = new SearchResponse({headers: mockHeaders(map)}).links;

    expect(links.next).to.eq('https://api.github.com/search?offset=5&limit=5');
    expect(links.last).to.eq('https://api.github.com/search?offset=10&limit=5');
  });

  it('should not parse link header if already parsed', function() {
    var time = new Date().getTime();
    var res = new SearchResponse({});
    res._links = time;

    expect(res.links).to.eq(time);
  });

  it('should not parse offset param if already parsed', function() {
    var time = new Date().getTime();
    var res = new SearchResponse({});
    res._offset = time;

    expect(res.offset).to.eq(time);
  });

  it('should parse offset from offset param of last page link', function() {
    var res = new SearchResponse({});
    res._links = {last: LINK_LAST};

    expect(res.offset).to.eq(10);
  });

  it('should return 0 for offset if there is no last link', function() {
    var res = new SearchResponse({});
    res._links = {};

    expect(res.offset).to.eq(0);
  });

  it('should not parse limit param if already parsed', function() {
    var time = new Date().getTime();
    var res = new SearchResponse({});
    res._limit = time;

    expect(res.limit).to.eq(time);
  });

  it('should parse limit from limit param of last page link', function() {
    var res = new SearchResponse({});
    res._links = {last: LINK_LAST};

    expect(res.limit).to.eq(5);
  });

  it('should return 0 for limit if there is no last link', function() {
    var res = new SearchResponse({});
    res._links = {};

    expect(res.limit).to.eq(0);
  });

  it('should support unlimited requests', function() {
    var res = new SearchResponse({});
    res._links = {last: LINK_LAST_UNLIMITED};

    expect(res.limit).to.eq('all');
  });

  it('should not calculate last page number if calculated', function() {
    var time = new Date().getTime();
    var res = new SearchResponse({});
    res._lastPage = time;

    expect(res.lastPageNumber).to.eq(time);
  });

  it('should calculate last page', function() {
    var res = new SearchResponse({});
    res._offset = 15;
    res._limit = 5;

    expect(res.lastPageNumber).to.eq(3);
  });
});