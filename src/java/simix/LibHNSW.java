package simix;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;

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
        loadLib("libhnsw_osx.dylib");
        break;
      case "Linux":
        loadLib("libhnsw_linux.so");
        break;
      default:
        throw new LinkageError("Non-supported OS: " + osName);
    }
  }

  private static void loadLib(String resourceName) {
    InputStream is = null;
    try {
      URL res = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      if (res == null) {
        throw new IOException("Library file not found: " + resourceName);
      }
      if ("file".equals(res.getProtocol())) {
        String filePath = res.getFile()
                             .replace('/', File.separatorChar)
                             .replace("+", URLEncoder.encode("+", "UTF-8"));
        is = new FileInputStream(URLDecoder.decode(filePath, "UTF-8"));
      } else {
        is = res.openStream();
      }
      File tempFile = Files.createTempFile("libhnsw", "." + resourceName.split("\\.")[1]).toFile();
      tempFile.deleteOnExit();
      IOUtils.copy(is, new FileOutputStream(tempFile));
      Native.register(tempFile.getAbsolutePath());
    } catch (Exception e) {
      throw new RuntimeException("Library loading failed", e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

}
