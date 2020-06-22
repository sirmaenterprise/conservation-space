/**
 * CKEditor plugin that allow the user to use the UP and DOWN arrow to move to the above/below cell when necessary.
 */
(function () {

  CKEDITOR.plugins.add('tablearrowsmovement', {

    init: function (editor) {
      editor.on('key', function (evt) {
        var keyCode = evt.data.keyCode;

        if (keyCode !== 38 && keyCode !== 40) {
          return;
        }

        var selection = editor.getSelection();
        var selectionStartElement = selection.getStartElement();

        if (selectionStartElement === null) {
          return;
        }

        // FF may return document and IE8 some UFO (object with no nodeType property...)
        // instead of an element (#11823).
        if (selectionStartElement.type !== CKEDITOR.NODE_ELEMENT) {
          return;
        }

        if (!selectionStartElement.is('td') && !selectionStartElement.getAscendant('td', true) &&
          !selectionStartElement.is('th') && !selectionStartElement.getAscendant('th', true)) {
          return;
        }

        var table = selectionStartElement.getAscendant('table', true);

        // Make sure the table we found is inside the container
        // (eg. we should not use tables the editor is embedded within)
        if ($(table.$).closest('.cke_editable').length === 0) {
          return;
        }

        // TODO: Do some validation!
        var startRange = selection.getRanges()[0];
        var caretElement = getCaretElement(startRange);
        var cellAscendant = caretElement.getAscendant('td', true) || caretElement.getAscendant('th', true);

        // Up arrow
        if (keyCode === 38) {
          if (hasElementAboveInTheCell(caretElement.$)) {
            return;
          }

          if (caretElement.$.nodeType === Node.TEXT_NODE && !isFirstLine(caretElement.$, selection)) {
            return;
          }

          evt.cancel();
          var aboveCell = getAboveCell(cellAscendant.$, table);
          var ckAboveCell = new CKEDITOR.dom.element(aboveCell);
          moveCursorToEndOfElement(editor, ckAboveCell);
        }

        // Down arrow
        else if (keyCode === 40) {
          if (hasElementBelowInTheCell(caretElement.$)) {
            return;
          }

          if (caretElement.$.nodeType === Node.TEXT_NODE && !isLastLine(caretElement.$, selection)) {
            return;
          }

          evt.cancel();
          var belowCell = getBelowCell(cellAscendant.$, table);
          var ckBelowCell = new CKEDITOR.dom.element(belowCell);
          moveCursorToBeginningOfElement(editor, ckBelowCell);
        }

      });
    }
  });

  /**
   * Gets the first element of a range.
   *
   * @param {CKEDITOR.dom.range} range.
   * @returns {CKEDITOR.dom.element} First element of a range.
   */
  function getCaretElement(range) {
    var caretElement = range.startContainer;
    if (range.startContainer.$.nodeType === Node.TEXT_NODE) {
      return caretElement;
    }
    if (range.startContainer.$.childNodes.length > range.startOffset) {
      if (range.startContainer.$.childNodes[range.startOffset].tagName !== 'BR') {
        caretElement = new CKEDITOR.dom.element(range.startContainer.$.childNodes[range.startOffset]);
      } else if (range.startOffset > 0) {
        caretElement = new CKEDITOR.dom.element(range.startContainer.$.childNodes[range.startOffset - 1]);
      }
      while (caretElement.getFirst && caretElement.getFirst() && caretElement.getFirst().$.tagName !== 'BR') {
        caretElement = caretElement.getFirst();
      }
      return caretElement;
    } else if (range.startContainer.$.childNodes.length === range.startOffset && range.startContainer.getLast().$.tagName !== 'BR') {
      caretElement = range.startContainer.getLast();
      while (caretElement.getLast && caretElement.getLast() && caretElement.getLast().$.tagName !== 'BR') {
        caretElement = caretElement.getLast();
      }
      return caretElement;
    } else {
      return range.startContainer;
    }
  }

  /**
   * Checks if there is element above the chosen one in the same cell.
   *
   * @param {Node} node to check.
   * @returns {Boolean} Return if there is an element above the chosen one in the same cell.
   */
  function hasElementAboveInTheCell(node) {
    var nodeTopPos = getTopPositionOfNode(node);
    var aboveNodeCandidate = node;
    var candidateTopPos;


    while (!nodeIsCell(aboveNodeCandidate)) {
      while (aboveNodeCandidate.previousSibling) {
        aboveNodeCandidate = aboveNodeCandidate.previousSibling;
        candidateTopPos = getTopPositionOfNode(aboveNodeCandidate);
        if (candidateTopPos) {
          if (hasTextNodeInside(aboveNodeCandidate) && nodeTopPos > candidateTopPos) {
            return true;
          }
          if (nodeTopPos >= candidateTopPos + getHeightOfNode(aboveNodeCandidate)) {
            return true;
          }
        }
      }
      aboveNodeCandidate = aboveNodeCandidate.parentElement || aboveNodeCandidate.parentNode;
    }
    return false;
  }

  /**
   * Checks if there is element below the chosen one in the same cell.
   *
   * @param {Node} node to check.
   * @returns {Boolean} Return if there is an element below the chosen one in the same cell.
   */
  function hasElementBelowInTheCell(node) {
    var nodeBottomPos = getTopPositionOfNode(node) + getHeightOfNode(node);
    var belowNodeCandidate = node;
    var candidateTopPos;

    while (!nodeIsCell(belowNodeCandidate)) {
      while (belowNodeCandidate.nextSibling) {
        belowNodeCandidate = belowNodeCandidate.nextSibling;
        candidateTopPos = getTopPositionOfNode(belowNodeCandidate);
        if (candidateTopPos) {
          if (hasTextNodeInside(belowNodeCandidate) &&
            nodeBottomPos < candidateTopPos + getHeightOfNode(belowNodeCandidate)) {
            return true;
          }
          if (nodeBottomPos <= candidateTopPos) {
            return true;
          }
        }
      }
      belowNodeCandidate = belowNodeCandidate.parentElement || belowNodeCandidate.parentNode;
    }
    return false;
  }

  /**
   * Gets all the cells from a table with rowSpan > 1 and stores them in an array together
   * with their row index and column index.
   *
   * @param {CKEDITOR.dom.element} table to get the cells from.
   * @returns {Array} Array containing each cell with rowSpan > 1 with its row and column index.
   */
  function getMultipleRowCells(table) {
    var multipleRowCellsArr = [];
    var multipleRowCellsWithTablePosition = [];
    var cells = table.find('td, th').$;

    for (var i = 0; i < cells.length; i++) {
      if (cells[i].rowSpan > 1) {
        multipleRowCellsArr.push(cells[i]);
      }
    }

    multipleRowCellsArr.forEach(function (multipleRowCell) {
      var colIndex = getColPosInTable(multipleRowCell);
      var rowIndex = getRowPosInTable(multipleRowCell);

      //The table 'registers' the <td> element of a multiple row cell only on its first row.
      //On the lower rows that cell is not marked in any way in the DOM.
      //So if a multiple row cell (that is to the left of the current cell)
      //started above the current cell and finished after the current cell
      //there is a need to add those cells to get the real X coordinates.
      multipleRowCellsWithTablePosition.forEach(function (cellWithCoordinates) {
        if (rowIndex > cellWithCoordinates.rowIndex && rowIndex < cellWithCoordinates.rowIndex + cellWithCoordinates.element.rowSpan && colIndex >= cellWithCoordinates.colIndex) {
          colIndex += cellWithCoordinates.element.colSpan;
        }
      });

      multipleRowCellsWithTablePosition.push({
        colIndex: colIndex,
        rowIndex: rowIndex,
        element: multipleRowCell
      });

      //Sorts the cells from left to right, because the cells to the left affect the other cells.
      multipleRowCellsWithTablePosition.sort(function (firstCell, secondCell) {
        return firstCell.colIndex - secondCell.colIndex;
      });
    });

    return multipleRowCellsWithTablePosition;
  }

  /**
   * Gets the cell standing directly above the chosen one.
   * Return the same cell if there is not a cell above the chosen one.
   *
   * @param {Node} cell to get the one above it.
   * @param {CKEDITOR.dom.element} table to get the cells from.
   * @returns {Node} cell that is above.
   */
  function getAboveCell(cell, table) {
    var currentRow = cell.parentElement;
    var previousRow = getRowAbove(currentRow);

    if (!previousRow) {
      return cell;
    }

    var cellIndexInRow = getColPosInTable(cell);
    var multipleRowCells = getMultipleRowCells(table);
    if (multipleRowCells.length > 0) {
      var rowPos = getRowPosInTable(cell);
      var cellColIndex = cellIndexInRow;

      for (var i = 0; i < multipleRowCells.length; i++) {
        //Since the multiple row cells are sorted from left to right, the first occurrence of a multiple row cell
        // that is to the right of the current cell can stop the loop
        if (cellColIndex < multipleRowCells[i].colIndex) {
          break;
        }
        //Calculates on what column the cell is in the table.
        if (rowPos > multipleRowCells[i].rowIndex && rowPos < multipleRowCells[i].rowIndex + multipleRowCells[i].element.rowSpan) {
          cellColIndex += multipleRowCells[i].element.colSpan;
        }
        //Checks if the current cell is directly above a multiple row cel, in which case returns is.
        if (rowPos === multipleRowCells[i].rowIndex + multipleRowCells[i].element.rowSpan && cellColIndex < multipleRowCells[i].colIndex + multipleRowCells[i].element.colSpan) {
          return multipleRowCells[i].element;
        }
        //Additional calculations of the actual position of the element.
        if (rowPos === multipleRowCells[i].rowIndex + 1) {
          cellIndexInRow += multipleRowCells[i].element.colSpan;
        } else if (rowPos === multipleRowCells[i].rowIndex + multipleRowCells[i].element.rowSpan) {
          cellIndexInRow -= multipleRowCells[i].element.colSpan;
        }
      }
    }

    return getCellAtColumnPosition(previousRow, cellIndexInRow);
  }

  /**
   * Gets the cell standing directly below the chosen one.
   * Return the same cell if there is not a cell below the chosen one.
   *
   * @param {Node} cell to get the one below it.
   * @param {CKEDITOR.dom.element} table to get the cells from.
   * @returns {Node} cell that is below.
   */
  function getBelowCell(cell, table) {
    var currentRow = cell.parentElement;
    var cellIndexInRow = getColPosInTable(cell);
    var multipleRowCells = getMultipleRowCells(table);
    var rowBelow = getRowBelow(currentRow);

    if (!rowBelow) {
      return cell;
    }

    if (multipleRowCells.length > 0) {
      var rowPos = getRowPosInTable(cell);
      var cellColIndex = cellIndexInRow;

      if (cell.rowSpan > 1) {
        var elementWithCoordinates;

        for (var i = 0; i < multipleRowCells.length; i++) {
          if (cell === multipleRowCells[i].element) {
            elementWithCoordinates = multipleRowCells[i];
          }
        }
        cellIndexInRow = elementWithCoordinates.colIndex;

        //The first row below the current is already calculated so we just increase the rowPos index
        rowPos++;
        for (i = 1; i < cell.rowSpan; i++) {
          rowBelow = getRowBelow(rowBelow);
          if (!rowBelow) {
            return cell;
          }
          rowPos++;
        }
        multipleRowCells.forEach(function (multipleRowCell) {
          if (multipleRowCell.rowIndex < rowPos && multipleRowCell.rowIndex + multipleRowCell.element.rowSpan - 1 >= rowPos && elementWithCoordinates.colIndex >= multipleRowCell.colIndex) {
            cellIndexInRow -= multipleRowCell.element.colSpan;
          }
        });

        return getCellAtColumnPosition(rowBelow, cellIndexInRow);
      }
      else {
        for (var i = 0; i < multipleRowCells.length; i++) {
          //Since the multiple row cells are sorted from left to right, the first occurrence of a multiple row cell
          // that is to the right of the current cell can stop the loop
          if (cellColIndex < multipleRowCells[i].colIndex) {
            break;
          }
          //Calculates the real column index of the cell.
          //Used to compare the X position between the current cell and the multiple row cells.
          if (rowPos > multipleRowCells[i].rowIndex && rowPos < multipleRowCells[i].rowIndex + multipleRowCells[i].element.rowSpan) {
            cellColIndex += multipleRowCells[i].element.colSpan;
          }
          //Additional calculations of the actual position of the <td> element.
          if (rowPos === multipleRowCells[i].rowIndex) {
            cellIndexInRow -= multipleRowCells[i].element.colSpan;
          } else if (rowPos === multipleRowCells[i].rowIndex + multipleRowCells[i].element.rowSpan - 1) {
            cellIndexInRow += multipleRowCells[i].element.colSpan;
          }
        }
      }
    }

    return getCellAtColumnPosition(rowBelow, cellIndexInRow);
  }

  /**
   * Gets the column index position of a cell in a table.
   *
   * @param {Node} cell to get the position from.
   * @returns {Integer} Index column position of the cell.
   */
  function getColPosInTable(cell) {
    var colPos = 1;
    while (cell.previousSibling) {
      cell = cell.previousSibling;
      colPos += cell.colSpan;
    }
    return colPos;
  }

  /**
   * Gets the row index position in a table. Keep in mind that the table can be divided into 3 sections
   * thead, tbody and tfoot. It gets the row pos in the table not in the section.
   *
   * @param {Node} node in table to get the row position from.
   * @returns {Integer} Index row position.
   */
  function getRowPosInTable(node) {
    var rowPos = 0;
    var row;
    while(node.parentNode) {
      if(node.nodeName === 'TR') {
        row = node;
        break;
      }
      node = node.parentNode;
    }
    if (!row) {
      return -1;
    }

    while (row) {
      row = getRowAbove(row);
      rowPos++;
    }
    return rowPos;
  }

  /**
   * Gets the row above the selected one. If that row doesn't exist, returns null.
   * @param row to get the row above it
   * @returns row above the selected.
   */
  function getRowAbove(row) {
    if (row.previousSibling) {
      return row.previousSibling;
    } else if (row.parentElement.previousSibling && row.parentElement.previousSibling.lastElementChild) {
      //Table can be divided into 3 sections thead, tbody and tfoot.
      return row.parentElement.previousSibling.lastElementChild;
    }
    return null;
  }

  /**
   * Gets the row below the selected one. If that row doesn't exist, returns null.
   * @param row to get the row below it
   * @returns row below the selected
   */
  function getRowBelow(row) {
    if (row.nextSibling) {
      return row.nextSibling;
    } else if (row.parentElement.nextSibling && row.parentElement.nextSibling.firstElementChild) {
      //Table can be divided into 3 sections thead, tbody and tfoot.
      return row.parentElement.nextSibling.firstElementChild;
    }
    return null;
  }

  /**
   * Gets the cell in a given position from a row
   *
   * @param {tr} rowElement to get the cell from.
   * @param {Integer} position of the cell in the row.
   * @returns {Node} cell standing in that position.
   */
  function getCellAtColumnPosition(rowElement, position) {
    var cellAtPosition = rowElement.firstChild;
    position -= cellAtPosition.colSpan;

    while (position > 0) {
      if (cellAtPosition.nextElementSibling) {
        cellAtPosition = cellAtPosition.nextElementSibling;
        position -= cellAtPosition.colSpan;
      } else {
        // TODO: Invalid position?
        return;
      }
    }

    return cellAtPosition;
  }

  /**
   * Gets the top position of the node.
   *
   * @param {Node} node to get the top position.
   * @returns {Integer} top position of the node.
   */
  function getTopPositionOfNode(node) {
    if (node.getBoundingClientRect) {
      return node.getBoundingClientRect().top;
    }

    var range = document.createRange();
    range.selectNodeContents(node);
    var topRect = range.getClientRects()[0];
    if (topRect) {
      return topRect.top;
    }

    return null;
  }

  /**
   * Gets the height of the node.
   *
   * @param {Node} node to get the height from.
   * @returns {Integer} Height of the node.
   */
  function getHeightOfNode(node) {
    if (node.offsetHeight) {
      return node.offsetHeight;
    }

    if (node.getBoundingClientRect) {
      return node.getBoundingClientRect().height;
    }

    return getLowermostRect(node).bottom - getUppermostRect(node).top;
  }

  /**
   * Checks if the selection is on the first line of a text node.
   *
   * @param {Node} textNode to check if the selection is on its first line.
   * @param {CKEDITOR.dom.selection} selection in the document.
   * @returns {Boolean} The selection is on the first line of a text node.
   */
  function isFirstLine(textNode, selection) {
    //If one/zero letters it must have only one line.
    if (textNode.length <= 1) {
      return true;
    }
    var selectionBoundingClientRect = getSelectionRect(selection);
    var textNodeTopPos = getTopPositionOfNode(textNode);
    return textNodeTopPos === selectionBoundingClientRect.top;
  }

  /**
   * Checks if the selection is on the last line of a text node.
   *
   * @param {Node} textNode to check if the selection is on its last line.
   * @param {CKEDITOR.dom.selection} selection in the document.
   * @returns {Boolean} The selection is on the last line of a text node.
   */
  function isLastLine(textNode, selection) {
    //If one/zero letters it must have only one line.
    if (textNode.length <= 1) {
      return true;
    }
    var selectionBoundingClientRect = getSelectionRect(selection);
    var textNodeBottomPos = getTopPositionOfNode(textNode) + getHeightOfNode(textNode);
    return textNodeBottomPos === selectionBoundingClientRect.bottom;
  }

  /**
   * Gets bounding client rectangle from a selection's range.
   * @param selection To get the rectangle form.
   * @return {ClientRect|*} rectangle of the current selection.
   */
  function getSelectionRect(selection) {
    var selectionRange = selection.getNative().getRangeAt(0);
    var selectionRect = selectionRange.getClientRects()[0];
    if (selectionRect && selectionRect.height > 0) {
      return selectionRect;
    }

    //If the height of a range's rectangle is 0, then all the values in the rectangle will be 0
    //This happens only when the user just changed a cell NOT via the mouse
    //So there are two options the caret to be either at the top or at the bottom of the cell
    var selectionElement = selectionRange.startContainer;
    if (selectionRange.startOffset === 0) {
      return getUppermostRect(selectionElement);
    } else {
      return getLowermostRect(selectionElement);
    }
  }

  /**
   * Gets the smallest part of an element that is on the uppermost of it
   * and retrieves it's rectangle.
   * @param selectionElement to get the sub-part from
   * @return {ClientRect|*} rectangle of the uppermost sub-element of the selectionElement
   */
  function getUppermostRect(selectionElement) {
    var uppermostRange = new Range();
    if (selectionElement.firstChild) {
      selectionElement = selectionElement.firstChild;
      while (selectionElement.tagName === 'BR') {
        selectionElement = selectionElement.nextSibling;
      }
    }
    uppermostRange.setStart(selectionElement, 0);
    uppermostRange.setEnd(selectionElement, 0);
    return uppermostRange.getClientRects()[0];
  }

  /**
   * Gets the smallest part of an element that is on the lowermost of it
   * and retrieves it's rectangle.
   * @param selectionElement to get the sub-part from
   * @return {ClientRect|*} rectangle of the lowermost sub-element of the selectionElement
   */
  function getLowermostRect(selectionElement) {
    var lowermostRange = new Range();
    if (selectionElement.lastChild) {
      selectionElement = selectionElement.lastChild;
      while (selectionElement.tagName === 'BR') {
        selectionElement = selectionElement.previousSibling;
      }
    }
    if (selectionElement.nodeType === Node.TEXT_NODE) {
      lowermostRange.setStart(selectionElement, selectionElement.length);
      lowermostRange.setEnd(selectionElement, selectionElement.length);
    } else {
      lowermostRange.setStart(selectionElement, selectionElement.childElementCount);
      lowermostRange.setEnd(selectionElement, selectionElement.childElementCount);
    }
    return lowermostRange.getClientRects()[0];
  }

  /**
   * Sets the caret of the beginning of a chosen element.
   *
   * @param {CKEDITOR.editor} editor variable of CKEditor.
   * @param {CKEDITOR.dom.element} element to set the focus to its beginning.
   */
  function moveCursorToBeginningOfElement(editor, element) {
    var sel = editor.getSelection();
    sel.selectElement(element);
    var ranges = editor.getSelection().getRanges();
    while (element.getFirst() && element.getFirst().$.childNodes.length > 0) {
      element = element.getFirst();
    }
    ranges[0].setStart(element, 0);
    ranges[0].setEnd(element, 0);
    sel.selectRanges([ranges[0]]);
  }

  /**
   * Sets the caret of the end of a chosen element.
   *
   * @param {CKEDITOR.editor} editor variable of CKEditor.
   * @param {CKEDITOR.dom.element} element to set the focus to its end.
   */
  function moveCursorToEndOfElement(editor, element) {
    var sel = editor.getSelection();
    sel.selectElement(element);
    var ranges = editor.getSelection().getRanges();
    while (element.getLast() && element.getLast().$.childNodes.length > 0) {
      element = element.getLast();
    }
    ranges[0].setStart(element, element.getChildCount());
    ranges[0].setEnd(element, element.getChildCount());
    sel.selectRanges([ranges[0]]);
  }

  /**
   * Checks if a chosen node is a text node or if there are any text nodes in its children.
   *
   * @param {Node} node to look for text nodes.
   * @returns {Boolean} The chosen node is a text node or there is a text node in its children.
   */
  function hasTextNodeInside(node) {
    if (node.nodeType === Node.TEXT_NODE) {
      return true;
    }
    if (node.hasChildNodes()) {
      for (var i = 0; i < node.childNodes.length; i++) {
        if (hasTextNodeInside(node.childNodes[i])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if a chosen node is a table header or table data element..
   *
   * @param {Node} node to check if cell.
   * @returns {Boolean} The chosen node is a cell.
   */
  function nodeIsCell(node) {
    if (!node.nodeType || node.nodeType !== Node.ELEMENT_NODE) {
      return false;
    }
    if (node.tagName === 'TD' || node.tagName === 'TH') {
      return true;
    }
    return false;
  }

})();
