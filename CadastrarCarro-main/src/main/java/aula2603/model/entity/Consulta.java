package aula2603.model.entity;



import aula2603.model.entity.Medico;
import aula2603.model.entity.Paciente;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
public class Consulta {  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)  private Long id;
    @NotNull(message = "A data e hora da consulta não podem estar em branco")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Garante o formato correto para entrada
    @Column(name = "data_consulta") // Mapeia para "data_consulta" no banco
    private LocalDateTime data; // Usando apenas um campo

    @Min(value = 0, message = "O valor da consulta não pode ser negativo")
    private double valor;
    private String observacao;

    @NotNull(message = "O paciente deve ser selecionado")
    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @NotNull(message = "O médico deve ser selecionado")
    @ManyToOne
    private Medico medico;

    // Getters e Setters
    public Long getId() { return id; }  public void setId(Long id) { this.id = id; }
    public LocalDateTime getData() { return data; }   public void setData(LocalDateTime data) { this.data = data; }
    public double getValor() { return valor; }  public void setValor(double valor) { this.valor = valor; }
    public String getObservacao() { return observacao; }  public void setObservacao(String observacao) { this.observacao = observacao; }
    public Paciente getPaciente() { return paciente; }  public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Medico getMedico() { return medico; }   public void setMedico(Medico medico) { this.medico = medico; }
    public String dados() {       return "Consulta em " + data + " | Valor: R$" + valor +   " | Paciente: " + (paciente != null ? paciente.getNome() : "N/A") + // Adicionado verificação de nulidade
            " | Médico: " + (medico != null ? medico.getNome() : "N/A"); // Adicionado verificação de nulidade
    }
    // Remova os métodos getDataConsulta/setDataConsulta
}
