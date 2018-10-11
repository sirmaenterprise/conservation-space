import {Injectable} from 'app/app';

@Injectable()
export class ObjectSelectorHelper {
  groupSelectedObjects() {
    return {
      aggregated: {
        'emf:hasParent': {
          '1': 1,
          '2': 1,
          '3': 1,
          '4': 1,
          '5': 1,
          '6': 1
        },
        'emf:type': {
          'OT210027': 1
        },
        'emf:department': {
          'ENG': 1,
          'INF': 1
        },
        'emf:integrated': {
          'true': 1,
          'false': 1
        }
      }
    };
  }

  static getFilteringConfiguration(config) {
    return config;
  }
}
