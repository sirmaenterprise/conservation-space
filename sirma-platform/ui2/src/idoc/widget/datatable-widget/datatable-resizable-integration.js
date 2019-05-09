import {Injectable} from 'app/app';
import _ from 'lodash';

/**
 * Created by svelikov on 5/14/18.
 */
@Injectable()
export class DatatableResizableIntegration {

  /**
   * Wrap header cells in resizable component
   * @param element datatable widget dom element
   * @param widget an idoc widget reference
   * @param headerCells cells (columns) in datatable widget header
   * @param tableHeader datatable widget header panel
   * @param tableBody datatable widget body panel
   * @param columnMinWidth the minimum configured column width
   * @param isResizableDisabled a callback which resolves with boolean true if resizable should be enabled and false otherwise
   */
  applyResizable(element, widget, headerCells, tableHeader, tableBody, columnMinWidth, isResizableDisabled) {
    let paramsObject = {
      subTotalWidth: 0,
      parentWidth: 0,
      nextColumn: {}
    };
    headerCells.resizable({
      handles: 'e',
      minWidth: columnMinWidth,
      start(event, ui) {
        DatatableResizableIntegration.onResizeStart(ui, paramsObject, tableHeader);
      },
      stop() {
        DatatableResizableIntegration.onResizeStop(widget, element.find('.header-cell'));
      },
      resize(event, ui) {
        DatatableResizableIntegration.onResize(ui, paramsObject, element, tableHeader, tableBody, widget, this);
      }
    });
    DatatableResizableIntegration.disableResizable(headerCells, isResizableDisabled);
  }

  static disableResizable(elements, isResizableDisabled) {
    elements.resizable('option', 'disabled', isResizableDisabled());
  }

  static onResizeStart(ui, paramsObject, tableHeader) {
    paramsObject.nextColumn = ui.originalElement.next();
    paramsObject.subTotalWidth = ui.originalSize.width + paramsObject.nextColumn.outerWidth();
    paramsObject.parentWidth = tableHeader.width();
  }

  static onResizeStop(widget, cells) {
    cells.each(function () {
      let cellName = $(this).attr('data-header-cell-name');
      widget.config.styles.columns[cellName].width = $(this).width();
      widget.config.styles.columns[cellName].calculatedWidth = widget.config.styles.columns[cellName].width;
    });
    widget.control.getBaseWidget().saveConfigWithoutReload(widget.config);
  }

  static onResize(ui, paramsObject, element, tableHeader, tableBody, widget, cell) {
    let $cell = $(cell);
    let cellName = $cell.attr('data-header-cell-name').replace(/:/i, '\\:');
    let resizeCells = `${cellName}-wrapper`;
    let tableBodyColumn = element.find(`.table-body form #${resizeCells}, .datatable-filter .${resizeCells}`);

    if (element.width() > DatatableResizableIntegration.newTableWidth(paramsObject.parentWidth, ui)) {
      // restore last cell width if user try to make it smaller than table
      tableHeader.add(tableBody);
      DatatableResizableIntegration.setTableWrapperWidth(element, element.width());
      widget.actualPanelsWidth = element.width();
      if (_.isEmpty(paramsObject.nextColumn) && ui.originalSize.width > ui.size.width) {
        setTimeout(function () {
          tableBodyColumn.width($cell.width());
          $cell.resizable('widget').width($cell.width());
        }, 0);
        $cell.resizable('widget').trigger('mouseup');
        return;
      }
      paramsObject.nextColumn.width(paramsObject.subTotalWidth - ui.size.width);
      tableBodyColumn.width(ui.size.width).next().width(paramsObject.subTotalWidth - ui.size.width);
    } else {
      tableHeader.add(tableBody);
      DatatableResizableIntegration.setTableWrapperWidth(element, DatatableResizableIntegration.newTableWidth(paramsObject.parentWidth, ui));
      widget.actualPanelsWidth = DatatableResizableIntegration.newTableWidth(paramsObject.parentWidth, ui);
      if (_.isEmpty(paramsObject.nextColumn)) {
        element.scrollLeft(tableHeader.width());
      }
      tableBodyColumn.width(ui.size.width);
    }
  }

  static newTableWidth(parentWidth, ui) {
    return parentWidth + (ui.size.width - ui.originalSize.width);
  }

  static setTableWrapperWidth(parentElement, newWidth) {
    parentElement.find('.table-wrapper').width(newWidth);
  }

}
