package hudson.plugins.openstf;

public abstract class STFException extends Exception {

  protected STFException(String message) {
    super(message);
  }

  protected STFException(String message, Throwable cause) {
    super(message, cause);
  }

  private static final long serialVersionUID = 1L;

}
