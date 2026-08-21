package com.example.serverinventory.exception;

public class ServerNotFoundException extends RuntimeException {

    public ServerNotFoundException(Long id) {
        super("서버를 찾을 수 없습니다. id=" + id);
    }
}
