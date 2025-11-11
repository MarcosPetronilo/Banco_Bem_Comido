package com.example.BemComido.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BemComido.model.Usuario;
import com.example.BemComido.service.UsuarioService;
import com.example.BemComido.service.FavoritosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.BemComido.model.Receita;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/users")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FavoritosService favoritosService;

    public UsuarioController(UsuarioService usuarioService, FavoritosService favoritosService) {
        this.usuarioService = usuarioService;
        this.favoritosService = favoritosService;
    }

    // Buscar usuário por username (match exato)
    @GetMapping("/{username}")
    public ResponseEntity<?> getByUsername(@PathVariable String username) {
        Optional<Usuario> user = usuarioService.buscarPorUsername(username);
        return user.<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado")));
    }

    // Buscar usuários por fragmento de username (case-insensitive)
    @GetMapping("/search")
    public ResponseEntity<List<Usuario>> searchByUsername(@RequestParam(name = "username") String fragment) {
        List<Usuario> results = usuarioService.buscarPorUsernameParcial(fragment);
        return ResponseEntity.ok(results);
    }

    // Excluir a própria conta (somente autenticado)
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMyAccount(Authentication auth) {
        try {
            boolean ok = usuarioService.deletarMinhaConta(auth);
            return ok ? ResponseEntity.noContent().build() : ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    // Excluir qualquer usuário por ID (somente ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id,
                                            @RequestParam(value = "justificativa", required = false) String justificativa,
                                            Authentication auth) {
        try {
            boolean ok = usuarioService.deletarPorAdmin(id, justificativa, auth);
            return ok ? ResponseEntity.noContent().build() : ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.toLowerCase().contains("administr")) return ResponseEntity.status(403).body(Map.of("error", msg));
            if (msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (DataIntegrityViolationException dive) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Não é possível excluir: existem registros vinculados (receitas, favoritos, etc.)",
                    "detail", dive.getMostSpecificCause() != null ? dive.getMostSpecificCause().getMessage() : dive.getMessage()
            ));
        }
    }

    // Excluir usuário por username (somente ADMIN)
    @DeleteMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username,
                                                   @RequestParam(value = "justificativa", required = false) String justificativa,
                                                   Authentication auth) {
        Optional<Usuario> u = usuarioService.buscarPorUsername(username);
        if (u.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        }
        try {
            boolean ok = usuarioService.deletarPorAdmin(u.get().getId(), justificativa, auth);
            return ok ? ResponseEntity.noContent().build() : ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.toLowerCase().contains("administr")) return ResponseEntity.status(403).body(Map.of("error", msg));
            if (msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (DataIntegrityViolationException dive) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Não é possível excluir: existem registros vinculados (receitas, favoritos, etc.)",
                    "detail", dive.getMostSpecificCause() != null ? dive.getMostSpecificCause().getMessage() : dive.getMessage()
            ));
        }
    }

    // Listar receitas de um usuário específico (público)
    @GetMapping("/{username}/recipes")
    public ResponseEntity<Page<Receita>> listarReceitasPorUsuario(@PathVariable String username,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(size,100));
        Page<Receita> receitas = favoritosService.listarReceitasPorUsuario(username, pageable);
        return ResponseEntity.ok(receitas);
    }
}
