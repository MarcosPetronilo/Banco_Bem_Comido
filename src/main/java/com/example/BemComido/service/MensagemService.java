package com.example.BemComido.service;

import org.springframework.stereotype.Service;

import com.example.BemComido.repository.MensagemRepository;
@Service
public class MensagemService {
    
    private final MensagemRepository mensagemRepository;

    public MensagemService(MensagemRepository mensagemRepository) {
        this.mensagemRepository = mensagemRepository;
    }


    public String obterMensagem() {
        return mensagemRepository.getMensagem();
    }
}
