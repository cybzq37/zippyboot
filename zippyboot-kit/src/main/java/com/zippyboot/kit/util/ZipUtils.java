package com.zippyboot.kit.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ZipUtils {

    private static final String MAC_OSX_PREFIX = "__MACOSX";
    private static final String FILE_MATCH_MODE = "file";
    private static final Charset ZIP_CHARSET = Charset.forName("GBK");

    public static void unzip(File zipFile, String descDir) throws IOException {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            unzip(descDir, inputStream);
        }
    }


    public static void unzip(InputStream zipFile, String descDir) {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            unzip(descDir, inputStream);
            log.info("******************解压完毕********************");
        } catch (Exception e) {
            log.error("[unzip] 解压zip文件出错", e);
            throw new RuntimeException(e);
        }
    }

    private static void unzip(String descDir, ZipArchiveInputStream inputStream) throws IOException {
        Path targetDir = Paths.get(descDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        ZipArchiveEntry entry;
        while ((entry = inputStream.getNextZipEntry()) != null) {
            String entryName = entry.getName();
            if (entryName == null || entryName.toLowerCase(Locale.ROOT).startsWith(MAC_OSX_PREFIX)) {
                continue;
            }

            Path targetPath = targetDir.resolve(entryName).normalize();
            if (!targetPath.startsWith(targetDir)) {
                throw new IOException("Zip entry is outside of target dir: " + entryName);
            }

            if (entry.isDirectory()) {
                Files.createDirectories(targetPath);
                continue;
            }

            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(targetPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE))) {
                IOUtils.copy(inputStream, os);
            }
        }
    }

    public static String getFileList(String path, String fileLastFix, String name) {
        File file = new File(path);

        if (!file.exists() || !file.isDirectory()) {
            return null;
        }
        String basePath = path.endsWith(File.separator) ? path : path + File.separator;
        String[] fixList = fileLastFix.split(";");
        String[] files = file.list();
        if (files == null) {
            return null;
        }

        for (String fileName : files) {
            File tfile = new File(basePath + fileName);
            if (tfile.isDirectory()) {
                String found = getFileList(tfile.getAbsolutePath(), fileLastFix, name);
                if (found != null) {
                    return found;
                }
            } else if (tfile.isFile()) {
                int dotIndex = fileName.lastIndexOf('.');
                String strFix = dotIndex >= 0 ? fileName.substring(dotIndex) : "";
                if (FILE_MATCH_MODE.equalsIgnoreCase(name)) {
                    if (exists(fileName, fixList)) {
                        return tfile.getAbsolutePath();
                    }
                } else {
                    if (exists(strFix, fixList)) {
                        return tfile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    public static boolean exists(String sourcee, String[] strList) {
        if (sourcee == null) {
            return false;
        }
        if (strList == null || strList.length == 0) {
            return false;
        }
        for (String str : strList) {
            if (str != null && str.equalsIgnoreCase(sourcee)) {
                return true;
            }
        }
        return false;
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws IOException {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)), ZIP_CHARSET.name());
    }

    private static ZipArchiveInputStream getZipFile(InputStream zipFile) {
        return new ZipArchiveInputStream(new BufferedInputStream(zipFile), ZIP_CHARSET.name());
    }

    /**
     * 压缩文件
     * @param sourceFolderPath 压缩目录获文件
     * @param zipFilePath 压缩包路径
     */
    public static void zipFolder(String sourceFolderPath, String zipFilePath) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
            File sourceFolder = new File(sourceFolderPath);
            addFolderToZip(sourceFolder, null, zos);
            zos.flush();
        } catch (IOException e) {
            log.error("zip folder error, source={}, target={}", sourceFolderPath, zipFilePath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated use zipFolder instead.
     */
    @Deprecated
    public static void ZipFolderExample(String sourceFolderPath, String zipFilePath) {
        zipFolder(sourceFolderPath, zipFilePath);
    }

    private static void addFolderToZip(File folder, String parentFolderName, ZipArchiveOutputStream zos) throws IOException {
        File[] files;
        if (folder.isFile()) {
             files = new File[]{new File(folder.getPath())};
        } else {
            files = folder.listFiles();
        }
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            String entryName = (isNotBlank(parentFolderName) ? parentFolderName + File.separator : "") + file.getName();
            if (file.isDirectory()) {
                addFolderToZip(file, entryName, zos);
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(entryName);
                zos.putArchiveEntry(zipEntry);
                IOUtils.copy(fis, zos);
                zos.closeArchiveEntry();
            }
        }
    }


    /**
     * 获取压缩包中的文件
     *
     * @param input
     * @return
     */
    public static List<String> checkZip(InputStream input) {
        if (input == null) {
            return Collections.emptyList();
        }

        List<String> fileList = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), ZIP_CHARSET)) {
            //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
            ZipEntry zipFile;
            while ((zipFile = zipInputStream.getNextEntry()) != null) {
                if (!zipFile.getName().toLowerCase(Locale.ROOT).startsWith(MAC_OSX_PREFIX)) {
                    if (!zipFile.isDirectory()) {
                        // 文件名称
                        String fileName = zipFile.getName();
                        long fileSize = zipFile.getSize();
                        // 最后修改时间
                        FileTime time = zipFile.getLastModifiedTime();
                        log.info("文件名称" + fileName + " 文件大小" + fileSize + " 最后修改时间" + time);
                        fileList.add(FilenameUtils.getExtension(fileName));
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception e) {
            log.error("check zip error", e);
        }
        return fileList;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
