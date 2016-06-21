package hudson.plugins.openstf.exception;

import hudson.plugins.openstf.STFException;

public final class WaitDeviceReleaseTimeoutException extends STFException {

  public WaitDeviceReleaseTimeoutException(String message) {
    super(message);
  }

  private static final long serialVersionUID = 1L;
}
