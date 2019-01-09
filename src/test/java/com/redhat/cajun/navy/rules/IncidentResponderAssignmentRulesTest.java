package com.redhat.cajun.navy.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.cajun.navy.rules.model.Incident;
import com.redhat.cajun.navy.rules.model.MissionAssignments;
import com.redhat.cajun.navy.rules.model.Responder;

@DisplayName( "Cajun Navy Incident Assignement Planner Tests" )
public class IncidentResponderAssignmentRulesTest {

    private static final String SOLVER_CONFIG = "com.redhat.cajun.navy.rules.solver/incident-responder-assignment-solver.xml";

    private static final Logger LOG = LoggerFactory.getLogger( IncidentResponderAssignmentRulesTest.class );

    @Test
    @DisplayName( "My first test" )
    public void myExampleTest() {
        //TODO - @justin - fill out params for a scenario
        Incident incident = new Incident();

        //TODO - @justin - fill out params for a scenario
        Responder responderA = new Responder();

        //TODO - @justin - fill out params for a scenario
        Responder responderB = new Responder();

        //TODO - @justin - fill out params for a scenario
        Responder responderC = new Responder();

        Solver<MissionAssignments> missionAssignment = getSolver();

    }

    private Solver getSolver() {
        // TODO - @michael - adapt this code later so that we can use kie scanner
        //        KieServices kieServices = KieServices.Factory.get();
        //        KieContainer kieContainer = kieServices.newKieContainer(
        //                kieServices.newReleaseId("org.nqueens", "nqueens", "1.0.0"));
        //        SolverFactory<NQueens> solverFactory = SolverFactory.createFromKieContainerXmlResource(
        //                kieContainer, ".../nqueensSolverConfig.xml");

        KieContainer container = KieServices.Factory.get().newKieClasspathContainer();
        SolverFactory<MissionAssignments> solver = SolverFactory.createFromKieContainerXmlResource( container, SOLVER_CONFIG );
        return solver.buildSolver();

    }

}
