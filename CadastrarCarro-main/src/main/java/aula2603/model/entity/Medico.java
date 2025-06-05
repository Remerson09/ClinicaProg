package aula2603.model.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
// @AttributeOverride(name = "id", column = @Column(name = "medico_id")) // Exemplo se necessário para TABLE_PER_CLASS
public class Medico extends Pessoa {
    // id e nome são herdados de Pessoa

    private String crm;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true) // Adicionado cascade e orphanRemoval
    private List<Consulta> consultas;

    // Getters e Setters
    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }

    // Métodos auxiliares
    public String dados() {
        return "Médico: " + getNome() + " | CRM: " + crm; // Usa getNome() da superclasse
    }

    public String consultas() {
        StringBuilder sb = new StringBuilder();
        if (consultas != null) {
            for (Consulta consulta : consultas) {
                sb.append(consulta.dados()).append("\n");
            }
        }
        return sb.toString();
    }
}
