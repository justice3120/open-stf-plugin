package hudson.plugins.openstf.exception;

import hudson.plugins.openstf.STFException;

public final class ApiFailedException extends STFException {

  public ApiFailedException(String message) {
    super(message);
  }

  private static final long serialVersionUID = 1L;
}
