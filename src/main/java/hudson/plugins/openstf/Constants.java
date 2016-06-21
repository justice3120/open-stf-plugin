package hudson.plugins.openstf;

public interface Constants {

  static final String REGEX_VARIABLE = "\\$([A-Za-z0-9_]+|\\{[A-Za-z0-9_]+\\}|\\$)";
  static final String REGEX_OS_VERSION = "^([0-9]+\\.){0,2}[0-9]+$";
}
