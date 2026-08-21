package com.example.serverinventory.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    /**
     * 저장 후 상대 경로(파일명)를 반환한다.
     */
    String store(MultipartFile file);

    Resource loadAsResource(String filename);
}
