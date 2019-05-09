'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;

describe('Model management headers section', () => {

  let headersSection;
  let modelData;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    modelData = sandbox.getModelData();
    headersSection = modelData.getHeadersSection();
  }

  it('should see all header types listed in panels', () => {
    // entity definition model
    openPage('en', 'en', 'EO1001');
    let entityTestData = [
      {id: 'default_header', title: 'Default Header', value: '${eval(<span>(EO1001) Default Header</span>)}', inherited: false, canBeRestored: false},
      {id: 'compact_header', title: 'Compact Header', value: '${eval(<span>(EO1001) Compact Header</span>)}', inherited: false, canBeRestored: false},
      {id: 'breadcrumb_header', title: 'Breadcrumb Header', value: '${eval(<span>(EO1001) Breadcrumb Header</span>)}', inherited: false, canBeRestored: false},
      {id: 'tooltip_header', title: 'Tooltip Header', value: '${eval(<span>(EO1001) Tooltip Header</span>)}', inherited: false, canBeRestored: false}
    ];
    checkHeaders(entityTestData, headersSection);

    // media definition model
    openPage('en', 'en', 'MX1001');
    let mediaTestData = [
      // redefined EO1001 default_header
      {id: 'default_header', title: 'Default Header', value: '${eval(<span>(MX1001) Default Header</span>)}', inherited: false, canBeRestored: true},
      // inherited from EO1001
      {id: 'compact_header', title: 'Compact Header', value: '${eval(<span>(EO1001) Compact Header</span>)}', inherited: true, inheritedFrom: 'entity', canBeRestored: false},
      // inherited from EO1001
      {id: 'breadcrumb_header', title: 'Breadcrumb Header', value: '${eval(<span>(EO1001) Breadcrumb Header</span>)}', inherited: true, inheritedFrom: 'entity', canBeRestored: false},
      // inherited from EO1001
      {id: 'tooltip_header', title: 'Tooltip Header', value: '${eval(<span>(EO1001) Tooltip Header</span>)}', inherited: true, inheritedFrom: 'entity', canBeRestored: false}
    ];
    checkHeaders(mediaTestData, headersSection);
  });

});

function checkHeaders(data, headersSection) {
  data.forEach(expectedHeader => {
    let headerViewPanel = headersSection.getHeaderViewPanel(expectedHeader.id);
    headerViewPanel.hasTitle(expectedHeader.title);
    headerViewPanel.isHeaderInherited(expectedHeader.inherited);
    if (expectedHeader.inheritedFrom) {
      headerViewPanel.isHeaderInheritedFrom(expectedHeader.inheritedFrom);
    }
    // TODO: uncomment when functionality is ready
    // headerViewPanel.canBeRestored(expectedHeader.canBeRestored);
    expect(headerViewPanel.getHeaderValue(), `The '${expectedHeader.id}' header should have value!`).to.eventually.equal(expectedHeader.value);
  });
}