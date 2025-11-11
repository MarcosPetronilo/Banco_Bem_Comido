package com.example.BemComido.controller;

import com.example.BemComido.service.ReceitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
@RequestMapping("/admin/recipes")
public class AdminReceitaController {

    private final ReceitaService receitaService;

    public AdminReceitaController(ReceitaService receitaService) {
        this.receitaService = receitaService;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarPorAdmin(
            @PathVariable("id") Long id,
            @RequestParam(value = "justificativa", required = false) String justificativa,
            Authentication auth) {
        try {
            receitaService.deletarReceitaPorAdmin(id, justificativa, auth);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Requisição inválida";
            if (msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            if (msg.toLowerCase().contains("apenas administradores")) return ResponseEntity.status(403).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> relatorio(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Instant fromInst = from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInst = to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var report = receitaService.relatorioReceitas(fromInst, toInst);
        return ResponseEntity.ok(report);
    }
}
