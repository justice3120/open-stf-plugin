package hudson.plugins.openstf.exception;

import hudson.plugins.openstf.STFException;

public final class NoDeviceAvailableException extends STFException {

  public NoDeviceAvailableException(String message) {
    super(message);
  }

  private static final long serialVersionUID = 1L;
}
