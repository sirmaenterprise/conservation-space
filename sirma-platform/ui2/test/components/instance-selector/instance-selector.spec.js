import {InstanceSelector, INSTANCE_SELECTOR_PROPERTIES} from 'components/instance-selector/instance-selector';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {PickerRestrictionsService} from 'services/picker/picker-restrictions-service';
import {InstanceObject} from 'models/instance-object';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {HEADER_DEFAULT, HEADER_COMPACT, HEADER_BREADCRUMB, NO_HEADER} from 'instance-header/header-constants';
import {InstanceModelProperty} from 'models/instance-model';
import {Logger} from 'services/logging/logger';
import {Configuration} from 'common/application-config';
import {EventEmitter} from 'common/event-emitter';
import {HeadersService} from 'instance-header/headers-service';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {InstanceRestService} from 'services/rest/instance-service';
import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {RelationshipsService} from 'services/rest/relationships-service';

describe('InstanceSelector', () => {
  let instanceSelector;

  beforeEach(() => {
    InstanceSelector.prototype.config = {eventEmitter : new EventEmitter()};
    instanceSelector = getComponentInstance();
    instanceSelector.instanceModelProperty = new InstanceModelProperty({
      value: {results: ['id:1']}
    });
    instanceSelector.ngOnInit();
  });

  describe('on init', () => {
    it('should resolve headerType', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.config.instanceHeaderType = HEADER_DEFAULT;
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: []}
      });
      instanceSelector.ngOnInit();
      expect(instanceSelector.headerType).to.equal(HEADER_DEFAULT);
    });

    it('should initialize field with default value if its undefined', () => {
      instanceSelector = getComponentInstance();

      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: undefined
      });

      instanceSelector.ngOnInit();

      expect(instanceSelector.instanceModelProperty.serialize()).to.eql({
        value: {
          results: [],
          total: 0,
          add: [],
          remove: [],
          headers: {}
        }
      });
    });
  });

  describe('showMore', () => {
    it('should load and show all selected objects', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: [], total: 3}
      });
      instanceSelector.ngOnInit();
      instanceSelector.instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({
        data: ['id:0', 'id:1', 'id:3']
      }));

      instanceSelector.showMore();

      expect(instanceSelector.displayObjectsCount).to.equal(3);
      expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1', 'id:3']);
    });

    it('should not trigger loading when there is nothing more to load', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: ['id:1', 'id:2', 'id:3'], total: 3}
      });
      instanceSelector.ngOnInit();

      instanceSelector.showMore();

      expect(instanceSelector.instanceRestService.getInstanceProperty.notCalled);
      expect(instanceSelector.displayObjectsCount).to.equal(3);
      expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:1', 'id:2', 'id:3']);
    });
  });

  describe('showLess', () => {
    it('should show only configured number of objects or less', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: []}
      });
      instanceSelector.config.visibleItemsCount = 5;
      instanceSelector.ngOnInit();
      instanceSelector.showLess();
      expect(instanceSelector.displayObjectsCount).to.equal(5);
    });
  });

  describe('isShowMoreButtonVisible', () => {
    let data = [
      {config: 3, total: 5, results: ['1', '2', '3'], expectedVisibility: true},
      {config: 3, total: 0, results: [], expectedVisibility: false},
      {config: 3, total: 1, results: ['1'], expectedVisibility: false},
      {config: 3, total: 2, results: ['1', '2'], expectedVisibility: false},
      {config: 3, total: 3, results: ['1', '2', '3'], expectedVisibility: false},
      {config: 3, total: 4, results: ['1', '2', '3', '4'], expectedVisibility: true}
    ];

    it('should be visible when total selected objects count is greater than calculated visible objects count', () => {
      data.forEach((set) => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {
            results: set.results,
            total: set.total
          }
        });
        instanceSelector.instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({
          data: ['1', '2', '3', '4', '5']
        }));
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.config.visibleItemsCount = set.config;
        instanceSelector.ngOnInit();

        let isVisible = instanceSelector.isShowMoreButtonVisible();
        expect(isVisible, `Show more button should be [${set.expectedVisibility ? 'visible' : 'hidden'}] with data: 
        ${JSON.stringify(set)}, calculated visible objects count [${instanceSelector.displayObjectsCount}]`).to.equal(set.expectedVisibility);
      });
    });

    it('should be hidden if selection type is single', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.displayObjectsCount = 1;
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['1'],
          total: 2
        }
      });
      instanceSelector.config.selection = SINGLE_SELECTION;
      instanceSelector.ngOnInit();
      expect(instanceSelector.isShowMoreButtonVisible()).to.be.false;
    });
  });

  describe('isShowLessButtonVisible', () => {
    let data = [
      {config: 3, total: 5, results: [], expectedVisibility: false},
      {config: 3, total: 5, results: ['1'], expectedVisibility: false},
      {config: 3, total: 5, results: ['1', '2'], expectedVisibility: false},
      {config: 3, total: 5, results: ['1', '2', '3'], expectedVisibility: false},
      {config: 3, total: 5, results: ['1', '2', '3', '4'], expectedVisibility: false},
      {config: 3, total: 5, results: ['1', '2', '3', '4', '5'], expectedVisibility: false},
      {config: 3, total: 2, results: [], expectedVisibility: false},
      {config: 3, total: 2, results: ['1'], expectedVisibility: false},
      {config: 3, total: 2, results: ['1', '2'], expectedVisibility: false}
    ];

    it('should be visible when calculated visible objects count is greater than configuration', () => {
      data.forEach((set) => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {
            results: set.results,
            total: set.total
          }
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.config.visibleItemsCount = set.config;
        instanceSelector.ngOnInit();

        let isVisible = instanceSelector.isShowLessButtonVisible();
        expect(isVisible, `Show less button should be [${set.expectedVisibility ? 'visible' : 'hidden'}] with data: 
          ${JSON.stringify(set)}, calculated visible objects count [${instanceSelector.displayObjectsCount}]`).to.equal(set.expectedVisibility);
      });
    });

    it('should be hidden when selection type is single', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.displayObjectsCount = 2;
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['1'],
          total: 1
        }
      });
      instanceSelector.config.selection = SINGLE_SELECTION;
      instanceSelector.config.visibleItemsCount = 1;
      expect(instanceSelector.isShowLessButtonVisible()).to.be.false;
    });
  });

  describe('resolveHeaderType', () => {
    it('should resolve with breadcrumb header by default if headerType is not provided', () => {
      instanceSelector.resolveHeaderType();
      expect(instanceSelector.headerType).to.equal(HEADER_BREADCRUMB);
    });

    it('should resolve with breadcrumb header by default if headerType is `none`', () => {
      instanceSelector.config.instanceHeaderType = NO_HEADER;
      instanceSelector.resolveHeaderType();
      expect(instanceSelector.headerType).to.equal(HEADER_BREADCRUMB);
    });

    it('should resolve with provided headerType', () => {
      instanceSelector.config.instanceHeaderType = HEADER_DEFAULT;
      instanceSelector.resolveHeaderType();
      expect(instanceSelector.headerType).to.equal(HEADER_DEFAULT);
    });
  });

  describe('on select', () => {
    it('should configure and open picker dialog with context', () => {
      instanceSelector.config.excludedObjects = [getContextPath().id];
      instanceSelector.select();

      let openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
      expect(openSpy.callCount).to.equal(1);

      let searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
      expect(searchConfig).to.exist;
      expect(searchConfig.triggerSearch).to.be.true;
      expect(searchConfig.arguments.properties).to.deep.equal(INSTANCE_SELECTOR_PROPERTIES);
      expect(searchConfig.results.config.exclusions).to.deep.equal([getContextPath().id]);

      let currentContext = openSpy.getCall(0).args[1];
      expect(currentContext).to.exist;
    });

    it('should configure and open picker dialog without context', () => {
      instanceSelector.idocContextFactory.getCurrentContext = () => undefined;
      instanceSelector.select();

      let openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
      expect(openSpy.callCount).to.equal(1);

      let searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
      expect(searchConfig).to.exist;
      expect(searchConfig.triggerSearch).to.be.true;
      expect(searchConfig.arguments.properties).to.deep.equal(INSTANCE_SELECTOR_PROPERTIES);
      expect(searchConfig.results.config.exclusions).to.deep.equal([]);

      let currentContext = openSpy.getCall(0).args[1];
      expect(currentContext).to.not.exist;
    });

    it('should not configure current object as exclusion if configured', () => {
      instanceSelector.config.excludeCurrentObject = false;
      instanceSelector.select();
      let openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
      let searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
      expect(searchConfig.results.config.exclusions).to.eql([]);
    });

    it('should not try to load properties if instance is not persisted', () => {
      instanceSelector.config.objectId = 'emf:123456';
      instanceSelector.config.isNewInstance = true;
      instanceSelector.select();
      expect(instanceSelector.instanceRestService.getInstanceProperty.calledOnce).to.be.false;
    });

    it('should configure the picker with restrictions if they are provided', () => {
      let restrictions = SearchCriteriaUtils.getDefaultRule();
      instanceSelector.config.pickerRestrictions = restrictions;
      instanceSelector.select();
      let openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
      let searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
      expect(searchConfig.restrictions).to.deep.equal(restrictions);
    });
  });

  describe('handleSelection', () => {
    // Assume selection mode is 'single'
    describe('in single selection mode', () => {

      it('should not update value and changeset if the selection is not changed', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0'], total: 1},
          defaultValue: {results: ['id:0'], total: 1}
        });
        instanceSelector.config.selection = SINGLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0']));

        expect(instanceSelector.instanceModelProperty.value.add, 'Changeset should not be changed').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.remove, 'Changeset should not be changed').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.results, 'Value should not be changed').to.eql(['id:0']);
      });

      it('should add single object in the changeset', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: [], total: 0},
          defaultValue: {results: [], total: 0}
        });
        instanceSelector.config.selection = SINGLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0']));

        expect(instanceSelector.instanceModelProperty.value.add, 'New value should be added to changeset').to.eql(['id:0']);
        expect(instanceSelector.instanceModelProperty.value.remove, 'remove changeset should be empty').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.results, 'New value should be applied').to.eql(['id:0']);
      });

      it('should replace selected object with a new selected one', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0'], total: 1},
          defaultValue: {results: ['id:0'], total: 1}
        });
        instanceSelector.config.selection = SINGLE_SELECTION;
        instanceSelector.ngOnInit();

        // new value is selected
        instanceSelector.handleSelection(createSelectedItemsArray(['id:1']));

        expect(instanceSelector.instanceModelProperty.value.add, 'Change set should be updated').to.eql(['id:1']);
        expect(instanceSelector.instanceModelProperty.value.remove, 'Change set should be updated').to.eql(['id:0']);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:1']);

        // and then select the initial value again
        instanceSelector.handleSelection(createSelectedItemsArray(['id:0']));

        expect(instanceSelector.instanceModelProperty.value.add, 'Change set should be empty').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.remove, 'Change set should be empty').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.results, 'Initial value should be selected').to.eql(['id:0']);
      });
    });

    describe('in multiple selection mode', () => {

      it('should add 2 objects in the changeset and results', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: [], total: 0},
          defaultValue: {results: [], total: 0}
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0', 'id:1']));

        expect(instanceSelector.instanceModelProperty.value.add).to.eql(['id:0', 'id:1']);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1']);
      });

      it('should add 2 more objects in the changeset and results', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0', 'id:1'], total: 2},
          defaultValue: {results: ['id:0', 'id:1'], total: 2}
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0', 'id:1', 'id:2', 'id:3']));

        expect(instanceSelector.instanceModelProperty.value.add, 'Should have added new values').to.eql(['id:2', 'id:3']);
        expect(instanceSelector.instanceModelProperty.value.remove, 'Should not have removed values').to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1', 'id:2', 'id:3']);
      });

      it('should remove all objects from the changeset and results', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0', 'id:1'], total: 2},
          defaultValue: {results: ['id:0', 'id:1'], total: 2}
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection([]);

        expect(instanceSelector.instanceModelProperty.value.add).to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql(['id:0', 'id:1']);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql([]);
      });

      it('should remove 2 objects from the changeset and results', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0', 'id:1', 'id:2', 'id:3'], total: 4},
          defaultValue: {results: ['id:0', 'id:1', 'id:2', 'id:3'], total: 4}
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0', 'id:1']));

        expect(instanceSelector.instanceModelProperty.value.add).to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql(['id:2', 'id:3']);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1']);
      });

      it('should add 2 objects in add array and results and remove 2 objects from the remove array', () => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:0', 'id:1', 'id:2', 'id:3'], total: 4},
          defaultValue: {results: ['id:0', 'id:1', 'id:2', 'id:3'], total: 4}
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        instanceSelector.handleSelection(createSelectedItemsArray(['id:0', 'id:1', 'id:4', 'id:5']));

        expect(instanceSelector.instanceModelProperty.value.add).to.eql(['id:4', 'id:5']);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql(['id:2', 'id:3']);
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1', 'id:4', 'id:5']);
      });

      it('should remove previously added objects from add array when they gets removed again', () => {
        instanceSelector = getComponentInstance();
        // Given I have loaded an object and for given object property I have two relations [id:0, id:1]
        // And I have added new relations to it [id:2, id:3]
        // Then The property value should look like this:
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {
            results: ['id:0', 'id:1', 'id:2', 'id:3'],
            add: ['id:2', 'id:3'],
            total: 4
          },
          defaultValue: {
            results: ['id:0', 'id:1'],
            total: 2
          }
        });
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        // When I deselect relations [id:2, id:3] again
        instanceSelector.handleSelection(createSelectedItemsArray(['id:0', 'id:1']));

        // Then These relations should be removed from the add array as they were added after the object was loaded
        expect(instanceSelector.instanceModelProperty.value.add, 'No relations should be present in add array').to.eql([]);
        // And The remove array should be empty as these relations actually are not present in backend for the object
        expect(instanceSelector.instanceModelProperty.value.remove, 'No relations should be present in remove array').to.eql([]);
        // And They should be removed from the results array as well
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1']);
      });

      it('should not send default values in the changeset', () => {
        instanceSelector = getComponentInstance();
        // Given I have relations [1, 2] by default
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: ['id:1', 'id:2'], total: 2},
          defaultValue: {results: ['id:1', 'id:2'], total: 2}
        });
        // And The selection mode is 'multiple'
        instanceSelector.config.selection = MULTIPLE_SELECTION;
        instanceSelector.ngOnInit();

        // When I add relation [3] and remove relation [1]
        instanceSelector.handleSelection(createSelectedItemsArray(['id:2', 'id:3']));

        // Then I expect to have result [2, 3], add [3], remove[1]
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:2', 'id:3']);
        expect(instanceSelector.instanceModelProperty.value.add).to.eql(['id:3']);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql(['id:1']);

        // When I add relation [1] again and remove relation [3]
        instanceSelector.handleSelection(createSelectedItemsArray(['id:1', 'id:2']));

        // Then I expect to have result [1, 2], add [], remove []
        expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:1', 'id:2']);
        expect(instanceSelector.instanceModelProperty.value.add).to.eql([]);
        expect(instanceSelector.instanceModelProperty.value.remove).to.eql([]);
      });
    });
  });

  describe('removeSelectedItem', () => {
    it('should remove deselected item from the results by index', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2', 'id:3'],
          add: ['id:2', 'id:3']
        }
      });
      instanceSelector.ngOnInit();
      instanceSelector.removeSelectedItem(2);
      expect(instanceSelector.instanceModelProperty.value.results).to.eql(['id:0', 'id:1', 'id:3']);
      expect(instanceSelector.instanceModelProperty.value.add).to.eql(['id:3']);
    });

    it('should load more of the selected objects if any when item is removed from the selection', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2'],
          total: 4
        }
      });
      instanceSelector.ngOnInit();
      instanceSelector.displayObjectsCount = 3;
      instanceSelector.config.visibleItemsCount = 3;

      instanceSelector.removeSelectedItem(2);

      expect(instanceSelector.instanceRestService.getInstanceProperty.calledOnce).to.be.true;
    });

    it('should decrement displayed objects count when selected items are less than configured count', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2', 'id:3'],
          total: 4
        }
      });
      instanceSelector.config.selection = MULTIPLE_SELECTION;
      instanceSelector.ngOnInit();
      instanceSelector.displayObjectsCount = 5;
      instanceSelector.config.visibleItemsCount = 5;
      instanceSelector.showingMore = true;

      instanceSelector.removeSelectedItem(2);

      expect(instanceSelector.displayObjectsCount).to.equal(4);
    });
  });

  describe('getHiddenObjectsCount', () => {
    it('should calculate count of the hidden objects', () => {
      let data = [
        {total: 0, displayObjectsCount: 0, expected: 0},
        {total: 1, displayObjectsCount: 1, expected: 0},
        {total: 5, displayObjectsCount: 3, expected: 2}
      ];
      data.forEach((set) => {
        instanceSelector = getComponentInstance();
        instanceSelector.instanceModelProperty = new InstanceModelProperty({
          value: {results: [], total: set.total}
        });
        instanceSelector.displayObjectsCount = set.displayObjectsCount;
        expect(instanceSelector.getHiddenObjectsCount(),
          `Expected counter [${set.expected}] with data ${JSON.stringify(set)}`).to.equal(set.expected);
      });
    });
  });

  describe('loadObjects', () => {
    it('should show loading indicator during objects loading', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.config.isNewInstance = false;
      instanceSelector.ngOnInit();
      sinon.spy(instanceSelector, 'setIsLoading');

      instanceSelector.loadObjects();

      expect(instanceSelector.setIsLoading.withArgs(true).calledOnce).to.be.true;
      expect(instanceSelector.setIsLoading.withArgs(false).calledOnce).to.be.true;
    });

    it('should not show loading indicator when no objects have to be loaded', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.config.isNewInstance = true;
      instanceSelector.ngOnInit();
      sinon.spy(instanceSelector, 'setIsLoading');

      instanceSelector.loadObjects();

      expect(instanceSelector.setIsLoading.notCalled).to.be.true;
    });

    it('should hide loading indicator if an error occurs during objects loading', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.config.isNewInstance = false;
      instanceSelector.ngOnInit();
      sinon.spy(instanceSelector, 'setIsLoading');
      instanceSelector.instanceRestService.getInstanceProperty.returns(PromiseStub.reject('server error'));

      instanceSelector.loadObjects().then(() => {
        expect(instanceSelector.setIsLoading.calledTwice).to.be.true;
        expect(instanceSelector.setIsLoading.withArgs(true).calledOnce).to.be.true;
        expect(instanceSelector.setIsLoading.withArgs(false).calledOnce).to.be.true;
        expect(instanceSelector.logger.error.calledOnce).to.be.true;
        expect(instanceSelector.logger.error.withArgs('server error').calledOnce).to.be.true;
      });
    });

    it('should update objectId if object property has sub properties', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.config.isNewInstance = false;
      instanceSelector.config.owningRelatedObjectId = 'emf:123456';
      instanceSelector.config.subPropertyName = 'owner';
      instanceSelector.ngOnInit();
      instanceSelector.loadObjects();

      expect(instanceSelector.config.objectId).to.equal('emf:123456');
      expect(instanceSelector.config.propertyName).to.equal('owner');
    });
  });

  describe('loadHeaders', () => {

    it('should delegate headers loading to service', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.ngOnInit();

      instanceSelector.loadHeaders(['id:0', 'id:1']);

      expect(instanceSelector.headersService.loadHeaders.calledWith(['id:0', 'id:1'], 'breadcrumb_header', {})).to.be.true;
    });

    it('should not load headers when they are already loaded', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            }
          }
        }
      });
      instanceSelector.ngOnInit();

      instanceSelector.loadHeaders(['id:0', 'id:1']);
      expect(instanceSelector.headersService.loadHeaders.notCalled).to.be.true;

      instanceSelector.loadHeaders([]);
      expect(instanceSelector.headersService.loadHeaders.notCalled).to.be.true;
    });

    it('should show and hide loading indicator during headers loading', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {}
        }
      });
      instanceSelector.ngOnInit();
      sinon.spy(instanceSelector, 'setIsLoading');

      instanceSelector.loadHeaders(['id:0', 'id:1']);

      expect(instanceSelector.setIsLoading.withArgs(true).calledOnce).to.be.true;
      expect(instanceSelector.setIsLoading.withArgs(false).calledOnce).to.be.true;
    });

    it('should not show loading indicator when there are no headers to be loaded', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            }
          }
        }
      });
      instanceSelector.ngOnInit();
      sinon.spy(instanceSelector, 'setIsLoading');

      instanceSelector.loadHeaders(['id:0', 'id:1']);

      expect(instanceSelector.setIsLoading.notCalled).to.be.true;
    });
  });

  describe('filterObjectsWithLoadedHeaders', () => {

    it('should remove ids for objects that already have loaded headers of given type', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: ['id:1']}
      });
      instanceSelector.ngOnInit();
      instanceSelector.instanceModelProperty.value.headers = {
        id1: {
          id: 'id1',
          compact_header: 'Header-1'
        },
        id2: {
          id: 'id2',
          compact_header: 'Header-2'
        },
        id3: {
          id: 'id3',
          default_header: 'Header-2'
        }
      };
      let ids = ['id1', 'id2', 'id3', 'id4', 'id5'];

      let actual = InstanceSelector.filterObjectsWithLoadedHeaders(ids, instanceSelector.instanceModelProperty.value.headers, HEADER_COMPACT);
      expect(actual).to.eql(['id3', 'id4', 'id5']);
      actual = InstanceSelector.filterObjectsWithLoadedHeaders(ids, instanceSelector.instanceModelProperty.value.headers, HEADER_DEFAULT);
      expect(actual).to.eql(['id1', 'id2', 'id4', 'id5']);
      actual = InstanceSelector.filterObjectsWithLoadedHeaders(ids, instanceSelector.instanceModelProperty.value.headers, HEADER_BREADCRUMB);
      expect(actual).to.eql(['id1', 'id2', 'id3', 'id4', 'id5']);
    });

    it('should not load headers which are already loaded', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {results: ['id:1']}
      });
      instanceSelector.ngOnInit();
      instanceSelector.instanceModelProperty.value.headers = {
        id1: {
          id: 'id1',
          header: 'Header-1'
        },
        id2: {
          id: 'id2',
          header: 'Header-2'
        }
      };

      sinon.spy(instanceSelector.instanceRestService, 'loadBatch');
      instanceSelector.loadHeaders(['id1', 'id2']);

      expect(instanceSelector.instanceRestService.loadBatch.notCalled).to.be.true;
    });
  });

  it('isEditMode should return true if mode is edit', () => {
    instanceSelector.config.mode = 'edit';
    expect(instanceSelector.isEditMode()).to.be.true;
    instanceSelector.config.mode = 'preview';
    expect(instanceSelector.isEditMode()).to.be.false;
  });

  it('should set header type according to the configuration', () => {
    instanceSelector.config.instanceHeaderType = HEADER_DEFAULT;
    instanceSelector.ngOnInit();
    expect(instanceSelector.headerType).to.equal(HEADER_DEFAULT);
  });

  describe('getUniqueItems', () => {

    it('should return empty array if arrays are equal', () => {
      let result = instanceSelector.getUniqueItems([{id: 1}], [{id: 1}]);
      expect(result.length).to.equal(0);
    });

    it('should return difference between new array values', () => {
      let result = instanceSelector.getUniqueItems([{id: 1}, {id: 2}], [{id: 1}]);
      expect(result.length).to.equal(1);
      expect(result).to.eql([{id: 2}]);
    });

  });

  describe('updateSelectionPool', () => {
    it('should update selection pool with new entries', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            }
          },
          total: 3,
          remove: [],
          add: []
        }
      });

      instanceSelector.instanceRestService.getInstanceProperty.returns(PromiseStub.resolve({
        data: ['id:0', 'id:1', 'id:3']
      }));

      instanceSelector.updateSelectionPool();
      expect(instanceSelector.selectionPool.size).to.eq(3)
    });

    it('should remove removed items from the pool', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            },
            'id:2': {
              id: 'id:2',
              breadcrumb_header: 'header'
            }
          },
          total: 3
        }
      });

      instanceSelector.updateSelectionPool();
      expect(instanceSelector.selectionPool.size).to.eq(3);

      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:2'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:2': {
              id: 'id:2',
              breadcrumb_header: 'header'
            }
          },
          total: 2
        }
      });
      instanceSelector.updateSelectionPool();
      expect(instanceSelector.selectionPool.size).to.eq(2);
      expect(instanceSelector.selectionPool.get('id:1')).to.eq(undefined);
    });

  });

  describe('handleSuggestedSelection', () => {

    it('should add to selection pool new entry in multiple mode', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.config.selection = MULTIPLE_SELECTION;
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            },
            'id:2': {
              id: 'id:2',
              breadcrumb_header: 'header'
            }
          },
          total: 3
        }
      });

      let newEntry = [{
        id: 'new',
        headers: {
          breadcrumb_header: 'bHeader',
          compact_header: 'cHeader',
          default_header: 'dHeader'
        }
      }];

      instanceSelector.handleSuggestedSelection(newEntry);
      expect(instanceSelector.selectionPool.size).to.eq(4);
      expect(instanceSelector.selectionPool.get('new')).to.deep.equal(newEntry[0]);
      expect(instanceSelector.instanceModelProperty.value.results).to.deep.equal(['id:0', 'id:1', 'id:2', 'new']);
    });

    it('should reset selection pool and add new entry in single mode', () => {
      instanceSelector = getComponentInstance();
      instanceSelector.config.selection = SINGLE_SELECTION;
      instanceSelector.instanceModelProperty = new InstanceModelProperty({
        value: {
          results: ['id:0', 'id:1', 'id:2'],
          headers: {
            'id:0': {
              id: 'id:0',
              breadcrumb_header: 'header'
            },
            'id:1': {
              id: 'id:1',
              breadcrumb_header: 'header'
            },
            'id:2': {
              id: 'id:2',
              breadcrumb_header: 'header'
            }
          },
          total: 3
        }
      });

      let newEntry = [{
        id: 'new',
        headers: {
          breadcrumb_header: 'bHeader',
          compact_header: 'cHeader',
          default_header: 'dHeader'
        }
      }];

      instanceSelector.handleSuggestedSelection(newEntry);
      expect(instanceSelector.selectionPool.size).to.eq(1);
      expect(instanceSelector.selectionPool.get('new')).to.deep.equal(newEntry[0]);
      expect(instanceSelector.instanceModelProperty.value.total).to.eql(1);
    });
  });

  function getComponentInstance() {

    let extensionsDialogService = stub(ExtensionsDialogService);
    let pickerResult = {};
    pickerResult[SEARCH_EXTENSION] = {
      results: {
        config: {
          selectedItems: [
            {
              id: 'id:999',
              headers: {
                'compact_header': 'compact-header',
                'breadcrumb_header': 'breadcrumb-header'
              }
            }
          ]
        }
      }
    };
    extensionsDialogService.openDialog.returns(PromiseStub.resolve(pickerResult));
    let pickerService = new PickerService(extensionsDialogService, stub(PickerRestrictionsService));

    let instanceRestService = stub(InstanceRestService);
    instanceRestService.loadBatch = (selectedItemIds) => {
      let result = selectedItemIds.map((selectedItemId) => {
        return {
          id: selectedItemId,
          headers: {
            default_header: `default-header-${selectedItemId}`,
            compact_header: `compact-header-${selectedItemId}`,
            breadcrumb_header: `breadcrumb_header-${selectedItemId}`
          }
        };
      });
      return PromiseStub.resolve({data: result});
    };
    instanceRestService.getInstanceProperty.returns(PromiseStub.resolve([]));

    let loggerStub = stub(Logger);

    let configurationStub = stub(Configuration);

    let headersServiceStub = stub(HeadersService);
    headersServiceStub.loadHeaders.returns(PromiseStub.resolve({}));
    let relationshipsService = stub(RelationshipsService);
    relationshipsService.getRelationInfo.returns(PromiseStub.resolve({data: {}}));

    return new InstanceSelector(pickerService, instanceRestService, mockContextFactoryService(), PromiseStub, loggerStub, configurationStub, headersServiceStub, mockTimeout(), relationshipsService);
  }

  function getContextPath() {
    return {
      id: 'test_id',
      type: 'caseinstance'
    };
  }

  function mockContextFactoryService() {
    return {
      getCurrentContext: () => {
        return {
          getCurrentObject: () => {
            let path = getContextPath();
            let instance = new InstanceObject(path.id);
            instance.setContextPath(path);
            return PromiseStub.resolve(instance);
          }
        };
      }
    };
  }

  function createSelectedItemsArray(ids) {
    return ids.map((id) => {
      return {
        id,
        headers: {
          compact_header: 'compact-header',
          breadcrumb_header: 'breadcrumb_header'
        }
      };
    });
  }

  function mockTimeout() {
    return (fn) => {
      fn();
    };
  }
});


