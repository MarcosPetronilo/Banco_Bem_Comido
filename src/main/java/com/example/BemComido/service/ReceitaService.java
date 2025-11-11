package com.example.BemComido.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BemComido.dto.ReceitaCreateDTO;
import com.example.BemComido.dto.ReceitaRankingDTO;
import com.example.BemComido.model.AvaliacaoReceita;
import com.example.BemComido.model.MovimentacaoTipo;
import com.example.BemComido.model.ReceitaMovimentacao;
import com.example.BemComido.model.deleted.ReceitaExcluida;
import com.example.BemComido.model.InformacoesNutricionais;
import com.example.BemComido.model.NivelTecnica;
import com.example.BemComido.model.Receita;
import com.example.BemComido.model.Usuario;
import com.example.BemComido.repository.ReceitaRepository;
import com.example.BemComido.repository.UsuarioRepository;
import com.example.BemComido.repository.AvaliacaoReceitaRepository;
import com.example.BemComido.repository.ReceitaMovimentacaoRepository;
import com.example.BemComido.repository.ReceitaExcluidaRepository;
import com.example.BemComido.spec.ReceitaSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


@Service
public class ReceitaService {

    private final ReceitaRepository receitaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AvaliacaoReceitaRepository avaliacaoRepository;
    private final ReceitaMovimentacaoRepository movimentacaoRepository;
    private final ReceitaExcluidaRepository receitaExcluidaRepository;

    public ReceitaService(ReceitaRepository receitaRepository,
                          UsuarioRepository usuarioRepository,
                          AvaliacaoReceitaRepository avaliacaoRepository,
                          ReceitaMovimentacaoRepository movimentacaoRepository,
                          ReceitaExcluidaRepository receitaExcluidaRepository) {
        this.receitaRepository = receitaRepository;
        this.usuarioRepository = usuarioRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.receitaExcluidaRepository = receitaExcluidaRepository;
    }

    @Transactional
    public Receita criarReceita(Usuario autor,
                                String nome,
                                String ingredientes,
                                String modoPreparo,
                                Integer tempoMedioMin,
                                String rendimento,
                                InformacoesNutricionais info,
                                NivelTecnica nivel) {

        if (autor == null) throw new IllegalArgumentException("Autor é obrigatório");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (ingredientes == null || ingredientes.isBlank()) throw new IllegalArgumentException("Ingredientes são obrigatórios");
        if (modoPreparo == null || modoPreparo.isBlank()) throw new IllegalArgumentException("Modo de preparo é obrigatório");
        if (tempoMedioMin == null || tempoMedioMin <= 0) throw new IllegalArgumentException("Tempo médio deve ser positivo");
        if (rendimento == null || rendimento.isBlank()) throw new IllegalArgumentException("Rendimento é obrigatório");
        if (nivel == null) throw new IllegalArgumentException("Nível de técnica é obrigatório");

        // Informações nutricionais são opcionais; frontend gerencia tags/fitness

        Receita r = new Receita();
        r.setAutor(autor);
        r.setNome(nome.trim());
        r.setIngredientes(ingredientes.trim());
        r.setModoPreparo(modoPreparo.trim());
        r.setTempoMedioMinutos(tempoMedioMin);
        r.setRendimento(rendimento.trim());
        r.setInfoNutricional(info);
        r.setNivelTecnica(nivel);
    Receita salvo = receitaRepository.save(r);
    movimentacaoRepository.save(new ReceitaMovimentacao(MovimentacaoTipo.CREATED, salvo.getId(), salvo.getNome(),
        salvo.getAutor() != null ? salvo.getAutor().getId() : null,
        salvo.getAutor() != null ? salvo.getAutor().getUsername() : null,
        null));
    return salvo;
    }

