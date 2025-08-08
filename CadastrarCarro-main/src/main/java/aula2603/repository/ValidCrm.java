package aula2603.repository;

import aula2603.model.entity.CrmValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotação customizada para validar o formato do CRM.
 * O formato esperado é: CRM-XXX onde XXX são 3 dígitos numéricos.
 * Exemplos válidos: CRM-123, CRM-456, CRM-789
 * Exemplos inválidos: CRM123, crm-123, CRM-12, CRM-1234, CRM-ABC
 */
@Documented
@Constraint(validatedBy = CrmValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCrm {

    String message() default "Formato de CRM inválido. Use o formato: CRM-XXX (onde XXX são 3 dígitos)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
