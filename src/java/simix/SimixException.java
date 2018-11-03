package simix;

public class SimixException extends RuntimeException {
  public SimixException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