    @Transactional
    public Receita criarReceita(ReceitaCreateDTO dto, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Não autenticado");
        }
        String subject = auth.getName();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Token inválido: subject vazio");
        }

        Usuario autor = usuarioRepository.findByUsername(subject)
                .orElseGet(() -> usuarioRepository.findByEmail(subject).orElse(null));
        if (autor == null) {
            throw new IllegalArgumentException("Usuário autenticado não encontrado: " + subject);
        }

        return criarReceita(
                autor,
                dto.getNome(),
                dto.getIngredientes(),
                dto.getModoPreparo(),
                dto.getTempoMedioMinutos(),
                dto.getRendimento(),
                dto.getInfoNutricional(),
                dto.getNivelTecnica()
        );
    }

    public Page<Receita> buscar(String keyword, java.util.List<String> ingredientes, boolean somenteIngredientes, Pageable pageable) {
        Specification<Receita> spec = Specification.where(ReceitaSpecifications.nomeContem(keyword))
                .and(ReceitaSpecifications.contemTodosIngredientes(ingredientes));

        Page<Receita> page = receitaRepository.findAll(spec, pageable);
    if (somenteIngredientes && ingredientes != null && !ingredientes.isEmpty()) {
        java.util.Set<String> alvo = ingredientes.stream()
            .filter(s -> s != null && !s.isBlank())
            .map(s -> s.trim().toLowerCase())
            .collect(java.util.stream.Collectors.toSet());
        java.util.List<Receita> filtradas = page.getContent().stream()
            .filter(r -> {
            String ingTexto = r.getIngredientes() == null ? "" : r.getIngredientes().toLowerCase();
            java.util.Set<String> presentes = java.util.Arrays.stream(ingTexto.split("[\n,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
            return presentes.equals(alvo);
            })
            .toList();
        return new org.springframework.data.domain.PageImpl<>(filtradas, pageable, filtradas.size());
    }
    return page;
    }

    @Transactional
    public AvaliacaoReceita avaliarReceita(Long receitaId, int score, String comentario, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new IllegalArgumentException("Não autenticado");
        String subject = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(subject)
                .orElseGet(() -> usuarioRepository.findByEmail(subject).orElse(null));
        if (usuario == null) throw new IllegalArgumentException("Usuário autenticado não encontrado: " + subject);

        Receita receita = receitaRepository.findById(receitaId)
                .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada"));
        if (score < 1 || score > 5) throw new IllegalArgumentException("Score deve ser entre 1 e 5");

        AvaliacaoReceita avaliacao = avaliacaoRepository.findByReceitaAndUsuario(receita, usuario)
                .orElseGet(AvaliacaoReceita::new);
        avaliacao.setReceita(receita);
        avaliacao.setUsuario(usuario);
        avaliacao.setScore(score);
        avaliacao.setComentario(comentario);
        avaliacao.setCreatedAt(java.time.Instant.now());
        return avaliacaoRepository.save(avaliacao);
    }

    public Page<ReceitaRankingDTO> topReceitas(String period, Pageable pageable) {
        java.time.Instant since = null;
        if (period == null || period.isBlank() || period.equalsIgnoreCase("all") || period.equalsIgnoreCase("geral")) {
            since = null;
        } else if (period.equalsIgnoreCase("week") || period.equalsIgnoreCase("semana")) {
            since = java.time.Instant.now().minus(java.time.Duration.ofDays(7));
        } else if (period.equalsIgnoreCase("month") || period.equalsIgnoreCase("mes") || period.equalsIgnoreCase("mês")) {
            since = java.time.Instant.now().minus(java.time.Duration.ofDays(30));
        } else {
            // fallback: tratar como geral
            since = null;
        }
        return avaliacaoRepository.findTopSince(since, pageable);
    }

    @Transactional
    public void deletarReceitaPorAdmin(Long receitaId, String justificativa, Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) throw new IllegalArgumentException("Não autenticado");
    String subject = auth.getName();
    Usuario admin = usuarioRepository.findByUsername(subject)
        .orElseGet(() -> usuarioRepository.findByEmail(subject).orElse(null));
    if (admin == null || admin.getRole() == null || admin.getRole().name().equals("USER")) {
        throw new IllegalArgumentException("Apenas administradores podem excluir receitas");
    }
    Receita receita = receitaRepository.findById(receitaId)
        .orElseThrow(() -> new IllegalArgumentException("Receita não encontrada"));

    ReceitaExcluida ex = new ReceitaExcluida();
    ex.setOriginalId(receita.getId());
    if (receita.getAutor() != null) {
        ex.setAutorId(receita.getAutor().getId());
        ex.setAutorUsername(receita.getAutor().getUsername());
    }
    ex.setNome(receita.getNome());
    ex.setIngredientes(receita.getIngredientes());
    ex.setModoPreparo(receita.getModoPreparo());
    ex.setTempoMedioMinutos(receita.getTempoMedioMinutos());
    ex.setRendimento(receita.getRendimento());
    ex.setInfoNutricional(receita.getInfoNutricional());
    ex.setNivelTecnica(receita.getNivelTecnica());
    ex.setCriadoEm(receita.getCriadoEm());
    ex.setRemovidoPorId(admin.getId());
    ex.setRemovidoPorUsername(admin.getUsername());
    ex.setJustificativa(justificativa);
    receitaExcluidaRepository.save(ex);
    receitaRepository.delete(receita);
    movimentacaoRepository.save(new ReceitaMovimentacao(MovimentacaoTipo.DELETED, receita.getId(), receita.getNome(),
        receita.getAutor() != null ? receita.getAutor().getId() : null,
        receita.getAutor() != null ? receita.getAutor().getUsername() : null,
        justificativa));
    }

    public java.util.Map<String, Object> relatorioReceitas(java.time.Instant from, java.time.Instant to) {
    long totalAtivas = receitaRepository.count();
    long totalExcluidas = receitaExcluidaRepository.countBy();
    long totalCriadasPeriodo = movimentacaoRepository.findMovimentacoes(from, to).stream().filter(m -> m.getTipo()==MovimentacaoTipo.CREATED).count();
    long totalExcluidasPeriodo = movimentacaoRepository.findMovimentacoes(from, to).stream().filter(m -> m.getTipo()==MovimentacaoTipo.DELETED).count();
    var movs = movimentacaoRepository.findMovimentacoes(from, to);
    java.util.Map<String,Object> resp = new java.util.LinkedHashMap<>();
    resp.put("periodo", java.util.Map.of(
        "from", from,
        "to", to
    ));
    resp.put("totais", java.util.Map.of(
        "ativas", totalAtivas,
        "excluidas", totalExcluidas,
        "criadasPeriodo", totalCriadasPeriodo,
        "excluidasPeriodo", totalExcluidasPeriodo
    ));
    resp.put("movimentacoes", movs);
    // Para não retornar possivelmente muitas excluídas, limitamos a 200 mais recentes
    java.util.List<ReceitaExcluida> excluidasRecentes = receitaExcluidaRepository.findAll().stream()
        .sorted(java.util.Comparator.comparing(ReceitaExcluida::getRemovidoEm).reversed())
        .limit(200).toList();
    resp.put("excluidasRecentes", excluidasRecentes);
    return resp;
    }
}
