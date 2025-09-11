package io.github.etr.playground;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import io.github.etr.playground.application.Filter;

@AnalyzeClasses(packages = "io.github.etr.playground")
class ArchitectureTest {

    @ArchTest
    static final ArchRule shouldNotDependOnOtherFilter = noClasses()
        .that().areAnnotatedWith(Filter.class)
        .should().dependOnClassesThat().areAnnotatedWith(Filter.class)
        .because("Filters should be independent of each other. (see ADR#12)");

    @ArchTest
    static final ArchRule shouldUseSystemTimeComponent = noClasses()
        .that().resideInAPackage("..playground..")
        .should().callMethod(LocalDateTime.class, "now")
        .orShould().callMethod(OffsetDateTime.class, "now")
        .orShould().callMethod(Instant.class, "now")
        .orShould().callMethod(LocalDate.class, "now")
        .because("We should use the dedicated SystemTime component to get the current time, for better testability. (see ADR#11)");

    @ArchTest
    static final ArchRule shouldNotPublishToKafkaDirectly = noClasses()
        .that().haveNameNotMatching(".*Test.*")
        .should().dependOnClassesThat().haveSimpleName("KafkaTemplate")
        .orShould().dependOnClassesThat().haveSimpleName("KafkaProducer")
        .because("We should publish messages using the pipes and filters pattern. (see ADR#13)");

}