package com.example.BemComido.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome (username) obrigatório e único
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    @Column(unique = true, nullable = false)
    private String email;

    // Telefone opcional agora
    // Permite vazio ou número com opcional + e entre 10 e 15 dígitos
    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Telefone deve ser vazio ou conter apenas dígitos (10-15) e opcional + no início")
    @Column(length = 20)
    private String telefone;

    @Past(message = "Data de nascimento deve estar no passado")
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @NotBlank(message = "País é obrigatório")
    @Column(name = "pais", length = 80)
    private String pais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER; // padrão

    // Token de redefinição (armazenado como hash determinístico - SHA-256)
    @Column(name = "reset_password_token", length = 64)
    private String resetPasswordToken;

    // Expiração do token de redefinição
    @Column(name = "reset_password_token_expires")
    private LocalDateTime resetPasswordTokenExpires;

    public Usuario() {
    }

    public Usuario(Long id, String username, String password, String email, String telefone, LocalDate dataNascimento, String pais, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.pais = pais;
        this.role = role == null ? Role.USER : role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() { return telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getPais() { return pais; }
    public Role getRole() { return role; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public LocalDateTime getResetPasswordTokenExpires() { return resetPasswordTokenExpires; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setPais(String pais) { this.pais = pais; }
    public void setRole(Role role) { this.role = role; }

    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }
    public void setResetPasswordTokenExpires(LocalDateTime resetPasswordTokenExpires) { this.resetPasswordTokenExpires = resetPasswordTokenExpires; }

}   
