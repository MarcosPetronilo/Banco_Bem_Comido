package com.example.BemComido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.BemComido.model.Role;
import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.UsuarioRepository;
import com.example.BemComido.service.UsuarioService;

@SpringBootApplication
public class BemComidoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BemComidoBackendApplication.class, args);
	}

	@Bean
	public org.springframework.boot.CommandLineRunner bootstrapAdmin(UsuarioRepository repo, UsuarioService usuarioService) {
		return args -> {
			if (repo.count() == 0) {
				// Inicializa um administrador padrão usando variáveis de ambiente ou defaults seguros
				String email = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@bemcomido.local");
				String password = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin#12345");
				String nome = System.getenv().getOrDefault("ADMIN_NAME", "Administrador");
				String pais = System.getenv().getOrDefault("ADMIN_COUNTRY", "Brasil");
				String birth = System.getenv().getOrDefault("ADMIN_BIRTHDATE", "1990-01-01");
				java.time.LocalDate dob;
				try { dob = java.time.LocalDate.parse(birth); } catch (Exception e) { dob = java.time.LocalDate.of(1990,1,1); }
				if (repo.findByEmail(email).isEmpty()) {
					Usuario admin = usuarioService.registrarAdministrador(nome, password, email, dob, pais, "");
					admin.setRole(Role.ADMIN);
					repo.save(admin);
					System.out.println("Administrador inicial criado: " + email);
				}
			}
		};
	}
}