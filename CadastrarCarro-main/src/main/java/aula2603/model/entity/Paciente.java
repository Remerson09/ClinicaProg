package aula2603.model.entity;


import aula2603.model.entity.Consulta;
import aula2603.model.entity.Pessoa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
// @AttributeOverride(name = \"id\", column = @Column(name = \"paciente_id\")) // Exemplo se necessário para TABLE_PER_CLASS com nomes de coluna específicos
public class Paciente extends Pessoa {
    // id e nome são herdados de Pessoa

    @NotBlank(message = "O telefone não pode estar em branco.")
    @Size(min = 10, max = 15, message = "O telefone deve ter entre 10 e 15 caracteres (incluindo DDD e possíveis hífens/parênteses).")
    private String telefone;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true) // Adicionado cascade e orphanRemoval para gerenciar consultas
    private List<Consulta> consultas;

    // Getters e Setters
    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }

    // Métodos auxiliares
    public String dados() {
        return "Paciente: " + getNome() + " | Telefone: " + telefone; // Usa getNome() da superclasse
    }

    public String consultas() {
        StringBuilder sb = new StringBuilder();
        if (consultas != null) {
            for (Consulta consulta : consultas) {
                sb.append(consulta.dados()).append("n");
            }
        }
        return sb.toString();
    }
}

