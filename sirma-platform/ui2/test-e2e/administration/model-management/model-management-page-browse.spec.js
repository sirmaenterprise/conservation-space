'use strict';

let ModelManagementSandbox = require('./model-management.js').ModelManagementSandbox;

describe('Models management page - browsing', () => {

  let section;

  it('should show a message prompting to select a model when a model is not selected', () => {
    section = new ModelManagementSandbox().open().getModelSection();
    expect(section.isModelSelectMessageDisplayed()).to.be.true;
  });

  it('should show a brief loading message when the model is in the process of loading', () => {
    section = new ModelManagementSandbox().open('en', 'bg', 'EO1001').getModelSection();
    expect(section.isModelLoadingMessageDisplayed()).to.be.true;
  });
});