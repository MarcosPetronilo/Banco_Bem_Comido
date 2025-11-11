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
			boolean noUsers = repo.count() == 0;
			boolean noAdmin = repo.findAll().stream().noneMatch(u -> u.getRole() == Role.ADMIN);
			if (noUsers || noAdmin) {
				// Inicializa um administrador padrão usando variáveis de ambiente ou defaults seguros
				String email = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@bemcomido.local");
				String password = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin#12345");
				String username = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");
				String pais = System.getenv().getOrDefault("ADMIN_COUNTRY", "Brasil");
				String birth = System.getenv().getOrDefault("ADMIN_BIRTHDATE", "1990-01-01");
				java.time.LocalDate dob;
				try { dob = java.time.LocalDate.parse(birth); } catch (Exception e) { dob = java.time.LocalDate.of(1990,1,1); }
				boolean existsByEmail = repo.findByEmail(email).isPresent();
				boolean existsByUsername = repo.findByUsername(username).isPresent();
				if (!existsByEmail && !existsByUsername) {
					Usuario admin = usuarioService.registrarAdministrador(username, password, email, dob, pais, "");
					admin.setRole(Role.ADMIN);
					repo.save(admin);
					System.out.println("[bootstrapAdmin] Admin inicial criado: username=" + username + ", email=" + email);
				} else {
					System.out.println("[bootstrapAdmin] Admin já existente (username/email). Nenhuma ação.");
				}
			}

			// Promover usuários adicionais a ADMIN via variável de ambiente ou valor fixo solicitado
			// EX: EXTRA_ADMIN_USERNAMES="user1;user2;Marcos Vinicius"
			String extraAdminsEnv = System.getenv().getOrDefault("EXTRA_ADMIN_USERNAMES", "Marcos Vinicius");
			java.util.Arrays.stream(extraAdminsEnv.split("[;,]"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.forEach(name -> {
					repo.findByUsername(name).ifPresent(u -> {
						if (u.getRole() != Role.ADMIN) {
							u.setRole(Role.ADMIN);
							repo.save(u);
							System.out.println("[bootstrapAdmin] Promovido a ADMIN: username=" + name);
						}
					});
				});
		};
	}

	@Bean
	public org.springframework.boot.CommandLineRunner patchReceitasTable(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			// Ajustes de esquema para receitas após remoção de campos do backend
			try {
				jdbcTemplate.execute("ALTER TABLE receitas MODIFY COLUMN foto_url VARCHAR(255) NULL");
				System.out.println("[schemaPatch] Ajustado receitas.foto_url para NULL");
			} catch (Exception e) {
				System.out.println("[schemaPatch] Ignorando ajuste de foto_url: " + e.getMessage());
			}

			try {
				jdbcTemplate.execute("ALTER TABLE receitas DROP COLUMN tags");
				System.out.println("[schemaPatch] Coluna receitas.tags removida");
			} catch (Exception e) {
				System.out.println("[schemaPatch] Ignorando remoção de tags: " + e.getMessage());
			}
		};
	}
}