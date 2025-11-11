package com.example.BemComido.controller;

import com.example.BemComido.model.MovimentacaoTipo;
import com.example.BemComido.model.UserMovimentacao;
import com.example.BemComido.model.UserMovimentacaoTipo;
import com.example.BemComido.repository.UserMovimentacaoRepository;
import com.example.BemComido.repository.UsuarioRepository;
import com.example.BemComido.repository.ReceitaRepository;
import com.example.BemComido.repository.ReceitaExcluidaRepository;
import com.example.BemComido.repository.ReceitaMovimentacaoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final ReceitaExcluidaRepository receitaExcluidaRepository;
    private final ReceitaMovimentacaoRepository receitaMovimentacaoRepository;
    private final UserMovimentacaoRepository userMovimentacaoRepository;

    public AdminAuditController(UsuarioRepository usuarioRepository,
                                ReceitaRepository receitaRepository,
                                ReceitaExcluidaRepository receitaExcluidaRepository,
                                ReceitaMovimentacaoRepository receitaMovimentacaoRepository,
                                UserMovimentacaoRepository userMovimentacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.receitaExcluidaRepository = receitaExcluidaRepository;
        this.receitaMovimentacaoRepository = receitaMovimentacaoRepository;
        this.userMovimentacaoRepository = userMovimentacaoRepository;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(
            @RequestParam(value = "from", required = false) String fromIso,
            @RequestParam(value = "to", required = false) String toIso
    ) {
        Instant from = null;
        Instant to = null;
        try { if (fromIso != null && !fromIso.isBlank()) from = Instant.parse(fromIso); } catch (Exception ignored) {}
        try { if (toIso != null && !toIso.isBlank()) to = Instant.parse(toIso); } catch (Exception ignored) {}

        long totalUsuarios = usuarioRepository.count();
        long totalReceitas = receitaRepository.count();
        long totalReceitasExcluidas = receitaExcluidaRepository.countBy();

        var movsReceitas = receitaMovimentacaoRepository.findMovimentacoes(from, to);
        long receitasCriadasPeriodo = movsReceitas.stream().filter(m -> m.getTipo()== MovimentacaoTipo.CREATED).count();
        long receitasExcluidasPeriodo = movsReceitas.stream().filter(m -> m.getTipo()== MovimentacaoTipo.DELETED).count();

        var movsUsuarios = userMovimentacaoRepository.findMovimentacoes(from, to);
        long usuariosCriadosPeriodo = movsUsuarios.stream().filter(m -> m.getTipo()== UserMovimentacaoTipo.CREATED).count();
        long usuariosExcluidosPeriodo = movsUsuarios.stream().filter(m -> m.getTipo()== UserMovimentacaoTipo.DELETED).count();
        long usuariosPromovidosPeriodo = movsUsuarios.stream().filter(m -> m.getTipo()== UserMovimentacaoTipo.PROMOTED).count();

        List<UserMovimentacao> ultimosUsuarios = movsUsuarios.stream()
                .sorted(Comparator.comparing(UserMovimentacao::getMomento).reversed())
                .limit(200)
                .toList();

        var ultimasReceitas = movsReceitas.stream()
                .sorted(Comparator.comparing(com.example.BemComido.model.ReceitaMovimentacao::getMomento).reversed())
                .limit(200)
                .toList();

        var excluidasRecentes = receitaExcluidaRepository.findAll().stream()
                .sorted(Comparator.comparing(com.example.BemComido.model.deleted.ReceitaExcluida::getRemovidoEm).reversed())
                .limit(200)
                .toList();

        return Map.of(
                "periodo", Map.of("from", from, "to", to),
                "totais", Map.of(
                        "usuarios", totalUsuarios,
                        "receitas", totalReceitas,
                        "receitasExcluidas", totalReceitasExcluidas
                ),
                "movimentosPeriodo", Map.of(
                        "receitas", Map.of(
                                "criadas", receitasCriadasPeriodo,
                                "excluidas", receitasExcluidasPeriodo
                        ),
                        "usuarios", Map.of(
                                "criados", usuariosCriadosPeriodo,
                                "excluidos", usuariosExcluidosPeriodo,
                                "promovidos", usuariosPromovidosPeriodo
                        )
                ),
                "ultimasMovimentacoes", Map.of(
                        "usuarios", ultimosUsuarios,
                        "receitas", ultimasReceitas,
                        "receitasExcluidas", excluidasRecentes
                )
        );
    }
}
