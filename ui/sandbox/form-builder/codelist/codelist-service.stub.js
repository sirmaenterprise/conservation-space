import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class CodelistRestService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getCodelist(opts) {
    return this.promiseAdapter.promise((resolve) => {

      if(opts.codelistNumber === 503) {
        resolve({
          data: [{
            'ln': 'bg',
            'codelist': 503,
            'label': 'Engeneering department',
            'value': 'ENG',
            'descriptions': {
              'bg': 'Engeneering department',
              'en': 'Engeneering department'
            }
          },
            {
              'ln': 'bg',
              'codelist': 503,
              'label': 'Infrastructure department',
              'value': 'INF',
              'descriptions': {
                'bg': 'Infrastructure department',
                'en': 'Infrastructure department'
              }
            },
            {
              'ln': 'bg',
              'codelist': 503,
              'label': 'Test department',
              'value': 'TSD',
              'descriptions': {
                'bg': 'Test department',
                'en': 'Test department'
              }
            }]
        });
      }

      if(opts.codelistNumber === 504) {
        if (opts.filterBy === "ENG") {
          resolve({
            data: [{
              'ln': 'bg',
              'codelist': 504,
              'label': 'Mechanical Design Group',
              'value': 'MDG',
              'descriptions': {
                'bg': 'Mechanical Design Group',
                'en': 'Mechanical Design Group',
                'extra1': 'ENG'
              }
            }]
          });
        }

        if (opts.filterBy === "INF") {
          resolve({
            data: [{
              'ln': 'bg',
              'codelist': 504,
              'label': 'Electrical Design Group',
              'value': 'EDG',
              'descriptions': {
                'bg': 'Electrical Design Group',
                'en': 'Electrical Design Group',
                'extra1': 'INF'
              }
            }]
          });
        }

        resolve({
          data: [{
            'ln': 'bg',
            'codelist': 504,
            'label': 'Mechanical Design Group',
            'value': 'MDG',
            'descriptions': {
              'bg': 'Mechanical Design Group',
              'en': 'Mechanical Design Group',
              'extra1': 'ENG'
            }
          },{
            'ln': 'bg',
            'codelist': 504,
            'label': 'Electrical Design Group',
            'value': 'EDG',
            'descriptions': {
              'bg': 'Electrical Design Group',
              'en': 'Electrical Design Group',
              'extra1': 'INF'
            }
          }]
        });
      }

      resolve({
        data: [{
          'ln': 'bg',
          'codelist': 210,
          'label': 'Обикновен документ',
          'value': 'OT210027',
          'descriptions': {
            'bg': 'Обикновен документ',
            'en': 'Common document'
          }
        },
          {
            'ln': 'bg',
            'codelist': 210,
            'label': 'Препоръки за внедряване',
            'value': 'CH210001',
            'descriptions': {
              'bg': 'Препоръки за внедряване',
              'en': 'Препоръки за внедряване',
              'extra1': 'DT217002'
            }
          },
          {
            'ln': 'bg',
            'codelist': 210,
            'label': 'Other',
            'value': 'DT210099',
            'descriptions': {
              'bg': 'Other',
              'en': 'Other'
            }
          }]
      });
    });
  }
}
