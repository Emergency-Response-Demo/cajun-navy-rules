package com.redhat.cajun.navy.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.redhat.cajun.navy.rules.model.Destination;
import com.redhat.cajun.navy.rules.model.Destinations;
import com.redhat.cajun.navy.rules.model.Incident;
import com.redhat.cajun.navy.rules.model.IncidentPriority;
import com.redhat.cajun.navy.rules.model.Mission;
import com.redhat.cajun.navy.rules.model.MissionAssignment;
import com.redhat.cajun.navy.rules.model.Responder;
import com.redhat.cajun.navy.rules.model.Responders;
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

@DisplayName( "Cajun Navy Incident Assignment Tests" )
public class IncidentResponderAssignmentRulesTest {

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
     *      The priority of the incident is equal to the average priority
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 100
     *        (100 for distance < 5 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKmNoPriorityObject() {

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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
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
        assertEquals(100, missionAssignment.getCompatibilityScore());
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
     *      The distance between the responder and the incident is less than 5 km
     *      The priority of the incident is equal to the average priority
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 100
     *        (100 for distance < 5 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKmPriorityEqualToAverage() {

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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(1));
        incidentPriority.setAveragePriority(new BigDecimal(1.0));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(100, missionAssignment.getCompatibilityScore());
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
     *      The distance between the responder and the incident is less than 5 km
     *      The priority of the incident is higher than the average priority
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 100
     *        (100 for distance < 5 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKmPriorityHigherThanAverage() {

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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(100, missionAssignment.getCompatibilityScore());
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
     *      The MissionAssignment has a priority of 75
     *        (75 for distance between 5 km and 10 km)
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
        responder.setBoatCapacity(10);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.06000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(75, missionAssignment.getCompatibilityScore());
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
     *      The MissionAssignment has a priority of 50
     *        (50 for distance between 10 km and 15 km)
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
        responder.setBoatCapacity(10);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.12000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(50, missionAssignment.getCompatibilityScore());
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
     *      The MissionAssignment has a priority of 25
     *        (25 for distance greater than 15 km)
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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.15000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(25, missionAssignment.getCompatibilityScore());
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
     *      The MissionAssignment has a priority of 200
     *        (100 for distance less than 5 km, 100 for exact capacity match)
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

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      The responder can fit the exact number of people in their boat
     *      The distance between the responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 150
     *        (100 for distance less than 5 km, 50 for boat capacity <= number of people + 2)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenBoatFitsNumberOfPeoplePlus2() {

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
        responder.setBoatCapacity(4);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(150, missionAssignment.getCompatibilityScore());
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
     *      The MissionAssignment has a priority of 125
     *        (100 for distance less than 5 km, 25 for boat capacity <= number of people + 4)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenBoatFitsNumberOfPeoplePlus4() {

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
        responder.setBoatCapacity(6);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      There is a need for medical assistance
     *      The responder can fit the exact number of people in their boat
     *      The responder can provide medical assistance
     *      The distance between the responder and the incident is less than 5 km
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 300
     *        (100 for distance less than 5 km, 100 for exact capacity match,
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

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(2));
        incidentPriority.setAveragePriority(new BigDecimal(1.9));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(300, missionAssignment.getCompatibilityScore());
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
     *      The responder can NOT fit the number of people in their boat
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

        Responders responders = new Responders();
        responders.add(responder);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
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
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is 0
     *
     *    Then:
     *      No MissionAssignment is created
     *      No mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenPriorityIsLowerThanAverageAndPriorityIsZero() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responder responder2 = new Responder();
        responder.setId("responder2");
        responder.setBoatCapacity(4);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);
        responders.add(responder2);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(0));
        incidentPriority.setAveragePriority(new BigDecimal(1.1));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is greater than zero but lower or equal than 5
     *      There are more incidents waiting to be assigned than available responders / 1.5
     *
     *    Then:
     *      No MissionAssignment is created
     *      No mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenPriorityIsLowerThanAverage() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responder responder2 = new Responder();
        responder.setId("responder2");
        responder.setBoatCapacity(4);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));

        Responders responders = new Responders();
        responders.add(responder);
        responders.add(responder2);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(5));
        incidentPriority.setAveragePriority(new BigDecimal(5.1));
        incidentPriority.setIncidents(new BigDecimal(2));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is greater than zero and lower or equal to 5
     *      There are less incidents waiting to be assigned than available responders / 1.5
     *
     *    Then:
     *      A MissionAssignment is created
     */
    @Test
    void testAssignMissionWhenPriorityIsLowerThanAverageSufficientAvailableResponders() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responder responder2 = new Responder();
        responder.setId("responder1");
        responder.setBoatCapacity(4);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("35.03000"));
        responder.setLongitude(new BigDecimal("-78.04000"));

        Responders responders = new Responders();
        responders.add(responder);
        responders.add(responder2);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(5));
        incidentPriority.setAveragePriority(new BigDecimal(5.1));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is greater than 5 but lower than or equal to 10
     *      The priority of the incident is lower than the average priority divided by 2
     *
     *    Then:
     *      No MissionAssignment is created
     *      No mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenPriorityIsBetweenFiveAndTenButStillTooLow() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(10));
        incidentPriority.setAveragePriority(new BigDecimal(21));
        incidentPriority.setIncidents(new BigDecimal(2));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is greater than 5 but lower than or equal to 10
     *      The priority of the incident is higher than the average priority divided by 2
     *
     *    Then:
     *      A MissionAssignment is created
     */
    @Test
    void testAssignMissionWhenPriorityIsBetweenFiveAndTen() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(10));
        incidentPriority.setAveragePriority(new BigDecimal(19));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      The priority of the incident is lower than the average priority
     *      The priority of the incident is greater 10
     *      The priority of the incident is higher than the average priority divided by 2
     *
     *    Then:
     *      A MissionAssignment is created
     */
    @Test
    void testAssignMissionWhenPriorityIsHigherThanTen() {

        Incident incident = new Incident();
        incident.setId("incident1");
        incident.setNumPeople(3);
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

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(11));
        incidentPriority.setAveragePriority(new BigDecimal(12));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(MissionAssignment.class), "missionassignment"));
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("missionassignment"));
        assertTrue(results.getValue("missionassignment") instanceof List);
        assertEquals(1, ((List)results.getValue("missionassignment")).size());

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(Status.ASSIGNED, mission.getStatus());
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

        Responders responders = new Responders();
        responders.add(responder1);
        responders.add(responder2);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
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

        Responders responders = new Responders();
        responders.add(responder1);
        responders.add(responder2);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
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

        Responders responders = new Responders();
        responders.add(responder);

        Destination destination1 = new Destination();
        destination1.setName("Destination1");
        destination1.setLatitude(new BigDecimal("33.97000"));
        destination1.setLongitude(new BigDecimal("-76.96000"));

        Destination destination2 = new Destination();
        destination2.setName("Destination2");
        destination2.setLatitude(new BigDecimal("34.03000"));
        destination2.setLongitude(new BigDecimal("-77.06000"));

        Destinations destinations = new Destinations();
        destinations.add(destination1);
        destinations.add(destination2);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(destinations));
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

    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There is an incident
     *      There is no need for medical assistance
     *      The responder can fit the number of people in their boat
     *      The distance between the responder and the incident is less than 5 km
     *      The priority of the incident is equal to the average priority
     *      The responder is a person
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 200
     *        (100 for distance < 5 km, 100 for responder = person)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKmPriorityEqualToAverageResponderIsPerson() {

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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));
        responder.setPerson(true);

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(1));
        incidentPriority.setAveragePriority(new BigDecimal(1.0));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
     *      The distance between the responder and the incident is less than 5 km
     *      The priority of the incident is equal to the average priority
     *      The responder is not a person
     *
     *
     *    Then:
     *      A MissionAssignment is created
     *      The MissionAssignment has a priority of 100
     *        (100 for distance < 5 km)
     *      A mission is assigned to the responder
     */
    @Test
    void testAssignMissionWhenDistanceLessThanFiveKmPriorityEqualToAverageResponderIsNotAPerson() {

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
        responder.setBoatCapacity(13);
        responder.setHasMedical(false);
        responder.setLatitude(new BigDecimal("34.03000"));
        responder.setLongitude(new BigDecimal("-77.04000"));
        responder.setPerson(false);

        Responders responders = new Responders();
        responders.add(responder);

        IncidentPriority incidentPriority = new IncidentPriority();
        incidentPriority.setIncidentId("incident1");
        incidentPriority.setPriority(new BigDecimal(1));
        incidentPriority.setAveragePriority(new BigDecimal(1.0));
        incidentPriority.setIncidents(new BigDecimal(1));

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(incidentPriority));
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
        assertEquals(100, missionAssignment.getCompatibilityScore());
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
     *      There are two responders
     *      There is an incident
     *      There is no need for medical assistance
     *      Both responders can fit the number of people in their boat
     *      The distance between the responders and the incident is less than 5 km
     *      The second responder is a person
     *
     *    Then:
     *      A mission is assigned to the second responder
     */
    @Test
    void testAssignMissionToPerson() {

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
        responder1.setPerson(false);

        Responder responder2 = new Responder();
        responder2.setId("responder2");
        responder2.setBoatCapacity(3);
        responder2.setHasMedical(false);
        responder2.setLatitude(new BigDecimal("34.03010"));
        responder2.setLongitude(new BigDecimal("-77.04000"));
        responder2.setPerson(true);

        Responders responders = new Responders();
        responders.add(responder1);
        responders.add(responder2);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident));
        commands.add(CommandFactory.newInsert(responders));
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
        assertEquals(responder2.getId(), mission.getResponderId());
        assertEquals(responder2.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder2.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }


    /**
     *  Test description:
     *
     *    When :
     *      There is a responder
     *      There are two incidents
     *      There is no need for medical assistance
     *      Both incidents have very high priority (> 10)
     *      The boat can fit EXACTLY the number of people from the first incident
     *      The difference between the boat capacity and second incident's number of people is greater than 4
     *      The distance between the responder and the first incident is less than 5 km
     *      The distance between the responder and the second incident is more than 15 km 
     *      The second incident is in a priority zone
     *
     *    Then:
     *      A mission is created for the second incident
     */
    @Test
    void testAssignMissionWhenIncidentInPriorityZone() {
        Incident incident1 = new Incident();
        incident1.setId("incident1");
        incident1.setNumPeople(6);
        incident1.setMedicalNeeded(false);
        incident1.setLatitude(new BigDecimal("34.15000"));
        incident1.setLongitude(new BigDecimal("-77.04000"));
        incident1.setReportedTime(System.currentTimeMillis());
        incident1.setReporterId("reporter1");

        Incident incident2 = new Incident();
        incident2.setId("incident2");
        incident2.setNumPeople(1);
        incident2.setMedicalNeeded(false);
        incident2.setLatitude(new BigDecimal("34.00000"));
        incident2.setLongitude(new BigDecimal("-77.00000"));
        incident2.setReportedTime(System.currentTimeMillis());
        incident2.setReporterId("reporter2");

        IncidentPriority incident1Priority = new IncidentPriority();
        incident1Priority.setIncidentId("incident1");
        incident1Priority.setEscalated(false);
        incident1Priority.setIncidents(new BigDecimal(2));
        incident1Priority.setAveragePriority(new BigDecimal(45));
        incident1Priority.setPriority(new BigDecimal(20));

        IncidentPriority incident2Priority = new IncidentPriority();
        incident2Priority.setIncidentId("incident2");
        incident2Priority.setEscalated(true);
        incident2Priority.setIncidents(new BigDecimal(2));
        incident2Priority.setAveragePriority(new BigDecimal(45));
        incident2Priority.setPriority(new BigDecimal(70));


        Responder responder1 = new Responder();
        responder1.setId("responder1");
        responder1.setBoatCapacity(6);
        responder1.setHasMedical(false);
        responder1.setLatitude(new BigDecimal("34.15000"));
        responder1.setLongitude(new BigDecimal("-77.04000"));
        responder1.setPerson(false);

        Responders responders = new Responders();
        responders.add(responder1);

        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession");

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(incident1));
        commands.add(CommandFactory.newInsert(incident2));
        commands.add(CommandFactory.newInsert(incident1Priority));
        commands.add(CommandFactory.newInsert(incident2Priority));
        commands.add(CommandFactory.newInsert(responders));
        commands.add(CommandFactory.newInsert(new Mission()));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newGetObjects(new ClassObjectFilter(Mission.class), "mission"));

        Command<?> batch = CommandFactory.newBatchExecution(commands);
        ExecutionResults results = (ExecutionResults) session.execute(batch);

        assertNotNull(results.getValue("mission"));
        assertTrue(results.getValue("mission") instanceof List);
        assertEquals(1, ((List)results.getValue("mission")).size());
        Mission mission = (Mission) ((List)(results.getValue("mission"))).get(0);
        assertEquals(incident2.getId(), mission.getIncidentId());
        assertEquals(incident2.getLatitude(), mission.getIncidentLat());
        assertEquals(incident2.getLongitude(), mission.getIncidentLong());
        assertEquals(responder1.getId(), mission.getResponderId());
        assertEquals(responder1.getLatitude(), mission.getResponderStartLat());
        assertEquals(responder1.getLongitude(), mission.getResponderStartLong());
        assertEquals(Status.ASSIGNED, mission.getStatus());
    }

}
