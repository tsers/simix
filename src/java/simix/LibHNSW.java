package simix;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibHNSW {

  public static native int hnsw_get_max_id_value(Pointer output);

  public static native Pointer hnsw_create_index(int spaceType, int dim, int maxElems, int M, int efConstruction, int random_seed);

  public static native Pointer hnsw_load_index(String indexPath, int spaceType, int dim, int maxElems);

  public static native void hnsw_save_index(Pointer index, String indexPath);

  public static native void hnsw_release_index(Pointer index);

  public static native void hnsw_set_query_ef(Pointer index, int ef);

  public static native void hnsw_add_item(Pointer index, long id, Pointer data);

  public static native void hnsw_knn_query(Pointer index, Pointer x, int k, Pointer distances, Pointer ids);

  static {
    String osName = System.getProperty("os.name", "Unknown");
    switch (osName) {
      case "Mac OS X":
        Native.register("libhnsw_osx.dylib");
        break;
      case "Linux":
        Native.register("libhnsw_linux.so");
        break;
      default:
        throw new LinkageError("Non-supported OS: " + osName);
    }
  }

}
