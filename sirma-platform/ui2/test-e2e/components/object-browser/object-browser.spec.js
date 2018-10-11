'use strict';

let ObjectBrowserSandboxPage = require('./object-browser').ObjectBrowserSandboxPage;

describe('Object Browser', function () {

  let page = new ObjectBrowserSandboxPage();

  it('should display the root node', function () {
    // Given I have opened the tree browser page
    page.open('emf:99fd21fc-678e-405a-84f0-eb83d69aa415');
    let objectBrowser = page.getObjectBrowser();

    // Then I should see root node
    objectBrowser.getNode('Root node');
  });

  it('should allow lazy loading child nodes', function () {
    // Given I have opened the tree browser page
    page.open('emf:99fd21fc-678e-405a-84f0-eb83d69aa415');
    let objectBrowser = page.getObjectBrowser();
    objectBrowser.getNode('Workflows').expand();

    //When I expand a node that should have children
    objectBrowser.getNode('7521-8').expand();

    // I should see the loaded children
    objectBrowser.getNode('7521-9');
  });

  it('should highlight leaf nodes', function () {
    // Given I have opened the tree browser page for the expected leaf entity
    page.open('emf:8fe55bc7-f205-4a56-8d26-af5bbabbb022');
    let objectBrowser = page.getObjectBrowser();

    // And I should see the current node highlighted
    expect(objectBrowser.getNode('Highlight testing node').isHighlighted()).to.eventually.be.true;
  });

  it('should allow node selection', function () {
    // Given I have opened the tree browser page for the expected child entity
    page.open('emf:695e442d-b96e-47aa-815c-4e7694a69a7d');
    let objectBrowser = page.getObjectBrowser();

    //When I check a node
    let firstNode = objectBrowser.getNode('Display the data');
    firstNode.check();

    // And then check another node
    let secondNode = objectBrowser.getNode('Aston');
    secondNode.check();

    // Then I should see fist node unchecked and the second node checked
    return Promise.all([firstNode.isChecked(), secondNode.isChecked(), page.getSelectedNode()]).then(function (values) {
      expect(values).to.eql([false, true, 'emf:dd8e9dbd-2400-4f63-f953-b6fe60df200d']);
    });
  });

  it('should autoexpand and scroll to the current entity if it isn\'t the root', function () {
    // resize the browser in order to check if the element is properly visible in the view port
    browser.driver.manage().window().setSize(1000, 100);

    // Given I have opened the tree browser page for the expected child entity
    page.open('emf:695e442d-b96e-47aa-815c-4e7694a69a7d');
    let objectBrowser = page.getObjectBrowser();

    // Then I should see the child entity opened
    objectBrowser.getNode('Aston');

    let node = objectBrowser.getNode('First 1.1');

    // And I should  be able to see the current node in the view port
    expect(node.isDisplayed()).to.eventually.be.true;

    // And I should see the current node highlighted
    expect(node.isHighlighted()).to.eventually.be.true;
  });

});
