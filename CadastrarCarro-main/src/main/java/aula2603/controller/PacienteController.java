package aula2603.controller;



import aula2603.model.entity.Consulta;
import aula2603.model.entity.Paciente;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.PacienteRepository;
import jakarta.validation.Valid; // Importação adicionada
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private ConsultaRepository consultaRepository;

    @GetMapping
    public String listar(Model model) { model.addAttribute("pacientes", pacienteRepository.findAll());
        return "paciente/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) { model.addAttribute("paciente", new Paciente());
        return "paciente/form";
    }

    // Método para salvar um novo paciente com validação
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("paciente") Paciente paciente, // Adicionado @Valid e nome do atributo
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) { // Adicionado Model

        if (result.hasErrors()) {// Não precisa adicionar 'paciente' ao model explicitamente se usar @ModelAttribute("paciente")
            // model.addAttribute("paciente", paciente); // O BindingResult já faz isso
            return "paciente/form"; // Retorna para o formulário se houver erros
        }

        try {// Verifica se é um novo paciente (ID deve ser nulo)
            if (paciente.getId() != null) {  // Considerar lançar exceção ou adicionar erro ao BindingResult
                result.rejectValue("id", "paciente.id.notnull", "ID deve ser nulo para novo paciente");
                return "paciente/form";
            }

            // Salva o paciente
            Paciente savedPaciente = pacienteRepository.save(paciente);

            // Verifica se o ID foi gerado corretamente (opcional, save geralmente lança exceção se falhar)
            if (savedPaciente.getId() == null) { throw new IllegalStateException("Falha ao gerar ID para o paciente");
            }

            redirectAttributes.addFlashAttribute("success", "Paciente cadastrado com sucesso!");
            return "redirect:/pacientes";

        } catch (Exception e) { // Em caso de outras exceções, pode ser útil logar e mostrar uma mensagem genérica
            // model.addAttribute("paciente", paciente); // Mantém os dados
            model.addAttribute("error", "Erro inesperado ao cadastrar: " + e.getMessage());
            return "paciente/form"; // Retorna ao form com a mensagem de erro
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + id)); // Mensagem mais informativa
        model.addAttribute("paciente", paciente);
        return "paciente/form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("paciente") Paciente pacienteAtualizado, // Adicionado @Valid e nome do atributo
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) { // Adicionado Model

        if (result.hasErrors()) { // Garante que o ID esteja no objeto para o th:action funcionar corretamente na view
            pacienteAtualizado.setId(id);
            // model.addAttribute("paciente", pacienteAtualizado); // Não necessário com @ModelAttribute
            return "paciente/form"; // Retorna para o formulário se houver erros
        }

        try {// Verifica se o paciente a ser atualizado existe
            // findById já lança exceção se não encontrar, mas podemos adicionar uma verificação explícita
            if (!pacienteRepository.existsById(id)) { throw new IllegalArgumentException("Paciente não encontrado para atualização: " + id);
            }

            // Define o ID no objeto atualizado para garantir que o save funcione como update
            pacienteAtualizado.setId(id);
            pacienteRepository.save(pacienteAtualizado);

            redirectAttributes.addFlashAttribute("success", "Paciente atualizado com sucesso!");
            return "redirect:/pacientes";

        } catch (Exception e) {// Em caso de outras exceções
            pacienteAtualizado.setId(id); // Garante o ID para o form
            // model.addAttribute("paciente", pacienteAtualizado); // Mantém os dados
            model.addAttribute("error", "Erro inesperado ao atualizar: " + e.getMessage());
            return "paciente/form"; // Retorna ao form com a mensagem de erro
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirPaciente(@PathVariable Long id, RedirectAttributes redirectAttributes) { // Adicionado RedirectAttributes
        try {
            // Verifica se o paciente existe antes de tentar excluir
            if (!pacienteRepository.existsById(id)) {
                throw new IllegalArgumentException("Paciente não encontrado para exclusão: " + id);
            }
            // Remove todas as consultas vinculadas (se a relação permitir cascade, isso pode não ser necessário)
            // A lógica atual parece correta se não houver cascade delete configurado
            List<Consulta> consultas = consultaRepository.findByPacienteId(id);
            consultaRepository.deleteAll(consultas);

            // Agora pode remover o paciente
            pacienteRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Paciente excluído com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir paciente: " + e.getMessage());
        }
        return "redirect:/pacientes";
    }

    @GetMapping("/consultas/{id}")
    public String consultasPaciente(@PathVariable Long id, Model model) {Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + id));

        // Usa o método com JOIN FETCH para garantir que os médicos sejam carregados junto
        List<Consulta> consultas = consultaRepository.findByPacienteIdWithMedico(id);

        model.addAttribute("paciente", paciente);
        model.addAttribute("consultas", consultas);
        return "paciente/consulta"; // Verifique se esse é o nome correto do seu template
    }

    @GetMapping("/buscar")
    public String buscarPorNome(@RequestParam(required = false) String nome, Model model) { List<Paciente> pacientes;if (nome == null || nome.isBlank()) {pacientes = pacienteRepository.findAll();
    } else { pacientes = pacienteRepository.findByNomeContainingIgnoreCase(nome);
    } model.addAttribute("pacientes", pacientes);
        model.addAttribute("nome", nome); // Mantém o termo de busca no input
        return "paciente/list"; // Template para mostrar lista de pacientes
    }
}
