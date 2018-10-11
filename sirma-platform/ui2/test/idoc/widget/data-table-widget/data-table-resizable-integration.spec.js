import {DatatableResizableIntegration} from 'idoc/widget/datatable-widget/datatable-resizable-integration';
import {instantiateDataTableWidget, stubElementFind} from 'test/idoc/widget/data-table-widget/datatable-test-helpers';

describe('DatatableResizableIntegration', () => {

  describe('Resize', () => {
    let ui = {
      originalElement: {
        next: () => {
          return {
            outerWidth: () => 50
          };
        }
      },
      originalSize: {
        width: 100
      },
      size: {
        width: 150
      }
    };

    it('should initialize resizable params correct', () => {
      let paramsObject = {
        subTotalWidth : 0,
        parentWidth : 0,
        nextColumn: {}
      };
      let tableHeader = {
        width: () => 700
      };
      DatatableResizableIntegration.onResizeStart(ui, paramsObject, tableHeader);
      expect(paramsObject.subTotalWidth).to.equal(150);
      expect(paramsObject.parentWidth).to.equal(700);
    });

    it('should update widget config after resize', () => {
      let paramsObject = {
        subTotalWidth : 0,
        parentWidth : 0,
        nextColumn: {}
      };
      let widget = instantiateDataTableWidget();
      widget.config.styles.columns = {
        column1: {width: 20},
        column2: {width: 40},
        column3: {width: 60}
      };
      let expected = {
        column1: {
          calculatedWidth: 20,
          width: 20
        },
        column2: {
          calculatedWidth: 150,
          width: 150
        },
        column3: {
          calculatedWidth: 60,
          width: 60
        }
      };
      let cells = $(`<div data-header-cell-name='column1' style='width: 20px'></div><div data-header-cell-name='column2' style='width: 150px'></div><div data-header-cell-name='column3' style='width: 60px'></div>`);
      DatatableResizableIntegration.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);

      // default (min table) width can not be changed
      widget.$element.width = () => {
        return 200;
      };
      DatatableResizableIntegration.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);

      widget.$element.width = () => {
        return 20;
      };
      paramsObject.nextColumn = {
        attr: () => {return 'column3';},
        width: () => 20
      };
      expected = {
        column1: {
          calculatedWidth: 20,
          width: 20
        },
        column2: {
          calculatedWidth: 150,
          width: 150
        },
        column3: {
          calculatedWidth: 20,
          width: 20
        }
      };
      cells = $(`<div data-header-cell-name='column1' style='width: 20px'></div><div data-header-cell-name='column2' style='width: 150px'></div><div data-header-cell-name='column3' style='width: 20px'></div>`);
      DatatableResizableIntegration.onResizeStop(widget, cells);
      expect(widget.config.styles.columns).to.eql(expected);
    });

    it('should calculate new table width correct', () => {
      expect(DatatableResizableIntegration.newTableWidth(350, ui)).to.equal(400);
    });

    it('should recalculate cells width if editor size is changed', () => {
      let widget = instantiateDataTableWidget();
      widget.$element.find = stubElementFind();
      widget.panelsWidth = 500;
      widget.actualPanelsWidth = 500;
      widget.recalculatePanelsWidth(1000);
      expect(widget.panelsWidth).to.equal(1000);

      widget.elementIsResized = true;
      widget.recalculatePanelsWidth(400);
      expect(widget.panelsWidth).to.equal(500);
    });

    it('should reset panels width in print mode', () => {
      let widget = instantiateDataTableWidget();
      widget.$element.find = stubElementFind();
      widget.panelsWidth = 500;
      widget.context.isPrintMode = () => {
        return true;
      };
      widget.recalculatePanelsWidth(1000);
      expect(widget.panelsWidth).to.eql({});
    });
  });

});