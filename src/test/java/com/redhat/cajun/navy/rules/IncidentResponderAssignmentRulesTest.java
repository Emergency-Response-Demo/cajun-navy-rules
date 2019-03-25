package com.redhat.cajun.navy.rules;

import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.latitude;
import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.longitude;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.redhat.cajun.navy.rules.model.Destination;
import com.redhat.cajun.navy.rules.model.Incident;
import com.redhat.cajun.navy.rules.model.Mission;
import com.redhat.cajun.navy.rules.model.MissionAssignment;
import com.redhat.cajun.navy.rules.model.Responder;
import com.redhat.cajun.navy.rules.model.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName( "Cajun Navy Incident Assignement Tests" )
public class IncidentResponderAssignmentRulesTest {

    private static final Logger LOG = LoggerFactory.getLogger( IncidentResponderAssignmentRulesTest.class );

    private static final KieContainer KCONTAINER = KieServices.Factory.get().newKieClasspathContainer();

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 200
     *        (100 for enough capacity, 100 for distance < 5 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKm() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(3);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() < 5000);
        assertEquals(200, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is between 5 and 10 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 175
     *        (100 for enough capacity, 75 for distance between 5 km and 10 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceBetweenFiveAndTenKm() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(3);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.06000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() > 5000);
        assertTrue(missionAssignment.getDistance() < 10000);
        assertEquals(175, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is between 10 and 15 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 150
     *        (100 for enough capacity, 50 for distance between 10 km and 15 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceBetweenTenAndFifteenKm() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(3);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.12000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() > 10000);
        assertTrue(missionAssignment.getDistance() < 15000);
        assertEquals(150, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(mission.getIncidentId(), incident.getId());
        assertEquals(mission.getIncidentLat(), incident.getLatitude());
        assertEquals(mission.getIncidentLong(), incident.getLongitude());
        assertEquals(mission.getResponderId(), responder.getId());
        assertEquals(mission.getResponderStartLat(), responder.getLatitude());
        assertEquals(mission.getResponderStartLong(), responder.getLongitude());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is greater than 15 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 125
     *        (100 for enough capacity, 25 for distance greater than 15 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceGreaterThanFifteenKm() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(3);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.15000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() > 15000);
        assertEquals(125, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the exact number of people in their boat
     *      The distance between the responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 250
     *        (100 for enough capacity, 100 for distance less than 5 km, 50 for exact capacity match)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenBoatFitsExactNumberOfPeople() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(2);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() < 5000);
        assertEquals(250, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is a need for medical assistance
     *      The responder can fit the exact number of people in their boat
     *      The responder can provide medical assistance
     *      The distance between the responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 250
     *        (100 for enough capacity, 100 for distance less than 5 km, 50 for exact capacity match,
     *         100 for the ability to provide medical assistance)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenMedicalAssistanceIsNeeded() {
        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(true);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(2);
        responder.setHasMedical(true);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());
        MissionAssignment missionAssignment = (MissionAssignment) ((List)(results.getValue("missionassignment"))).get(0);
        assertTrue(missionAssignment.getDistance() < 5000);
        assertEquals(350, missionAssignment.getCompatibilityScore());
        assertEquals(incident, missionAssignment.getIncident());
        assertEquals(responder, missionAssignment.getResponder());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      The responder can NOT fit the number of people in their boat     *
     *
     *    Then:
     *      No MissionAssignment is created
     *      No mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenResponderCannotFitNumberOfPeople() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(5);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(4);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(0, ((List)results.getValue("missionassignment")).size());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(Status.UNASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There are two responders
     *      There is an incident
     *      There is no need for medical assistance
     *      Both responders can fit the number of people in their boat
     *      The distance between the first responder and the incident is less than 5 km
     *      The distance between the second responder and the incident is between 5 and 10 km
     *
     *
     *    Then:
     *      A mission is assigned to the first responder
     */
    @Test
    void testAssignMissionToNearestResponder() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder1 = new Responder();
        responder1.setId("responder1");
        responder1.setBoatCapacity(3);
        responder1.setHasMedical(false);
        responder1.setLatitude(new BigDecimal("34.03000"));
        responder1.setLongitude(new BigDecimal("-77.04000"));

        Responder responder2 = new Responder();
        responder2.setId("responder2");
        responder2.setBoatCapacity(3);
        responder2.setHasMedical(false);
        responder2.setLatitude(new BigDecimal("34.06000"));
        responder2.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder1));
        commands.add(CommandFactory.newInsert(responder2));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder1.getId(), mission.getResponderId());
        assertEquals(responder1.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder1.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There are two responders
     *      There is an incident
     *      There is need for medical assistance
     *      Both responders can fit the number of people in their boat
     *      The first responder can provide medical assistance, the second can't
     *      The distance between the first responder and the incident is more than 15 km
     *      The distance between the second responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A mission is assigned to the first responder - priority is given to the ability to deliver medical assistance
     */
    @Test
    void testAssignMissionToResponderWithMedicalKit() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(true);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder1 = new Responder();
        responder1.setId("responder1");
        responder1.setBoatCapacity(3);
        responder1.setHasMedical(true);
        responder1.setLatitude(new BigDecimal("34.15000"));
        responder1.setLongitude(new BigDecimal("-77.04000"));

        Responder responder2 = new Responder();
        responder2.setId("responder2");
        responder2.setBoatCapacity(3);
        responder2.setHasMedical(false);
        responder2.setLatitude(new BigDecimal("34.03000"));
        responder2.setLongitude(new BigDecimal("-77.04000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder1));
        commands.add(CommandFactory.newInsert(responder2));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder1.getId(), mission.getResponderId());
        assertEquals(responder1.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder1.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There are two destinations
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is less than 5 km
     *      The distance between the first destination and the incident is less than 5 km
     *      The distance between the second destination and the incident is more than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      A mission is assigned to the responder
     *      The destination of the mission is set to the first destination
     *
     */
    @Test
    void testAssignMissionWithDestination() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(2);
        incident.setMedicalNeeded(false);
        incident.setLatitude(new BigDecimal("34.00000"));
        incident.setLongitude(new BigDecimal("-77.00000"));
        incident.setReportedTime(System.currentTimeMillis());
        incident.setReporterId("reporter1");

        Responder responder = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(3);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Destination destination1 = new Destination();
        destination1.setName("Destination1");
        destination1.setLatitude(new BigDecimal("33.97000"));
        destination1.setLongitude(new BigDecimal("-76.96000"));

        Destination destination2 = new Destination();
        destination2.setName("Destination2");
        destination2.setLatitude(new BigDecimal("34.03000"));
        destination2.setLongitude(new BigDecimal("-77.06000"));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responder));
        commands.add(CommandFactory.newInsert(destination1));
        commands.add(CommandFactory.newInsert(destination2));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident.getId(), mission.getIncidentId());
        assertEquals(incident.getLatitude(), mission.getIncidentLat());
        assertEquals(incident.getLongitude(), mission.getIncidentLong());
        assertEquals(responder.getId(), mission.getResponderId());
        assertEquals(responder.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder.getLongitude(), mission.getResponderStartLong());
        assertEquals(destination1.getLatitude(), mission.getDestinationLat());
        assertEquals(destination1.getLongitude(), mission.getDestinationLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    @Test
    void myExampleTest() {

        List<Incident> incidents = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
        //for (int i = 0; i < Incidents.latitudes.length; i++) {
        	Incident incident = new Incident();
        	incident.setId( Integer.toString(i + 1) );
        	incident.setNumPeople( ThreadLocalRandom.current().nextInt(1, 6 ));
        	int medicalNeeded = ThreadLocalRandom.current().nextInt(1, 3);
        	incident.setMedicalNeeded(medicalNeeded == 1);
        	incident.setReportedTime( System.currentTimeMillis() );
        	incident.setLatitude( latitude( Incidents.latitudes[i] ) );
        	incident.setLongitude( longitude( Incidents.longitudes[i] ) );
        	incident.setReporterId( Integer.toString(i + 1) );
        	incidents.add( incident );
        }
        
        List<Responder> responders = new ArrayList<>();
        for (int i = 0; i < Responders.latitudes.length; i++) {
        	Responder responder = new Responder();
            responder.setId( Integer.toString(i + 1) );
            responder.setFullname( Responders.names[i] );
            responder.setBoatCapacity( ThreadLocalRandom.current().nextInt(1, 8) );
        	int hasMedical = ThreadLocalRandom.current().nextInt(1, 3);
            responder.setHasMedical(hasMedical == 1);
            responder.setLatitude( latitude( Responders.latitudes[i] ) );
            responder.setLongitude( longitude( Responders.longitudes[i] ) );
            responder.setPhoneNumber( "555-555-5555" );
            responders.add( responder );
        }

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession" );

        List<Command<?>> commands = new ArrayList<>();
        commands.add( CommandFactory.newInsertElements( incidents ) );
        commands.add( CommandFactory.newInsertElements( responders ) );
        commands.add( CommandFactory.newFireAllRules() );
        commands.add( CommandFactory.newGetObjects( new ClassObjectFilter( Mission.class ), "mission" ) );

        Command<?> batch = CommandFactory.newBatchExecution( commands );
        ExecutionResults results = (ExecutionResults) session.execute( batch );
        System.err.println( results.getValue( "mission" ) );

    }
    
    @Test
    void testGeoRules() {
    	 List<Incident> incidents = new ArrayList<>();
         for (int i = 0; i < 10; i++) {
         	Incident incident = new Incident();
         	incident.setId( Integer.toString(i + 1) );
         	incident.setNumPeople( ThreadLocalRandom.current().nextInt(1, 6 ));
         	int medicalNeeded = ThreadLocalRandom.current().nextInt(1, 3);
         	incident.setMedicalNeeded(medicalNeeded == 1);
         	incident.setReportedTime( System.currentTimeMillis() );
         	incident.setLatitude( latitude( Incidents.latitudes[i] ) );
         	incident.setLongitude( longitude( Incidents.longitudes[i] ) );
         	incident.setReporterId( Integer.toString(i + 1) );
         	incidents.add( incident );
         }

        List<Responder> responders = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Responder responder = new Responder();
            responder.setId( Integer.toString(i + 1) );
            responder.setFullname( Responders.names[i] );
            responder.setBoatCapacity( ThreadLocalRandom.current().nextInt(1, 8) );
            int hasMedical = ThreadLocalRandom.current().nextInt(1, 3);
            responder.setHasMedical(hasMedical == 1);
            responder.setLatitude( latitude( Responders.latitudes[i] ) );
            responder.setLongitude( longitude( Responders.longitudes[i] ) );
            responder.setPhoneNumber( "555-555-5555" );
            responders.add( responder );
        }

         StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession" );

         List<Command<?>> commands = new ArrayList<>();
         commands.add( CommandFactory.newInsertElements( incidents ) );
         commands.add( CommandFactory.newInsertElements( responders ) );
         commands.add( CommandFactory.newFireAllRules() );
         commands.add( CommandFactory.newGetObjects( new ClassObjectFilter( Mission.class ), "mission" ) );

         Command<?> batch = CommandFactory.newBatchExecution( commands );
         ExecutionResults results = (ExecutionResults) session.execute( batch );
         System.err.println( results.getValue( "mission" ) );
    }

}
