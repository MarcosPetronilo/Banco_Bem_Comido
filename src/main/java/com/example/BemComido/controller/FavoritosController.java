package com.example.BemComido.controller;

import com.example.BemComido.model.Receita;
import com.example.BemComido.model.Usuario;
import com.example.BemComido.service.FavoritosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoritosController {

    private final FavoritosService favoritosService;

    public FavoritosController(FavoritosService favoritosService) {
        this.favoritosService = favoritosService;
    }

    private Pageable page(int page, int size) {
        return PageRequest.of(Math.max(page,0), Math.min(size,100));
    }

    // Usuários favoritos
    @PostMapping("/users/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> favoritarUsuario(@PathVariable String username, Authentication auth) {
        try {
            var fav = favoritosService.favoritarUsuario(username, auth);
            return ResponseEntity.status(201).body(fav);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @DeleteMapping("/users/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> desfavoritarUsuario(@PathVariable String username, Authentication auth) {
        try {
            favoritosService.desfavoritarUsuario(username, auth);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listarUsuariosFavoritos(Authentication auth,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            Page<Usuario> favoritos = favoritosService.listarUsuariosFavoritos(auth, page(page,size));
            return ResponseEntity.ok(favoritos);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    // Receitas favoritas
    @PostMapping("/recipes/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> favoritarReceita(@PathVariable("id") Long id, Authentication auth) {
        try {
            var fav = favoritosService.favoritarReceita(id, auth);
            return ResponseEntity.status(201).body(fav);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @DeleteMapping("/recipes/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> desfavoritarReceita(@PathVariable("id") Long id, Authentication auth) {
        try {
            favoritosService.desfavoritarReceita(id, auth);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @GetMapping("/recipes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listarReceitasFavoritas(Authentication auth,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            Page<Receita> favoritos = favoritosService.listarReceitasFavoritas(auth, page(page,size));
            return ResponseEntity.ok(favoritos);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    // Listar receitas de um usuário (público)
    @GetMapping("/recipes/by-user/{username}")
    public ResponseEntity<?> listarReceitasPorUsuario(@PathVariable String username,
                                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Receita> receitas = favoritosService.listarReceitasPorUsuario(username, page(page,size));
        return ResponseEntity.ok(receitas);
    }

    // Listar minhas receitas (autenticado)
    @GetMapping("/recipes/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listarMinhasReceitas(Authentication auth,
                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            Page<Receita> receitas = favoritosService.listarMinhasReceitas(auth, page(page,size));
            return ResponseEntity.ok(receitas);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("autentic")) return ResponseEntity.status(401).body(Map.of("error", msg));
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
}
