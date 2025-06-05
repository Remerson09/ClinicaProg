package aula2603.controller;



import aula2603.model.entity.Consulta;
import aula2603.model.entity.Medico;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.MedicoRepository;
import jakarta.validation.Valid; // Importação adicionada
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/medicos")
public class MedicoController {
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private ConsultaRepository consultaRepository;

    @GetMapping
    public String listar(Model model) {model.addAttribute("medicos", medicoRepository.findAll());
        return "medico/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) { model.addAttribute("medico", new Medico());
        return "medico/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("medico") Medico medico, // Adicionado @Valid
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) { // Adicionado Model

        if (result.hasErrors()) { // model.addAttribute("medico", medico); // Não necessário com @ModelAttribute
            return "medico/form"; // Retorna para o formulário se houver erros
        }

        try {if (medico.getId() != null) {  result.rejectValue("id", "medico.id.notnull", "ID deve ser nulo para novo médico");
            return "medico/form";
        }
            medicoRepository.save(medico);
            redirectAttributes.addFlashAttribute("success", "Médico cadastrado com sucesso!");
            return "redirect:/medicos";
        } catch (Exception e) {// model.addAttribute("medico", medico);
            model.addAttribute("error", "Erro inesperado ao cadastrar: " + e.getMessage());
            return "medico/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {Medico medico = medicoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado: " + id));
        model.addAttribute("medico", medico);
        return "medico/form";
    }

    // Renomeado para /atualizar/{id} para consistência
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("medico") Medico medicoAtualizado, // Adicionado @Valid
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) { // Adicionado Model

        if (result.hasErrors()) { medicoAtualizado.setId(id); // Garante ID para o form
            // model.addAttribute("medico", medicoAtualizado);
            return "medico/form"; // Retorna para o formulário se houver erros
        }

        try { if (!medicoRepository.existsById(id)) {  throw new IllegalArgumentException("Médico não encontrado para atualização: " + id);
        }
            medicoAtualizado.setId(id); // Garante que o ID será mantido para update
            medicoRepository.save(medicoAtualizado);
            redirectAttributes.addFlashAttribute("success", "Médico atualizado com sucesso!");
            return "redirect:/medicos";
        } catch (Exception e) { medicoAtualizado.setId(id); // Garante ID para o form
            // model.addAttribute("medico", medicoAtualizado);
            model.addAttribute("error", "Erro inesperado ao atualizar: " + e.getMessage());
            return "medico/form";
        }
    }

    @GetMapping("/consultas/{id}")
    public String consultasMedico(@PathVariable Long id, Model model) { Medico medico = medicoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado: " + id));

        // Considerar adicionar JOIN FETCH para paciente se necessário na view
        List<Consulta> consultas = consultaRepository.findByMedicoIdWithPaciente(id); // Usando método com JOIN FETCH

        model.addAttribute("medico", medico);
        model.addAttribute("consultas", consultas);
        return "medico/consulta";
    }

    @GetMapping("/excluir/{id}")
    public String excluirMedico(@PathVariable Long id, RedirectAttributes redirectAttributes) { // Adicionado RedirectAttributes
        try {
            if (!medicoRepository.existsById(id)) {
                throw new IllegalArgumentException("Médico não encontrado para exclusão: " + id);
            }
            // Adicionar verificação se médico possui consultas antes de excluir?
            // Dependendo da regra de negócio, pode ser necessário impedir a exclusão
            // ou reatribuir/excluir consultas associadas.
            // A implementação atual pode falhar se houver FK constraint.
            // Assumindo que a exclusão é permitida ou as consultas são tratadas (ex: cascade)
            medicoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Médico excluído com sucesso!");
        } catch (Exception e) {
            // Capturar DataIntegrityViolationException especificamente pode ser útil
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir médico: " + e.getMessage());
        }
        return "redirect:/medicos";
    }

    @GetMapping("/buscar")
    public String buscarMedicos(@RequestParam(required = false) String nome, Model model) { List<Medico> medicos;  if (nome == null || nome.isBlank()) { medicos = medicoRepository.findAll();
    } else {// Assumindo que existe um método buscarPorNome no repositório
            // Se não existir, usar findByNomeContainingIgnoreCase
            medicos = medicoRepository.findByNomeContainingIgnoreCase(nome); // Ajustado para método padrão
    } model.addAttribute("medicos", medicos);
        model.addAttribute("nome", nome);
        return "medico/list";
    }
}
