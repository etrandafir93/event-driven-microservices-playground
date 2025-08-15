package io.github.etr.playground;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.application.annotations.Port;

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
        .beAnnotatedWith(Port.class)
        .orShould()
        .beAnnotatedWith(Adapter.class)
        .because("The should use Port and Adapter components to interact with external systems");
}
