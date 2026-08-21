package com.example.serverinventory.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService newStorageService() {
        StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());
        return new FileSystemStorageService(properties);
    }

    @Test
    void storeShouldSaveFileUnderRootLocationWithUniquePrefix() {
        StorageService storageService = newStorageService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "spec.txt", "text/plain", "cpu=8core".getBytes(StandardCharsets.UTF_8));

        String storedFilename = storageService.store(file);

        assertThat(storedFilename).endsWith("-spec.txt");
        assertThat(tempDir.resolve(storedFilename)).exists();
    }

    @Test
    void storeShouldRejectEmptyFile() {
        StorageService storageService = newStorageService();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> storageService.store(emptyFile))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void loadAsResourceShouldReturnStoredFileContent() throws IOException {
        StorageService storageService = newStorageService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "spec.txt", "text/plain", "cpu=8core".getBytes(StandardCharsets.UTF_8));
        String storedFilename = storageService.store(file);

        Resource resource = storageService.loadAsResource(storedFilename);

        assertThat(resource.getContentAsString(StandardCharsets.UTF_8)).isEqualTo("cpu=8core");
    }

    @Test
    void loadAsResourceShouldThrowWhenFileMissing() {
        StorageService storageService = newStorageService();

        assertThatThrownBy(() -> storageService.loadAsResource("does-not-exist.txt"))
                .isInstanceOf(StorageFileNotFoundException.class);
    }
}
