'use strict';

var StaticInstanceHeaderSandboxPage = require('./static-instance-header').StaticInstanceHeaderSandboxPage;
var StaticInstanceHeader = require('./static-instance-header').StaticInstanceHeader;

describe('StaticInstanceHeader', function () {

  var page = new StaticInstanceHeaderSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should render the passed header', function () {
    var staticInstaceHeader = new StaticInstanceHeader($('#icon_header .instance-header'));
    expect(staticInstaceHeader.getHeaderAsText()).to.eventually.equal('Title');
  });

  it('disabled header link should not be clickable', function () {
    var staticInstaceHeader = new StaticInstanceHeader($('#disabled_header .instance-header'));
    expect(staticInstaceHeader.isClickable()).to.eventually.be.false;
  });
});
