/**
 * Sample data for testing.
 */
export function getCodeLists() {
  let codeLists = [
    {
      id: '1',
      descriptions: {
        EN: {name: 'Name1', comment: 'Comment1', language: 'EN'},
        BG: {name: 'Име1', comment: 'Коментар1', language: 'BG'}
      },
      extras: {'1': 'extra11', '2': 'extra12', '3': 'extra13'},
      values: [{
        id: 'val11',
        descriptions: {
          EN: {name: 'Name V11', comment: 'Comment V11', language: 'EN'},
          BG: {name: 'Име V11', comment: 'Коментар V11', language: 'BG'}
        }
      }, {
        id: 'val12',
        descriptions: {
          EN: {name: 'Name V12', comment: 'Comment V12', language: 'EN'},
          BG: {name: 'Име V12', comment: 'Коментар V12', language: 'BG'}
        }
      }]
    }, {
      id: '2',
      descriptions: {
        EN: {name: 'Name2', comment: 'Comment2', language: 'EN'},
        BG: {name: 'Име2', comment: 'Коментар2', language: 'BG'}
      },
      extras: {'1': 'extra21', '2': 'extra22', '3': 'extra23'},
      values: [{
        id: 'val21',
        descriptions: {
          EN: {name: 'Name V21', comment: 'Comment V21', language: 'EN'},
          BG: {name: 'Име V21', comment: 'Коментар V21', language: 'BG'}
        }
      }, {
        id: 'val22',
        descriptions: {
          EN: {name: 'Name V22', comment: 'Comment V22', language: 'EN'},
          BG: {name: 'Име V22', comment: 'Коментар V22', language: 'BG'}
        }
      }]
    }
  ];

  // Link description
  codeLists[0].description = codeLists[0].descriptions['EN'];
  codeLists[0].values[0].description = codeLists[0].values[0].descriptions['EN'];
  codeLists[0].values[1].description = codeLists[0].values[1].descriptions['EN'];

  codeLists[1].description = codeLists[1].descriptions['EN'];
  codeLists[1].values[0].description = codeLists[1].values[0].descriptions['EN'];
  codeLists[1].values[1].description = codeLists[1].values[1].descriptions['EN'];

  return codeLists;
}