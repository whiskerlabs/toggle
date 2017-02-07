package com.whiskerlabs.toggle.json;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePath {
  private ResourcePath () { /* singleton */ }

  /**
   * Returns the {@link Path} of a resource with a given name.
   */
  public static Path get(String name) {
    try {
      final URL resourceUrl = ResourcePath.class.getClassLoader().getResource(name);

      if (resourceUrl == null) {
        throw new IllegalArgumentException("No resource exists for name " + name);
      } else {
        return Paths.get(resourceUrl.toURI());
      }
    } catch (URISyntaxException err) {
      throw new RuntimeException(err);
    }
  }
}
