import {Event} from 'app/app';

class MiradorEvent {
  constructor(args) {
    this.args = args;
  }

  getData() {
    return this.args;
  }
}

/**
 * Fired when mirador searches for annotations.
 */
@Event()
export class AnnotationListUpdatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

@Event()
export class AnnotationsRenderedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when annotation shape is created and dialog for the comment should pop up.
 */
@Event()
export class AnnotationShapeCreatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when the shape and comment are done and mirador sends the annotation to the store.
 */
@Event()
export class AnnotationCreatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when the miradors enters edit mode.
 */
@Event()
export class AnnotationEditModeEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when the comment for the annotation (the shape) is updated.
 */
@Event()
export class AnnotationUpdatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when an annotation is deleted.
 */
@Event()
export class AnnotationDeletedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when the above (search, create, edit, delete) actions are finished.
 */
@Event()
export class AnnotationListLoadedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when a new slot is added.
 */
@Event()
export class WindowSlotAddedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when a slot is removed.
 */
@Event()
export class WindowRemovedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when a different image is selected.
 */
@Event()
export class WindowUpdatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when mirador is loaded.
 */
@Event()
export class MiradorLoadedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when mirador is in image view and the comments for the image should be shown.
 */
@Event()
export class MiradorChangeViewTypeEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when mirador exits annotations mode and the annotations popup should be removed.
 */
@Event()
export class RemoveAnnotationsDialogEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when mirador fires its internal event for changed state.
 */
@Event()
export class MiradorSaveControllerUpdated extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when mirador should reload the annotation list.
 */
@Event()
export class UpdateMiradorAnnotationListEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}

/**
 * Fired when slot is being used.
 */
@Event()
export class SlotActivatedEvent extends MiradorEvent {
  constructor() {
    super(arguments);
  }
}
