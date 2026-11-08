package com.egon.dto;

import java.util.UUID;

import com.egon.model.Admin;

public record AdminResponse(UUID id, String nome, String login) {

    public static AdminResponse de(Admin admin) {
        return new AdminResponse(admin.getId(), admin.getNome(), admin.getLogin());
    }

}
