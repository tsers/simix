package simix;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.IOException;

public class LibHNSW {

  public static native Pointer hnsw_create_index(int spaceType, int dim, int maxElems, int M, int efConstruction, int random_seed);

  public static native Pointer hnsw_load_index(String indexPath, int spaceType, int dim, int maxElems);

  public static native void hnsw_save_index(Pointer index, String indexPath);

  public static native void hnsw_release_index(Pointer index);

  public static native void hnsw_set_query_ef(Pointer index, int ef);

  public static native void hnsw_add_item(Pointer index, int id, Pointer data);

  public static native void hnsw_knn_query(Pointer index, Pointer x, int k, Pointer distances, Pointer ids);

  static {
    String osName = System.getProperty("os.name", "Unknown");
    switch (osName) {
      case "Mac OS X":
        loadNativeLib("libhnsw_osx.dylib");
        break;
      default:
        throw new LinkageError("Non-supported OS: " + osName);
    }
  }

  private static void loadNativeLib(String resourceName) {
    try {
      File lib = Native.extractFromResourcePath(resourceName);
      Native.register(lib.getAbsolutePath());
    } catch (IOException e) {
      throw new LinkageError("Can't load native Faiss library " + resourceName, e);
    }
  }
}
