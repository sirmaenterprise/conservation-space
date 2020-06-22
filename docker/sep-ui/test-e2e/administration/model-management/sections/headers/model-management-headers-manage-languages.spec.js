'use strict';

let ModelManagementSandbox = require('../../model-management.js').ModelManagementSandbox;
let LanguageSelector = require('./model-headers-section.js').LanguageSelector;
let INSTANCE_HEADERS = require('../../../../instance-header/instance-header.js').INSTANCE_HEADERS;

describe('Model management headers section - manage languages', () => {

  let headersSection;
  let modelData;
  let tree;
  let sandbox;

  function openPage(systemLang, userLang, modelId) {
    // Given The default system language is English
    // And I have opened entity definition model
    // When I open model headers section
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    tree = sandbox.getModelTree();
    modelData = sandbox.getModelData();
    headersSection = modelData.getHeadersSection();
  }

  it('should have language selector listing all available languages', () => {
    openPage('en', 'en', 'EO1001');
    // Then I expect to see a language selector
    let languageSelector = headersSection.getLanguageSelector();

    // And I expect default language to be selected and its title to be visible in the selector
    expect(languageSelector.getSelectedLanguage()).to.eventually.equal('English');

    // And I expect the selector to have selectable options for all available languages
    expect(languageSelector.getAvailableLanguages()).to.eventually.eql(['English', 'Bulgarian', 'German', 'Romanian', 'fi', 'ru']);
  });

  it('should allow language change', () => {
    openPage('en', 'en', 'EO1001');
    // Then I expect to see all headers in the default language
    let entityDefaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);
    expect(entityDefaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Default Header</span>)}');

    let entityCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    expect(entityCompactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Compact Header</span>)}');

    let entityBreadcrumbHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_BREADCRUMB);
    expect(entityBreadcrumbHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Breadcrumb Header</span>)}');

    let entityTooltipHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_TOOLTIP);
    expect(entityTooltipHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Tooltip Header</span>)}');

    // When I change the language to Bulgarian
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_BG);

    // Then I expect to see all headers in Bulgarian
    expect(entityDefaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Основно заглавие</span>)}');
    expect(entityCompactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Съкратено заглавие</span>)}');
    expect(entityBreadcrumbHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Кратко заглавие</span>)}');
    expect(entityTooltipHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Разширено заглавие</span>)}');

    // When I open Media type from the tree
    tree.search('Media');
    tree.getNode('Media').openObject();

    // Then I expect to see the Media headers in Bulgarian
    let mediaDefaultHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_DEFAULT);
    expect(mediaDefaultHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(MX1001) Основно заглавие</span>)}');

    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);
    expect(mediaCompactHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Съкратено заглавие</span>)}');

    let mediaBreadcrumbHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_BREADCRUMB);
    expect(mediaBreadcrumbHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Кратко заглавие</span>)}');

    let mediaTooltipHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_TOOLTIP);
    expect(mediaTooltipHeader.getHeaderValue()).to.eventually.equal('${eval(<span>(EO1001) Разширено заглавие</span>)}');
  });

  it('should forbid copy default value in default language', () => {
    // Given I have opened a Media definition - in the default system language (EN)
    // And I have selected headers tab
    openPage('en', 'en', 'MX1001');

    // When I open a media header
    let mediaCompactHeader = headersSection.getHeaderViewPanel(INSTANCE_HEADERS.HEADER_COMPACT);

    // Then I expect copy default value control to be hidden
    expect(mediaCompactHeader.canCopyDefaultValue(), 'Copy default value control should be hidden!').to.eventually.be.false;

    // When I change the language
    let languageSelector = headersSection.getLanguageSelector();
    languageSelector.selectLanguage(LanguageSelector.LANGUAGE_BG);

    // Then I expect copy default value control to be visible
    expect(mediaCompactHeader.canCopyDefaultValue(), 'Copy default value control should be visible!').to.eventually.be.true;
  });

});

