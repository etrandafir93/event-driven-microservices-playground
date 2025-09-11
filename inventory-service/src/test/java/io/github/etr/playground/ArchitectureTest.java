package io.github.etr.playground;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "io.github.etr.playground")
class ArchitectureTest {

    @ArchTest
    static final ArchRule shouldUseSystemTimeComponent = noClasses()
        .that().resideInAPackage("..inventory..")
        .should().callMethod(LocalDateTime.class, "now")
        .orShould().callMethod(OffsetDateTime.class, "now")
        .orShould().callMethod(Instant.class, "now")
        .orShould().callMethod(LocalDate.class, "now")
        .because("We should use the dedicated SystemTime component to get the current time, for better testability");

    @ArchTest
    static final ArchRule shouldUseKafkaOperationsInterface = noClasses()
        .that()
        .resideInAnyPackage("..inventory..", "..reservation..")
        .should()
        .dependOnClassesThat().haveSimpleName("KafkaTemplate")
        .orShould().dependOnClassesThat().haveSimpleName("KafkaProducer")
        .because("We should use the KafkaOperations interface, for better testability");

}