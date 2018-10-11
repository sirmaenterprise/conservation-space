'use strict';

let StaticInstanceHeaderSandboxPage = require('./static-instance-header').StaticInstanceHeaderSandboxPage;
let StaticInstanceHeader = require('./static-instance-header').StaticInstanceHeader;

describe('StaticInstanceHeader', function () {

  let page = new StaticInstanceHeaderSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should render the passed header', function () {
    let staticInstaceHeader = new StaticInstanceHeader($('#icon_header .instance-header'));
    expect(staticInstaceHeader.getHeaderAsText()).to.eventually.equal('Title');
  });

  it('disabled header link should not be clickable', function () {
    let staticInstaceHeader = new StaticInstanceHeader($('#disabled_header .instance-header'));
    expect(staticInstaceHeader.isClickable()).to.eventually.be.false;
  });
});
