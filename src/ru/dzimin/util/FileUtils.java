package ru.dzimin.util;

import java.util.Optional;

public final class FileUtils {

    public static String getExtensionByFileName(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1))
                .orElseThrow(() -> new RuntimeException("Файл не содержит расширения!"));
    }

    private FileUtils() {
    }
}