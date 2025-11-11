package com.example.BemComido.spec;

import com.example.BemComido.model.Receita;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;

public class ReceitaSpecifications {

    public static Specification<Receita> nomeContem(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("nome")), like);
        };
    }

    // Ingredientes armazenados em campo texto grande; fazemos busca por cada item contendo.
    public static Specification<Receita> contemTodosIngredientes(List<String> ingredientes) {
        return (root, query, cb) -> {
            if (ingredientes == null || ingredientes.isEmpty()) return cb.conjunction();
            Expression<String> ingredientesField = root.get("ingredientes");
            return ingredientes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> "%" + s.trim().toLowerCase(Locale.ROOT) + "%")
                    .map(like -> cb.like(cb.lower(ingredientesField), like))
                    .reduce(cb.conjunction(), cb::and);
        };
    }

    // Para modo "apenas esses ingredientes", validamos no pós-processamento porque LIKE não garante igualdade.
}
