package aula2603.model.entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Alterando para JOINED strategy
abstract public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY é geralmente compatível com JOINED
    private Long id;

    @Column(nullable = false)
    private String nome;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
