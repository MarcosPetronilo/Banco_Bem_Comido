package com.example.BemComido.repository;

import org.springframework.stereotype.Repository;

@Repository
public class MensagemRepository {

    public String getMensagem() {
        return "Hello from MensagemRepository!";
    }
}
