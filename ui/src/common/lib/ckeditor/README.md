# Custom plugins list
* dropdownmenumanager
* pastefromsep


# Custom icons Preset
  According to https://ittruse.ittbg.com/jira/browse/CMF-21912 ckeditor should use custom icons preset
  Copy the content of custom_icons folder to the build after updating


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
  Tableresize plugin is replaced by SesTableresize.

# Pastebase64

  Regarding CMF-20803 Cannot paste images from Word into CK Editor https://ittruse.ittbg.com/jira/browse/CMF-20803
            CMF-21760 Cannot paste(visualize) images from clipboard to CK editor https://ittruse.ittbg.com/jira/browse/CMF-21760

  Added fix:
             Before paste we calculate element width in order to size it to fit the target container if bigger
              if(element.$.width > event.target.parentNode.clientWidth){
                             element.setStyle('width', 100 + '%');
                           }

# Image
  Regarding Browser crash when Image properties dialog is shown for inline images https://ittruse.ittbg.com/jira/browse/CMF-22767

  Added fix:
  txtUrl field is disabled if image is base64 encoded
  Added browse button and file input to browse file from the system and convert it to base64
  See changes in src/common/lib/ckeditor/patches/CMF-22767.patch

# Steps to update to newer version (Only under Linux)

`
 run gulp ckeditor:update
`

or
`
 gulp ckeditor:update --url=<url_from_build-config>
`

Check the plugins for migration and ... test!