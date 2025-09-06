package io.github.etr.playground;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import io.github.etr.playground.application.annotations.Adapter;

@AnalyzeClasses(packages = "io.github.etr.playground")
class ArchitectureTest {

    @ArchTest
    static final ArchRule shouldUseHexArchitecture = noClasses().that()
        .resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infra..")
        .because("The Domain layer shouldn't depend on the Infrastructure layer");

    @ArchTest
    static final ArchRule shouldNotLeakDomainModelViaRestAPI = methods().that()
        .arePublic().or().arePackagePrivate()
        .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
        .should()
        .notHaveRawReturnType(resideInAPackage("..domain.."))
        .because("The Domain model is encapsulated - therefore we shouldn't expose it directly to the outside world");

    @ArchTest
    static final ArchRule shouldDefinePortsAndAdapters = classes().that()
        .resideInAPackage("..infra..")
        .and().implement(resideInAPackage("..domain.."))
        .should()
        .beAnnotatedWith(Adapter.class)
        .because("We should use Adapter components to interact with external systems");

    @ArchTest
    static final ArchRule inboxShouldNotDependOnAnyOtherModule = noClasses().that()
        .resideInAPackage("..inbox..")
        .or()
        .resideInAPackage("..outbox..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..application..", "..domain..", "..infra")
        .because("The Inbox and Outbox should be decoupled from the rest of the code" + " because they will be extracted as separate libraries");

    @ArchTest
    static final ArchRule shouldUseSystemTimeComponent = noClasses()
        .that().resideInAPackage("..domain..")
        .should().callMethod(LocalDateTime.class, "now")
        .orShould().callMethod(OffsetDateTime.class, "now")
        .orShould().callMethod(Instant.class, "now")
        .orShould().callMethod(LocalDate.class, "now")
        .because("We should use the dedicated SystemTime component to get the current time, for better testability");

}

