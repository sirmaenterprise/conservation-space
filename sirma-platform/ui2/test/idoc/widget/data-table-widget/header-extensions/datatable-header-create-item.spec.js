import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {stub} from 'test/test-utils';
import {WidgetControl} from 'idoc/widget/widget';
import {DatatableHeaderCreateItem} from 'idoc/widget/datatable-widget/header-extensions/datatable-header-create-item';
import {CreatePanelService, INSTANCE_CREATE_PANEL, FILE_UPLOAD_PANEL} from 'services/create/create-panel-service';
import {ModelsService} from 'services/rest/models-service';
import {NamespaceService} from 'services/rest/namespace-service';
import {MockEventbus} from 'test/test-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('DatatableHeaderCreateItem', () => {

  let datatableHeaderCreateItem;
  let scopeMock;
  let namespaceService;

  before(() => {
    scopeMock = mock$scope();
    namespaceService = stubNamespaceService();

    datatableHeaderCreateItem = new DatatableHeaderCreateItem(stub(CreatePanelService), stub(ModelsService), namespaceService, scopeMock, MockEventbus);
    datatableHeaderCreateItem.config = {
      expanded: true,
      selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
      displayCreateAction: true
    };
    datatableHeaderCreateItem.control = stub(WidgetControl);
    datatableHeaderCreateItem.ngOnInit();
  });

  it('should extract class and definition filters from criteria type', () => {
    let criteriaTypes = ['http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject', 'DT0007'];
    let result = datatableHeaderCreateItem.extractClassAndDefinitionFilters(criteriaTypes);

    expect(result.classFilter).to.deep.equal(['http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject']);
    expect(result.definitionFilter).to.deep.equal(['DT0007']);
  });

  describe('should determine exclusions with given models', () => {
    it('and exclude upload panel if only creatable models are given', () => {
      let models = [
        {
          creatable: true,
          id: 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book',
          label: 'Book',
          parent: 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject',
          type: 'class',
          uploadable: false
        },
        {
          id: 'CO1004',
          label: 'Book',
          parent: 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book',
          type: 'definition'
        }
      ];
      datatableHeaderCreateItem.determineExclusions(models);

      expect(datatableHeaderCreateItem.exclusions).to.deep.equal([FILE_UPLOAD_PANEL]);
    });

    it('and exclude create panel if only uploadable models are given', () => {
      let models = [
        {
          creatable: false,
          id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Audio',
          label: 'Audio',
          parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Media',
          type: 'class',
          uploadable: true
        },
        {
          id: 'audio',
          label: 'Audio',
          parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Audio',
          type: 'definition'
        }
      ];
      datatableHeaderCreateItem.determineExclusions(models);

      expect(datatableHeaderCreateItem.exclusions).to.deep.equal([INSTANCE_CREATE_PANEL]);
    });

    it('and have no exclusions if both types of models are given', () => {
      let models = [
        {
          creatable: false,
          id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Audio',
          label: 'Audio',
          parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Media',
          type: 'class',
          uploadable: true
        },
        {
          creatable: true,
          id: 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book',
          label: 'Book',
          parent: 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject',
          type: 'class',
          uploadable: false
        }
      ];
      datatableHeaderCreateItem.determineExclusions(models);

      expect(datatableHeaderCreateItem.exclusions).to.be.empty;
    });
  });

  function stubNamespaceService() {
    let namespaceService = stub(NamespaceService);
    namespaceService.isUri = sinon.spy((uri) => {
      return uri.indexOf(':') > 0;
    });
    return namespaceService;
  }
});
