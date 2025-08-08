package aula2603.model.entity;

import aula2603.repository.ValidCrm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validador customizado para o formato do CRM.
 * Implementa a lógica de validação para a anotação @ValidCrm.
 */
public class CrmValidator implements ConstraintValidator<ValidCrm, String> {

    // Padrão regex para validar o formato CRM-XXX
    // ^CRM-\d{3}$ significa:
    // ^ - início da string
    // CRM- - literal "CRM-"
    // \d{3} - exatamente 3 dígitos
    // $ - fim da string
    private static final String CRM_PATTERN = "^CRM-\\d{3}$";

    private static final Pattern pattern = Pattern.compile(CRM_PATTERN);

    @Override
    public void initialize(ValidCrm constraintAnnotation) {
        // Método chamado uma vez durante a inicialização do validador
        // Pode ser usado para configurações específicas se necessário
    }

    @Override
    public boolean isValid(String crm, ConstraintValidatorContext context) {
        // Se o valor for null, deixa a validação @NotNull/NotBlank cuidar disso
        if (crm == null) {
            return true;
        }

        // Remove espaços em branco no início e fim
        crm = crm.trim();

        // Se estiver vazio após trim, deixa @NotBlank cuidar
        if (crm.isEmpty()) {
            return true;
        }

        // Valida o padrão
        boolean isValid = pattern.matcher(crm).matches();

        if (!isValid) {
            // Personaliza a mensagem de erro baseada no tipo de erro
            String customMessage = buildCustomMessage(crm);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(customMessage)
                    .addConstraintViolation();
        }

        return isValid;
    }

    /**
     * Constrói uma mensagem de erro personalizada baseada no tipo de erro encontrado.
     */
    private String buildCustomMessage(String crm) {
        if (crm.length() < 7) {
            return "CRM muito curto. Formato correto: CRM-XXX (exemplo: CRM-123)";
        }

        if (crm.length() > 7) {
            return "CRM muito longo. Formato correto: CRM-XXX (exemplo: CRM-123)";
        }

        if (!crm.startsWith("CRM-")) {
            if (crm.toLowerCase().startsWith("crm-")) {
                return "CRM deve estar em maiúsculas. Formato correto: CRM-XXX (exemplo: CRM-123)";
            } else {
                return "CRM deve começar com 'CRM-'. Formato correto: CRM-XXX (exemplo: CRM-123)";
            }
        }

        if (!crm.substring(4).matches("\\d{3}")) {
            return "Os últimos 3 caracteres devem ser números. Formato correto: CRM-XXX (exemplo: CRM-123)";
        }

        return "Formato de CRM inválido. Use o formato: CRM-XXX (exemplo: CRM-123)";
    }

    /**
     * Método utilitário para validar CRM programaticamente.
     */
    public static boolean isValidCrmFormat(String crm) {
        if (crm == null || crm.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(crm.trim()).matches();
    }

    /**
     * Método utilitário para extrair apenas o número do CRM.
     */
    public static String extractCrmNumber(String crm) {
        if (isValidCrmFormat(crm)) {
            return crm.substring(4); // Remove "CRM-"
        }
        return null;
    }
}

