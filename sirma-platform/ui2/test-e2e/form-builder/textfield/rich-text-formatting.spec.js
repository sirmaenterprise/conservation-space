'use strict';

let IdocPage = require('../../idoc/idoc-page').IdocPage;
let ObjectSelector = require('../../idoc/widget/object-selector/object-selector.js').ObjectSelector;

describe.skip('RichText control - text formatting', () => {

  let idocPage = new IdocPage();

  beforeEach(() => {
    idocPage.open(true);
  });

  afterEach(() => {
    browser.executeScript('$(".seip-modal").remove();');
  });

  it('should allow bold text', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.bold().then(() => {
        mandatoryDescription.type('bold text').then(() => {
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>​​​​​​​bold text</strong><br></p>');

          idocPage.getActionsToolbar().saveIdoc();
          idocPage.waitForPreviewMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>bold text</strong></p>');

          idocPage.getActionsToolbar().getActionsMenu().editIdoc();
          idocPage.waitForEditMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><strong>bold text</strong></p>');
        });
      });
    });
  });

  it('should allow italic text', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toobar) => {
      toobar.italic().then(() => {
        mandatoryDescription.type('italic text').then(() => {
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>​​​​​​​italic text</em><br></p>');

          idocPage.getActionsToolbar().saveIdoc();
          idocPage.waitForPreviewMode();

          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>italic text</em></p>');

          idocPage.getActionsToolbar().getActionsMenu().editIdoc();
          idocPage.waitForEditMode();

          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><em>italic text</em></p>');
        });
      });
    });
  });

  it('should allow ordered lists', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.orderedList().then(() => {
        mandatoryDescription.type('item 1').then(() => {
          mandatoryDescription.newLine().then(() => {
            mandatoryDescription.type('item 2').then(() => {
              mandatoryDescription.newLine().then(() => {
                mandatoryDescription.tab().then(() => {
                  mandatoryDescription.type('item 2.1').then(() => {
                    mandatoryDescription.newLine().then(() => {
                      mandatoryDescription.type('item 2.2').then(() => {
                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');

                        idocPage.getActionsToolbar().saveIdoc();
                        idocPage.waitForPreviewMode();

                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');

                        idocPage.getActionsToolbar().getActionsMenu().editIdoc();
                        idocPage.waitForEditMode();

                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ol><li>item 1</li><li>item 2<ol><li>item 2.1</li><li>item 2.2</li></ol></li></ol>');
                      });
                    });
                  });
                });
              });
            });
          });
        });
      });
    });
  });

  it('should allow unordered lists', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.unorderedList().then(() => {
        mandatoryDescription.type('item 1').then(() => {
          mandatoryDescription.newLine().then(() => {
            mandatoryDescription.type('item 2').then(() => {
              mandatoryDescription.newLine().then(() => {
                mandatoryDescription.tab().then(() => {
                  mandatoryDescription.type('item 2.1').then(() => {
                    mandatoryDescription.newLine().then(() => {
                      mandatoryDescription.type('item 2.2').then(() => {
                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');

                        idocPage.getActionsToolbar().saveIdoc();
                        idocPage.waitForPreviewMode();

                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');

                        idocPage.getActionsToolbar().getActionsMenu().editIdoc();
                        idocPage.waitForEditMode();

                        expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li>item 1</li><li>item 2<ul><li>item 2.1</li><li>item 2.2</li></ul></li></ul>');
                      });
                    });
                  });
                });
              });
            });
          });
        });
      });
    });
  });

  it('should keep unordered lists style', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.unorderedList().then(() => {
        mandatoryDescription.getEditorToolbar().then((toolbar) => {
          toolbar.fontColor('Bright Blue').then(() => {
            mandatoryDescription.type('colored item 1').then(() => {
              mandatoryDescription.newLine().then(() => {
                expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">colored item 1</span><br></li><li style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\"><span style=\"-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);\">​​​​​​​</span></li></ul>');

                idocPage.getActionsToolbar().saveIdoc();
                idocPage.waitForPreviewMode();

                expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);"><span style="-webkit-text-fill-color:#3498db;color:#3498db;">colored item 1</span></li><li style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);"><span style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);">‌</span></li></ul>');

                idocPage.getActionsToolbar().getActionsMenu().editIdoc();
                idocPage.waitForEditMode();

                expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<ul><li style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);"><span style="-webkit-text-fill-color:#3498db;color:#3498db;">colored item 1</span></li><li style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);"><span style="-webkit-text-fill-color: rgb(52, 152, 219); color: rgb(52, 152, 219);">‌</span></li></ul>');
              });
            });
          });
        });
      });
    });
  });

  it('should allow font size', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.fontSize(8).then(() => {
        mandatoryDescription.type('fontsize 8').then(() => {
          mandatoryDescription.newLine().then(() => {
            mandatoryDescription.getEditorToolbar().then((toolbar) => {
              toolbar.fontSize(72).then(() => {
                mandatoryDescription.type('fontsize 72').then(() => {
                  mandatoryDescription.newLine().then(() => {
                    expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"font-size:8px;\">fontsize 8</span><br></p><p><span style=\"font-size:72px;\">fontsize 72</span><br></p><p><br></p>');

                    idocPage.getActionsToolbar().saveIdoc();
                    idocPage.waitForPreviewMode();

                    expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="font-size:8px;">fontsize 8</span></p><p><span style="font-size:72px;">fontsize 72</span></p><p><br></p>');

                    idocPage.getActionsToolbar().getActionsMenu().editIdoc();
                    idocPage.waitForEditMode();

                    expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="font-size:8px;">fontsize 8</span></p><p><span style="font-size:72px;">fontsize 72</span></p><p><br></p>');
                  });
                });
              });
            });
          });
        });
      });
    });
  });

  it('should allow text background', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.backgroundColor('Bright Blue').then(() => {
        mandatoryDescription.type('colored text').then(() => {
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"background-color:#3498db !important;\">​​​​​​​colored text</span><br></p>');

          idocPage.getActionsToolbar().saveIdoc();
          idocPage.waitForPreviewMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="background-color:#3498db !important;">colored text</span></p>');

          idocPage.getActionsToolbar().getActionsMenu().editIdoc();
          idocPage.waitForEditMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="background-color:#3498db !important;">colored text</span></p>');
        });
      });
    });
  });

  it('should allow text color', () => {
    let widget = idocPage.insertODWWithFields(['mandatoryDescription'], {type: ObjectSelector.MANUALLY, item: 9});
    let mandatoryDescription = widget.getForm().getRichTextField('mandatoryDescription');

    mandatoryDescription.getEditorToolbar().then((toolbar) => {
      toolbar.fontColor('Bright Blue').then(() => {
        mandatoryDescription.type('colored text').then(() => {
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style=\"-webkit-text-fill-color:#3498db;color:#3498db;\">​​​​​​​colored text</span><br></p>');

          idocPage.getActionsToolbar().saveIdoc();
          idocPage.waitForPreviewMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="-webkit-text-fill-color:#3498db;color:#3498db;">colored text</span></p>');

          idocPage.getActionsToolbar().getActionsMenu().editIdoc();
          idocPage.waitForEditMode();
          expect(mandatoryDescription.getAsHtml()).to.eventually.equal('<p><span style="-webkit-text-fill-color:#3498db;color:#3498db;">colored text</span></p>');
        });
      });
    });
  });
});
