package hudson.plugins.openstf.exception;

import hudson.plugins.openstf.STFException;

public final class WaitDeviceReleaseInterruptedException extends STFException {

  public WaitDeviceReleaseInterruptedException(String message) {
    super(message);
  }

  public WaitDeviceReleaseInterruptedException(String message, Throwable cause) {
    super(message, cause);
  }

  private static final long serialVersionUID = 1L;
}
