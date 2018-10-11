import {Injectable} from 'app/app';
import {EditorContentProcessor} from 'idoc/editor/editor-content-processor';

/*
 Used when idoc is loaded to process the empty spans attributes which are not widgets.
 Adds a Zero Width Non-Joiner (&#8204) to the inner html so that lines don't appear like missing.

 Used for before appending to editor processing, and also before save processing.
 */
@Injectable()
export class IdocEditorContentProcessor extends EditorContentProcessor {

  preprocessContent(editorInstance, content) {
    let editorContent = $('<div>').append(content);
    editorContent.find('span:not([widget])').each(function () {
      let span = $(this);
      if (span.html().length === 0 || (span.children().length === 1 && span.children(0).is('br'))) {
        span.html('&#8204');
      }
    });

    return editorContent.html();
  }


  postprocessContent() {
    // not implemented
  }
}