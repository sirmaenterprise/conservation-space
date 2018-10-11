# table plugin

  According to scenario described in this issue https://jira.sirmaplatform.com/jira/browse/CMF-25797, it's not possible
  to demote header rows to normal body rows if the table doesn't have other body rows. The reason is that the dom element
  returned by the call to tbody.getFirst() returns null when there are no children in the tbody element.

  Original code (line 181):
  ```javascript
  if ( table.$.tHead !== null && !( headers == 'row' || headers == 'both' ) ) {
    // Move the row out of the THead and put it in the TBody:
    thead = new CKEDITOR.dom.element( table.$.tHead );
    tbody = table.getElementsByTag( 'tbody' ).getItem( 0 );

    var previousFirstRow = tbody.getFirst();
    while ( thead.getChildCount() > 0 ) {
      theRow = thead.getFirst();
      for ( i = 0; i < theRow.getChildCount(); i++ ) {
        var newCell = theRow.getChild( i );
        if ( newCell.type == CKEDITOR.NODE_ELEMENT ) {
          newCell.renameNode( 'td' );
          newCell.removeAttribute( 'scope' );
        }
      }
      theRow.insertBefore( previousFirstRow );
    }
    thead.remove();
  }
  ```
  Changed to:
  ```javascript
  if ( table.$.tHead !== null && !( headers == 'row' || headers == 'both' ) ) {
    // Move the row out of the THead and put it in the TBody:
    thead = new CKEDITOR.dom.element( table.$.tHead );
    tbody = table.getElementsByTag( 'tbody' ).getItem( 0 );

    var previousFirstRow = tbody.getFirst();
    var command = 'insertBefore';
    if ( !previousFirstRow ) {
      command = 'appendTo';
      previousFirstRow = tbody;
    }
    while ( thead.getChildCount() > 0 ) {
      theRow = thead.getFirst();
      for ( i = 0; i < theRow.getChildCount(); i++ ) {
        var newCell = theRow.getChild( i );
        if ( newCell.type == CKEDITOR.NODE_ELEMENT ) {
          newCell.renameNode( 'td' );
          newCell.removeAttribute( 'scope' );
        }
      }
      theRow[command]( previousFirstRow );
    }
    thead.remove();
  }
  ```

# Custom icons Preset
  According to https://ittruse.ittbg.com/jira/browse/CMF-21912 ckeditor should use custom icons preset
  Copy the content of custom_icons folder to the build after updating

# Updating icons preset after plugin addition/removal
  custom_icons files need to be modified by removing or adding the needed icons.
  The new ckeditor that is downloaded will hold the changeset of the icons and
  image manipulation software must be used to update the icons set. After that copy the icons to the plugin directory.

# Custom plugins list

# SesTableresize plugin

  Regarding https://ittruse.ittbg.com/jira/browse/CMF-21570
  When resizing the last column of the table and changing its width, the tableresize
  plugin sets ith width in pixels. To be resizable, we have to recalculate the table
  width in percent.

  Original code:
  ```javascript
    leftCell && leftCell.setStyle( 'width', pxUnit( Math.max( leftOldWidth + sizeShift, 1 ) ) );
    rightCell && rightCell.setStyle( 'width', pxUnit( Math.max( rightOldWidth - sizeShift, 1 ) ) );
  ```
  Custom change:
  ```javascript
    leftCell && leftCell.setStyle( 'width', ( Math.max( leftOldWidth + sizeShift, 1 ) ) / getWidth(table) * 100 + '%');
    rightCell && rightCell.setStyle( 'width', ( Math.max( rightOldWidth - sizeShift, 1 ) ) / getWidth(table) * 100 + '%');
  ```
  Original code:
  `
    table.setStyle( 'width', pxUnit( tableWidth + sizeShift * ( rtl ? -1 : 1 ) ) );
  `
  Custom change:
  `
    table.setStyle('width', ( ( tableWidth + sizeShift * ( rtl ? -1 : 1 ) ) / getWidth(editor.element)) * 100 + '%');
  `

  Regarding https://ittruse.ittbg.com/jira/browse/CMF-21817
  IE doesn't calculate computed border-width style properly. While current style is returned as medium, computed style is 0px which is inconsistent.
  Added rounding to integer of pillars' x position because pilars are sometimes calculated outside of the table (by a small, less than 1 number) where mousemove event is not handled.
  See full diff in src/common/lib/ckeditor/patches/CMF_21817.patch

  Tableresize plugin is replaced by SesTableresize.

# Image
  Regarding Browser crash when Image properties dialog is shown for inline images https://ittruse.ittbg.com/jira/browse/CMF-22767

  Added fix:
  txtUrl field is disabled if image is base64 encoded
  Added browse button and file input to browse file from the system and convert it to base64
  See changes in src/common/lib/ckeditor/patches/CMF-22767.patch

# Liststyle
  About problem with numbered list styling - https://ittruse.ittbg.com/jira/browse/CMF-24685

  The problem occurs because of https://ittruse.ittbg.com/jira/browse/CMF-19444 which uses custom method for numbering which breaks default behavior.
  Few styles are added to src/idoc/editor/editor.scss for ol and their child li elements.
  Also there are few changes in liststyle plugin which apply these styles to respectful elements.
  See changes in src/common/lib/ckeditor/patches/CMF-24685.patch

# Line height
  To add new language you should add language file in lang folder similar to en.js and to add new language abbreviation to lineheight/plugin.js line 67 (lang: '...')

# Steps to update to newer version (Only under Linux)

`
 run gulp ckeditor:update
`

or
`
 gulp ckeditor:update --url=<url_from_build-config>
`

Check the plugins for migration and ... test!