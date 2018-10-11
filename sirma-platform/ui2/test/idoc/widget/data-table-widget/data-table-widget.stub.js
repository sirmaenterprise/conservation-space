
export function getObjectModels() {

  return {
    'id:1': {
      'models': {
        'definitionId': 'GEP111111',
        'viewModel': {
          'fields': [
            {
              'identifier': 'title'
            },
            {
              'identifier': 'emf:createdBy'
            }
          ]
        },
        'validationModel': {
          'title': {
            'value': 'Project 1'
          },
          'emf:createdBy': {
            'value': {
              'results': ['id:1_1'],
              'total': 1
            }
          }
        }
      }
    },
    'id:2': {
      'models': {
        'definitionId': 'GEP100002',
        'viewModel': {
          'fields': [
            {
              'identifier': 'title'
            },
            {
              'identifier': 'emf:createdBy'
            }
          ]
        },
        'validationModel': {
          'title': {
            'value': 'Project 1'
          },
          'emf:createdBy': {
            'value': {
              'results': ['id:2_1'],
              'total': 1
            }
          }
        }
      }
    }
  };

}