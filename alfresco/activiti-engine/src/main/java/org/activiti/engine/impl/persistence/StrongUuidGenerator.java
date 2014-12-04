package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.cfg.IdGenerator;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

// TODO: Auto-generated Javadoc
/**
 * The Class StrongUuidGenerator.
 *
 * {@link IdGenerator} implementation based on the current time and the ethernet
 * address of the machine it is running on.
 * @author Daniel Meyer
 */
public class StrongUuidGenerator implements IdGenerator {

  // different ProcessEngines on the same classloader share one generator.
  /** The time based generator. */
  protected static TimeBasedGenerator timeBasedGenerator;

  /**
   * Instantiates a new strong uuid generator.
   */
  public StrongUuidGenerator() {
    ensureGeneratorInitialized();
  }

  /**
   * Ensure generator initialized.
   */
  protected void ensureGeneratorInitialized() {
    if (timeBasedGenerator == null) {
      synchronized (StrongUuidGenerator.class) {
        if (timeBasedGenerator == null) {
          timeBasedGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.cfg.IdGenerator#getNextId()
   */
  public String getNextId() {
    return timeBasedGenerator.generate().toString();
  }

}
