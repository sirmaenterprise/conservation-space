var IdocPage = require('../idoc/idoc-page').IdocPage;
var HelloWidget = require('../idoc/widget/hello-widget').HelloWidget;
var HelloWidgetConfigDialog = require('../idoc/widget/hello-widget').HelloWidgetConfigDialog;

describe('Component', function () {

  it('should support lifecycle methods', function () {
    //Given I have opened the idoc page for edit
    var idocPage = new IdocPage();
    idocPage.open(true);

    // When I insert a hello widget
    widgetElement = idocPage.getTabEditor(1).insertWidget(HelloWidget.WIDGET_NAME);
    new HelloWidgetConfigDialog().setName('world!').save();
    var helloWidget = new HelloWidget(widgetElement);

    // Then the on init callback should get called
    expect($('#onInit').isPresent()).to.eventually.be.true;

    // And the after view callback should get called
    expect($('#ngAfterViewInit').getText()).to.eventually.equal('Hello world!');

    //When I remove the widget
    var helloWidget = new HelloWidget(widgetElement);
    // scrolling is required because the editor gets focused on load and sometimes overlays the widget buttons
    idocPage.scrollToTop();
    helloWidget.getHeader().remove();

    //Then I should see the input name
    expect($('#onDestroy').isPresent()).to.eventually.be.true;
  });

  it('should support component events', function () {
    browser.get('/sandbox/app');

    browser.wait(EC.textToBePresentInElement($('.message'), 'echo'), DEFAULT_TIMEOUT);
  });
});