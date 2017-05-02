'use strict';

var InstanceHeaderSandboxPage = require('./instance-header').InstanceHeaderSandboxPage;

describe('InstanceHeader', function () {

  var instanceHeaderPage = new InstanceHeaderSandboxPage();

  beforeEach(function () {
    instanceHeaderPage.open();
  });

  it('should show iDoc header when it is loaded', function () {
    instanceHeaderPage.loadHeader();
    var header = instanceHeaderPage.getIconHeader();
    expect(header.getField('.instance-link').getText()).to.eventually.equal('(Type) Title (Draft) Owner');
  });

  it('should change header when model is changed', function () {
    instanceHeaderPage.changeTitle('Stella');
    var header = instanceHeaderPage.getIconHeader();
    expect(header.getField('.instance-link').getText()).to.eventually.equal('(Type) Stella (Draft) Owner');
  });

  it('should not allow script injection', function () {
    instanceHeaderPage.changeTitle(`<script>javascript:document.getElementById('injectionTarget').innerHTML = 'Injected text';</script>`);
    var header = instanceHeaderPage.getIconHeader();
    expect(element(by.id('injectionTarget')).getText()).to.eventually.be.empty;
  });

  describe('Dates', ()=> {
    it('should format dates according to its data-format attribute', ()=> {
      instanceHeaderPage.loadHeader();
      // dueDate has its data-format property stored in an inner span
      var dueDate = instanceHeaderPage.getIconHeader().getField('[data-property="plannedEndDate"]');
      // createdOn has its data-format property stored in its element
      var createdOn = instanceHeaderPage.getIconHeader().getField('[data-property="createdOn"]');

      expect(dueDate.getAttribute('data-format')).to.eventually.equal('MMM/DD/YYYY');
      expect(dueDate.getText()).to.eventually.equal("Dec/22/2015");
      expect(createdOn.getAttribute('data-format')).to.eventually.equal('MM.DD.YYYY');
      expect(createdOn.getText()).to.eventually.equal("12.22.2015");
    });
  });

});
