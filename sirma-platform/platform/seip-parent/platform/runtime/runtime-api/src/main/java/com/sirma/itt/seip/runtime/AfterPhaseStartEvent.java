/**
 *
 */
package com.sirma.itt.seip.runtime;

import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Event fired after completion of each startup phase and before next phase start. Observers could delay phase execution
 * by calling the {@link #pause()} method. The phase execution could be resumed by calling the method {@link #resume()}.
 * <p>
 * The observer are allowed to cache the event instance but only until the phase completion. After phase startup is
 * completed new event instance is created and fired.
 * <p>
 * Note that the {@link StartupPhase#DEPLOYMENT} phase cannot be vetoed.
 *
 * @author BBonev
 */
public class AfterPhaseStartEvent {

	/** The completed phase. */
	private final StartupPhase completedPhase;

	/** The next phase. */
	private final StartupPhase nextPhase;

	/** The vetos. */
	private volatile boolean paused = false;

	/**
	 * Instantiates a new after phase start event.
	 *
	 * @param completedPhase
	 *            the completed phase
	 * @param nextPhase
	 *            the next phase
	 */
	public AfterPhaseStartEvent(StartupPhase completedPhase, StartupPhase nextPhase) {
		this.completedPhase = completedPhase;
		this.nextPhase = nextPhase;
	}

	/**
	 * Mark the remaining startup phases as paused.
	 */
	public void pause() {
		paused = true;
	}

	/**
	 * Resume the phase loading
	 */
	public void resume() {
		paused = false;
	}

	/**
	 * Returns <code>true</code> if the next phase could be executed.
	 *
	 * @return true, if allowed to execute next phase.
	 */
	public boolean canContinue() {
		return !paused;
	}

	/**
	 * Gets the completed phase.
	 *
	 * @return the completed phase, never <code>null</code>
	 */
	public StartupPhase getCompletedPhase() {
		return completedPhase;
	}

	/**
	 * Gets the next phase.
	 *
	 * @return the next phase, never <code>null</code>.
	 */
	public StartupPhase getNextPhase() {
		return nextPhase;
	}

}
