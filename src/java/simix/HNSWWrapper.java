package simix;

import clojure.lang.IFn;
import com.sun.jna.Pointer;

import java.io.Closeable;

public class HNSWWrapper implements Closeable {
  private Pointer ptr;
  private int nUsers;
  private boolean closing;

  public HNSWWrapper(Pointer ptr) {
    this.ptr = ptr;
    this.nUsers = 0;
    this.closing = false;
  }

  public Object use(IFn f) {
    this.stepIn();
    try {
      return f.invoke(this.ptr);
    } finally {
      this.stepOut();
    }
  }

  public void finalize() {
    this.close();
  }

  public void close() {
    try {
      synchronized (this) {
        this.closing = true;
        if (this.nUsers > 0) {
          this.wait();
        }
        if (this.ptr != null) {
          LibHNSW.hnsw_release_index(this.ptr);
          this.ptr = null;
        }
        this.closing = false;
        this.notify();
      }
    } catch (InterruptedException ex) {
      throw new SimixException("Thread was interrupted during close", ex);
    }
  }

  private void stepIn() {
    synchronized (this) {
      if (this.ptr == null) {
        throw new SimixException("Index is closed", null);
      }
      this.nUsers++;
    }
  }

  private void stepOut() {
    synchronized (this) {
      if (--this.nUsers == 0 && this.closing) {
        this.notify();
      }
    }
  }
}
