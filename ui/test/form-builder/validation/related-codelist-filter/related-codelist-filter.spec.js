import {RelatedCodelistFilter} from 'form-builder/validation/related-codelist-filter/related-codelist-filter';
import {InstanceModel} from 'models/instance-model';
import {PromiseStub} from 'test/promise-stub';

describe('Related Codelist Filter', ()=> {
  let codeListService = {};

  it('should validate two related codelists', ()=> {
    let relatedCodelistFilter = new RelatedCodelistFilter();
    let fieldName = 'department';
    let validatorDef = {inclusive: true, rerender: 'functional', filter_source: 'extra1'};
    let validatorModel =  new InstanceModel({department: {value: 'ENG'}, functional: {value: 'ALL'}});
    let flatModel = {department: {codelist: '504'}, functional: {codelist: '504'}};
    let formControl = {
      form: {
        functional: {
          fieldConfig: {
            dataLoader: () => {
              return PromiseStub.resolve('test');
            }
          }
        }
      },
      fieldViewModel: {identifier: 'department'}
    };

    let dataLoaderSpy = sinon.spy(formControl.form[validatorDef.rerender].fieldConfig, 'dataLoader');
    relatedCodelistFilter.validate(fieldName, validatorDef, validatorModel, flatModel, formControl);
    expect(dataLoaderSpy.calledOnce).to.be.true;
    expect(validatorModel[validatorDef.rerender].value).to.eql(null);
  });

  it('should not validate two unrelated codelists', ()=> {
    //when they codelists are unrelated, the rest service is not called.
    codeListService.getCodelist = sinon.spy();
    let relatedCodelistFilter = new RelatedCodelistFilter(codeListService);
    let fieldName = 'department';
    let validatorDef = {inclusive: true, rerender: 'functional', filterSource: 'extra1'};
    let validatorModel = new InstanceModel({department: {value: false}});
    let flatModel = {department: {codelist: '504'}, functional: {codelist: '504'}};
    let formControl = {fieldViewModel: {identifier: 'department'}, functional: {fieldConfig: {dataLoader: {}}}};
    relatedCodelistFilter.validate(fieldName, validatorDef, validatorModel, flatModel, formControl);
    expect(codeListService.getCodelist.called).to.be.false;
  });

  it('should load correct multivalued controls', ()=> {
    let relatedCodelistFilter = new RelatedCodelistFilter();
    let fieldName = 'department';
    let validatorDef = {inclusive: true, rerender: 'functional', filter_source: 'extra1'};
    let validatorModel =  new InstanceModel({department: {value: 'ENG'}, functional: {value: ['ALL']}});
    let flatModel = {department: {codelist: '504'}, functional: {codelist: '504'}};
    let formControl = {
      form: {
        functional: {
          fieldConfig: {
            dataLoader: () => {
              return PromiseStub.resolve({data: 'test'});
            }
          },
        }
      },
      fieldViewModel: {identifier: 'department'}
    };
    relatedCodelistFilter.validate(fieldName, validatorDef, validatorModel, flatModel, formControl);
    expect(validatorModel[validatorDef.rerender].value).to.eql([]);
  });
});