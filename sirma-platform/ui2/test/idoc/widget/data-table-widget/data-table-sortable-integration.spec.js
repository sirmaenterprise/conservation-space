import {DatatableSortableIntegration} from 'idoc/widget/datatable-widget/datatable-sortable-integration';
import {instantiateDataTableWidget} from 'test/idoc/widget/data-table-widget/datatable-test-helpers';

describe('DatatableSortableIntegration', () => {

  describe('Sort', () => {
    it('should initialize sortable params correct', () => {
      let paramsObject = {
        initialColumnWidth: 0,
        itemIndex: 0
      };
      let cell = {
        width: () => 100,
        index: () => 5
      };
      let expected = {
        initialColumnWidth: 100,
        itemIndex: 5
      };
      DatatableSortableIntegration.onDragStart(cell, paramsObject);
      expect(paramsObject).to.eql(expected);
    });

    it('should update widget config after reorder', () => {
      let paramsObject = {
        initialColumnWidth: 0,
        itemIndex: 0
      };
      let cell = {
        width: () => 100,
        index: () => 2
      };
      let widget = instantiateDataTableWidget();
      widget.widgetConfig.headers = [{name: 'field1'}, {name: 'field2'}, {name: 'field3'}];
      let expected = {field2: {index: 0}, field3: {index: 1}, field1: {index: 2}};

      DatatableSortableIntegration.onDrop(cell, paramsObject, widget);
      expect(widget.config.columnsOrder.columns).to.eql(expected);

      // Cover onDrop after columns are already reordered
      expected = {field3: {index: 0}, field1: {index: 1}, field2: {index: 2}};
      DatatableSortableIntegration.onDrop(cell, paramsObject, widget);
      expect(widget.config.columnsOrder.columns).to.eql(expected);
    });
  });

});