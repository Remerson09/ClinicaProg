package aula2603.model.entity;


import aula2603.repository.ValidCrm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@Entity
// Removido @PrimaryKeyJoinColumn(name = "id_pessoa")
public class Medico extends Pessoa implements Serializable {

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }



    private String especialidade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMedico status = StatusMedico.ATIVO;

    @Column(name = "crm", nullable = false, unique = true)
    @NotBlank(message = "CRM é obrigatório")
    @ValidCrm // Usa nossa validação customizada
    private String crm;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultaList;

    // Relacionamentos - Lado fraco — não tem FK no banco
    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agenda> agendaList; // Renomeado de disponibilidadeLista para agendaList

    // Construtores
    public Medico() {}

    public Medico(String nome, String crm) {
        super(nome);
        this.crm = crm;
    }

    public Medico(Long id, String nome, String crm, List<Consulta> consultaList, List<Agenda> agendaList) {
        super(id, nome);
        this.crm = crm;
        this.consultaList = consultaList;
        this.agendaList = agendaList;
    }

    // Getters e Setters
    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public List<Consulta> getConsultaList() {
        return consultaList;
    }

    public void setConsultaList(List<Consulta> consultaList) {
        this.consultaList = consultaList;
    }

    public List<Agenda> getAgendaList() {
        return agendaList;
    }

    public void setAgendaList(List<Agenda> agendaList) {
        this.agendaList = agendaList;
    }

    // Métodos auxiliares
    @Override
    public boolean isMedico() {
        return true;
    }

    /**
     * Retorna apenas o número do CRM (sem o prefixo CRM-)
     */
    public String getCrmNumero() {
        if (crm != null && crm.startsWith("CRM-")) {
            return crm.substring(4);
        }
        return crm;
    }

    /**
     * Método para formatar o CRM corretamente
     */
    public static String formatCrm(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return null;
        }

        // Remove qualquer formatação existente
        String cleanNumber = numero.replaceAll("[^0-9]", "");

        // Adiciona zeros à esquerda se necessário para ter 3 dígitos
        if (cleanNumber.length() < 3) {
            cleanNumber = String.format("%03d", Integer.parseInt(cleanNumber));
        }

        return "CRM-" + cleanNumber;
    }

    /**
     * Valida se o CRM está no formato correto antes de salvar
     */
    @PrePersist
    @PreUpdate
    private void validateAndFormatCrm() {
        if (this.crm != null) {
            this.crm = this.crm.trim().toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "Medico{" +
                "id=" + getId() +
                ", nome=\'" + getNome() + "\'" +
                ", crm=\'" + crm + "\'" +
                "}";
    }

    public String dados() {
        return "Dr(a). " + getNome() + " - " + crm;
    }
}

