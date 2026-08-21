package com.example.serverinventory.exception;

public class SelfRoleChangeException extends RuntimeException {

    public SelfRoleChangeException() {
        super("자기 자신의 권한은 변경할 수 없습니다.");
    }
}
