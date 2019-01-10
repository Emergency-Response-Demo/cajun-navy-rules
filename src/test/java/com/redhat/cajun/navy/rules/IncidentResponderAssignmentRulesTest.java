package com.redhat.cajun.navy.rules;

import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.latitude;
import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.longitude;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

import com.redhat.cajun.navy.rules.model.Incident;
import com.redhat.cajun.navy.rules.model.Mission;
import com.redhat.cajun.navy.rules.model.Responder;

@DisplayName( "Cajun Navy Incident Assignement Planner Tests" )
public class IncidentResponderAssignmentRulesTest {

    private static final Logger LOG = LoggerFactory.getLogger( IncidentResponderAssignmentRulesTest.class );

    private static final KieContainer KCONTAINER = KieServices.Factory.get().newKieClasspathContainer();

    @Test
    @DisplayName( "My first test" )
    public void myExampleTest() {

        List<Incident> incidents = new ArrayList<>();
        for (int i = 0; i < Incidents.latitudes.length; i++) {
        	Incident incident = new Incident();
        	incident.setId( i + 1 );
        	incident.setNumPeople( ThreadLocalRandom.current().nextInt(1, 6 ));
        	Integer medicalNeeded = ThreadLocalRandom.current().nextInt(1, 3);
        	incident.setMedicalNeeded( (medicalNeeded == 1) ? true : false );
        	incident.setReportedTime( ZonedDateTime.now( ZoneId.systemDefault() ) ); //TODO - @michael - need to find the code for my utility class for handling datetime
        	incident.setLatitude( latitude( Incidents.latitudes[i] ) );
        	incident.setLongitude( longitude( Incidents.longitudes[i] ) );
        	incident.setReporterId( i + 1 );
        	incidents.add( incident );
        	
        	//LOG.info(incident.toString());
        }
        
        List<Responder> responders = new ArrayList<>();
        for (int i = 0; i < Responders.latitudes.length; i++) {
        	Responder responder = new Responder();
            responder.setId( i + 1 );
            responder.setFullname( Responders.names[i] );
            responder.setBoatCapacity( ThreadLocalRandom.current().nextInt(1, 8) );
        	Integer hasMedical = ThreadLocalRandom.current().nextInt(1, 3);
            responder.setHasMedical( (hasMedical == 1) ? true : false );
            responder.setLatitude( latitude( Responders.latitudes[i] ) );
            responder.setLongitude( longitude( Responders.longitudes[i] ) );
            responder.setPhoneNumber( "555-555-5555" );
            responders.add( responder );
            
            //LOG.info(responder.toString());
        }

        //TODO - @michael - use the adapter lib!!!! https://gitlab.consulting.redhat.com/na-business-automation-practice/business-automation-api-adapters
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
    public void testGeoRules() {
    	 List<Incident> incidents = new ArrayList<>();
         //for (int i = 0; i < Incidents.latitudes.length; i++) {
         for (int i = 0; i < 10; i++) {
         	Incident incident = new Incident();
         	incident.setId( i + 1 );
         	incident.setNumPeople( ThreadLocalRandom.current().nextInt(1, 6 ));
         	Integer medicalNeeded = ThreadLocalRandom.current().nextInt(1, 3);
         	incident.setMedicalNeeded( (medicalNeeded == 1) ? true : false );
         	incident.setReportedTime( ZonedDateTime.now( ZoneId.systemDefault() ) ); //TODO - @michael - need to find the code for my utility class for handling datetime
         	incident.setLatitude( latitude( Incidents.latitudes[i] ) );
         	incident.setLongitude( longitude( Incidents.longitudes[i] ) );
         	incident.setReporterId( i + 1 );
         	incidents.add( incident );
         	
         	//LOG.info(incident.toString());
         }
         
         List<Responder> responders = new ArrayList<>();
         for (int i = 0; i < 3; i++) {
         //for (int i = 0; i < Responders.latitudes.length; i++) {
         	Responder responder = new Responder();
             responder.setId( i + 1 );
             responder.setFullname( Responders.names[i] );
             responder.setBoatCapacity( ThreadLocalRandom.current().nextInt(1, 8) );
         	Integer hasMedical = ThreadLocalRandom.current().nextInt(1, 3);
             responder.setHasMedical( (hasMedical == 1) ? true : false );
             responder.setLatitude( latitude( Responders.latitudes[i] ) );
             responder.setLongitude( longitude( Responders.longitudes[i] ) );
             responder.setPhoneNumber( "555-555-5555" );
             responders.add( responder );
             
             //LOG.info(responder.toString());
         }

         //TODO - @michael - use the adapter lib!!!! https://gitlab.consulting.redhat.com/na-business-automation-practice/business-automation-api-adapters
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
