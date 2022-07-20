package io.micronaut.starter.cli.feature.linweiyu.entity.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to reader classpath resources.
 */
public class ResourceReader {

    private ResourceReader() {
    }

    public static InputStream getResourceAsStream(String path) throws IOException {
        InputStream classPathResource = ResourceReader.class.getClassLoader().getResourceAsStream(path);
        if (classPathResource != null) {
            return classPathResource;
        }
        InputStream fileResource = new FileInputStream(new File(path));
        return fileResource;
    }

    public static InputStream getResourceFromRelativePathAsStream(String relativePath) {
        return FileUtil.getInputStream(relativePath);
    }

}
