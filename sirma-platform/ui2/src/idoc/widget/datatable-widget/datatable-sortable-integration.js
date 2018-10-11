import {Injectable} from 'app/app';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';
import _ from 'lodash';

/**
 * Created by svelikov on 5/14/18.
 */
@Injectable()
export class DatatableSortableIntegration {

  /**
   * Wrap header cells in sortable component
   * @param tableHeader datatable widget header panel
   * @param widget idoc widget reference
   * @param isSortableDisabled a callback which resolves with boolean true when sortable should be disabled and false otherwise
   */
  applySortable(tableHeader, widget, isSortableDisabled) {
    let paramsObject = {
      initialColumnWidth: 0,
      itemIndex: 0
    };
    DragAndDrop.makeDraggable(tableHeader, {
      // ol or ul with li by default
      itemSelector: '.header-cell',
      containerSelector: '.table-header',
      delay: 200,
      handle: '.title-cell',
      onDragStart: function ($item, container, _super) {
        _super($item, container);
        container.el.css({'padding-left': '5px'});
        DatatableSortableIntegration.onDragStart($item, paramsObject);
      },
      onDrop: function ($item, container, _super) {
        _super($item, container);
        container.el.css({'padding': '0px'});
        DatatableSortableIntegration.onDrop($item, paramsObject, widget);
      }
    });
    DatatableSortableIntegration.disableSortable(tableHeader, isSortableDisabled);
  }

  static disableSortable(element, isSortableDisabled) {
    if (isSortableDisabled()) {
      DragAndDrop.disable(element);
    } else {
      DragAndDrop.enable(element);
    }
  }

  static onDragStart($item, paramsObject) {
    paramsObject.initialColumnWidth = $item.width();
    paramsObject.itemIndex = $item.index();
  }

  static onDrop($item, paramsObject, widget) {
    $item.width(paramsObject.initialColumnWidth);
    let cellsArray;
    if (_.isEmpty(widget.config.columnsOrder.columns)) {
      cellsArray = widget.widgetConfig.headers.map((header) => {
        return header.name;
      });
    } else {
      cellsArray = _.sortBy(Object.keys(widget.config.columnsOrder.columns), (columnName) => {
        return widget.config.columnsOrder.columns[columnName].index;
      });
    }

    cellsArray.splice($item.index(), 0, cellsArray.splice(paramsObject.itemIndex, 1)[0]);
    let orderMap = {};
    cellsArray.forEach(function (cell, index) {
      orderMap[cell] = {
        index
      };
    });
    widget.config.columnsOrder.columns = orderMap;
    widget.control.saveConfig(widget.config);
  }

}