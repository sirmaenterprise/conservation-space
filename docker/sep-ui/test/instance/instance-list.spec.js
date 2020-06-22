import {InstanceList} from 'instance/instance-list';
import {HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT} from 'instance-header/header-constants';

describe('InstanceList', function () {
  var instanceList;

  beforeEach(function () {
    instanceList = new InstanceList({
      loadBatch: sinon.spy(function (ids) {
        return {
          then: function (cb) {
            let data = ids.map((id) => {
              return {id: id};
            });

            cb({data});
          }
        };
      })
    });
  });

  describe('ngOnInit()', function () {

    it('should not load instances if they are provided', function () {
      instanceList.instances = [{
        id: 1,
        headers: {
          default_header: 'test'
        }
      }];
      instanceList.ngOnInit();

      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.called).to.be.false;
    });

    it('should not load instances if identifiers is empty', function () {
      instanceList.identifiers = [];
      instanceList.ngOnInit();

      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.called).to.be.false;
    });

    it('should load instances using the provided identifiers', function () {
      instanceList.identifiers = [1];
      instanceList.refreshInstances = sinon.spy();
      instanceList.ngOnInit();

      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.calledOnce).to.be.true;
      expect(instanceList.instanceService.loadBatch.getCall(0).args[0]).to.deep.eq([1]);
      expect(instanceList.instanceService.loadBatch.getCall(0).args[1]).to.deep.eq({
        params: {
          properties: [HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT]
        }
      });

      expect(instanceList.refreshInstances.calledOnce).to.be.true;
      expect(instanceList.refreshInstances.getCall(0).args[0]).to.deep.eq({data: [{id: 1}]});
    });

    it('should construct excluded identifiers objects', function () {
      instanceList.instances = [{id: 1}];
      instanceList.config = {
        exclusions: ['test-id']
      };

      instanceList.ngOnInit();
      expect(instanceList.excluded).to.deep.eq({'test-id': true});
    });
  });

  describe('loadHeaders(instances)', function () {

    it('should not load headers if there are no instances', function () {
      instanceList.loadHeaders(null);
      expect(instanceList.instanceService.loadBatch.called).to.be.false;
      expect(instanceList.finishedLoading).to.be.true;

      instanceList.loadHeaders([]);
      expect(instanceList.instanceService.loadBatch.called).to.be.false;
      expect(instanceList.finishedLoading).to.be.true;
    });

    it('should not load headers if they are loaded', function () {
      instanceList.loadHeaders([{
        headers: {
          default_header: 'test'
        }
      }]);

      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.called).to.be.false;
    });

    it('should load headers if there is at least one w/o headers', function () {
      var instances = [{
        id: 1,
        headers: {
          default_header: 'test'
        }
      }, {
        id: 2
      }];

      instanceList.loadHeaders(instances);
      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.calledOnce).to.be.true;
      expect(instanceList.instanceService.loadBatch.getCall(0).args[0]).to.deep.eq([1, 2]);
      expect(instanceList.instanceService.loadBatch.getCall(0).args[1]).to.deep.eq({
        params: {
          properties: [HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT]
        }
      });

      instances[1].headers = {};
      instanceList.instanceService.loadBatch.reset();
      instanceList.loadHeaders(instances);
      expect(instanceList.finishedLoading).to.be.true;
      expect(instanceList.instanceService.loadBatch.calledOnce).to.be.true;
      expect(instanceList.instanceService.loadBatch.getCall(0).args[0]).to.deep.eq([1, 2]);
      expect(instanceList.instanceService.loadBatch.getCall(0).args[1]).to.deep.eq({
        params: {
          properties: [HEADER_DEFAULT, HEADER_BREADCRUMB, HEADER_COMPACT]
        }
      });
    });
  });

  describe('refreshInstances(response)', function () {
    it('should not set instances if response does not have data', function () {
      instanceList.refreshInstances(null);
      expect(instanceList.instances).to.deep.equal([]);

      instanceList.refreshInstances({});
      expect(instanceList.instances).to.deep.equal([]);

      instanceList.refreshInstances({data: []});
      expect(instanceList.instances).to.deep.equal([]);
    });

    it('should set instances if response has data', function () {
      instanceList.refreshInstances({data: [1]});
      expect(instanceList.instances).to.deep.eq([1]);
    });

    it('should preserve the array reference', () => {
      var originalInstances = [1];
      instanceList.instances = originalInstances;
      instanceList.refreshInstances({data: [2]});
      expect(instanceList.instances).to.equal(originalInstances);
    });
  });

  describe('onClick(instance)', function () {
    var instance = {id: 'test'};

    it('should call the configured selection handler', function () {
      instanceList.excluded = {};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};
      instanceList.onClick(instance);

      expect(instanceList.config.selectionHandler.calledOnce).to.be.true;
      expect(instanceList.config.selectionHandler.getCall(0).args[0]).to.deep.eq(instance);
    });

    it('should not call the configured selection handler if instance is excluded', function () {
      instanceList.excluded = {'test': true};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};
      instanceList.onClick(instance);

      expect(instanceList.config.selectionHandler.called).to.be.false;
    });

    it('should not call the configured selection handler if there are no selectable items', function () {
      instanceList.excluded = {};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: false};
      instanceList.onClick(instance);

      expect(instanceList.config.selectionHandler.called).to.be.false;
    });
  });

  describe('isSelected(instance)', function () {

    it('should return true if isntance is contained in selectedItems', function () {
      instanceList.selectedItems = [{id: 1}];
      expect(instanceList.isSelected({id: 1})).to.be.true;
    });

    it('should return false if isntance is not contained in selectedItems', function () {
      instanceList.selectedItems = [{id: 1}];
      expect(instanceList.isSelected({id: 2})).to.be.false;
    });
  });

  describe('selectionControlType()', function () {

    it('should be radio if single selection', function () {
      instanceList.config = {singleSelection: true};
      expect(instanceList.selectionControlType).to.eq('radio');
    });

    it('should be checkbox if multiple selection', function () {
      instanceList.config = {singleSelection: false};
      expect(instanceList.selectionControlType).to.eq('checkbox');
    });
  });

  describe('instance list batch selection mode', () => {

    it('should by default be configured to use select all & deselect all options', () => {
      expect(instanceList.config.selectAll).to.be.true;
      expect(instanceList.config.deselectAll).to.be.true;
    });

    it('should not enable batch selection mode if configuration is not properly set', () => {
      instanceList.filteredInstances = [];
      instanceList.config = {
        singleSelection: false,
      };

      expect(instanceList.isSelectDeselectEnabled()).to.be.false;
    });

    it('should enable batch selection mode if configuration properly set', () => {
      instanceList.filteredInstances = [{id: 1}];
      instanceList.config = {
        singleSelection: false,
        selectableItems: true
      };
      expect(instanceList.isSelectDeselectEnabled()).to.be.true;
    });

    it('should select only non selected instances', () => {
      instanceList.excluded = {};
      instanceList.selectedItems = [{id: '2'}];
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};
      instanceList.filteredInstances = [{id: '1'}, {id: '2'}, {id: '3'}];

      instanceList.selectAll();
      expect(instanceList.config.selectionHandler.callCount).to.equal(2);
    });

    it('should select only non excluded instances', () => {
      instanceList.selectedItems = [];
      instanceList.excluded = {'1': true};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};
      instanceList.filteredInstances = [{id: '1'}, {id: '2'}, {id: '3'}];

      instanceList.selectAll();
      expect(instanceList.isSelected({id: '1'})).to.be.false;
      expect(instanceList.config.selectionHandler.callCount).to.equal(2);
    });

    it('should select all instances when no exclusions are specified', () => {
      instanceList.excluded = {};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};
      instanceList.filteredInstances = [{id: '1'}, {id: '2'}, {id: '3'}, {id: '4'}];

      instanceList.selectAll();
      expect(instanceList.config.selectionHandler.callCount).to.equal(4);
    });

    it('should deselect all instances', () => {
      instanceList.excluded = {};
      instanceList.config = {selectionHandler: sinon.spy(), selectableItems: true};

      instanceList.selectedItems = [{id: '1'}, {id: '2'}];
      instanceList.filteredInstances = [{id: '1'}, {id: '2'}, {id: '3'}, {id: '4'}];

      instanceList.deselectAll();
      expect(instanceList.config.selectionHandler.callCount).to.equal(2);
    });
  });
});