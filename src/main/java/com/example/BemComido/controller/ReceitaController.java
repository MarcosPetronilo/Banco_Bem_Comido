package com.example.BemComido.controller;

import com.example.BemComido.dto.ReceitaCreateDTO;
import com.example.BemComido.model.Receita;
import com.example.BemComido.service.ReceitaService;
import com.example.BemComido.service.FavoritosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/recipes")
@Validated
public class ReceitaController {

    private final ReceitaService receitaService;
    private final FavoritosService favoritosService;

    public ReceitaController(ReceitaService receitaService, FavoritosService favoritosService) {
        this.receitaService = receitaService;
        this.favoritosService = favoritosService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> criarReceita(@Valid @RequestBody ReceitaCreateDTO req, Authentication auth) {
        try {
            Receita criada = receitaService.criarReceita(req, auth);
            return ResponseEntity.status(201).body(criada);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Requisição inválida";
            if (msg.toLowerCase().contains("autentic")) {
                return ResponseEntity.status(401).body(Map.of("error", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao criar receita"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> buscar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "ingredientes", required = false) String ingredientesCsv,
            @RequestParam(value = "somente", required = false, defaultValue = "false") boolean somente,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(size, 100));
        java.util.List<String> ingredientes = ingredientesCsv == null || ingredientesCsv.isBlank() ? java.util.List.of() : java.util.Arrays.stream(ingredientesCsv.split("[;,]")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Page<Receita> resultado = receitaService.buscar(nome, ingredientes, somente, pageable);
        return ResponseEntity.ok(resultado);
    }

    public static class RateRequest { public int score; public String comentario; }

    @PostMapping("/{id}/rate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> avaliar(@PathVariable("id") Long id, @RequestBody RateRequest body, Authentication auth) {
        try {
            var saved = receitaService.avaliarReceita(id, body.score, body.comentario, auth);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Requisição inválida";
            if (msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @GetMapping("/top")
    public ResponseEntity<?> top(
            @RequestParam(value = "period", defaultValue = "all") String period,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(size, 100));
        var result = receitaService.topReceitas(period, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> minhasReceitas(Authentication auth,
                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(size, 100));
        try {
            Page<Receita> minhas = favoritosService.listarMinhasReceitas(auth, pageable);
            return ResponseEntity.ok(minhas);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
}
