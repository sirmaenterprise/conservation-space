import {PasteFromWordProcessor} from 'idoc/editor/plugins/paste-from-word-processor/paste-from-word-processor';

describe('PasteFromWordProcessor', () => {
  let pasteFromWordProcessor;
  before(() => {
    pasteFromWordProcessor = new PasteFromWordProcessor();
  });

  it('should process pasted content according to defined filters', () => {
    let filters = {
      '*': {
        'color': 'red',
        'background-color': null
      },
      'p': null,
      'table': {
        'width': (oldValue) => {
          if (!oldValue) {
            return '100%';
          }
          return oldValue;
        }
      }
    };

    let content = '<p style="font-size:12px">Remove all styles</p><span style="color:green;background-color:blue">Remove background-color and set color to red</span><table width="50px" style="width:50px"><tr><td>Keep width the same and add color red</td></tr></table><table style=""><tr><td>Set width to 100% and add color red</td></tr></table>';
    let event = {
      data: {
        dataValue: content
      },
      editor: {
        config: {
          pasteFromWordFilters: filters
        }
      }
    };

    pasteFromWordProcessor.processAfterPasteFromWordEvent(event);

    expect(event.data.dataValue).to.equals('<p style="">Remove all styles</p><span style="color:red">Remove background-color and set color to red</span><table style="width:50px; color:red"><tr><td>Keep width the same and add color red</td></tr></table><table style="color:red; width:100%"><tr><td>Set width to 100% and add color red</td></tr></table>');
  });
});
