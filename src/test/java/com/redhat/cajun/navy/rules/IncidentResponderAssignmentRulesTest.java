package com.redhat.cajun.navy.rules;

import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.latitude;
import static com.redhat.cajun.navy.rules.test.util.DistanceHelper.longitude;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Incident i1 = new Incident();
        i1.setId( 1 );
        i1.setNumPeople( 2 );
        i1.setMedicalNeeded( true );
        i1.setReportedTime( ZonedDateTime.now( ZoneId.systemDefault() ) ); //TODO - @michael - need to find the code for my utility class for handling datetime
        i1.setLatitude( latitude( 34.210383 ) );
        i1.setLongitude( longitude( -77.886765 ) );
        i1.setReporterId( 1 );

        Incident i2 = new Incident();
        i2.setId( 2 );
        i2.setNumPeople( 3 );
        i2.setMedicalNeeded( true );
        i2.setReportedTime( ZonedDateTime.now( ZoneId.systemDefault() ) ); //TODO - @michael - need to find the code for my utility class for handling datetime
        i2.setLatitude( latitude( 34.210383 ) );
        i2.setLongitude( longitude( -77.886765 ) );
        i2.setReporterId( 2 );

        Responder responder = new Responder();
        responder.setId( 1 );
        responder.setFullname( "Donald Duck" );
        responder.setBoatCapacity( 5 );
        responder.setHasMedical( true );
        responder.setLatitude( latitude( 34.210383 ) );
        responder.setLongitude( longitude( -77.886765 ) );
        responder.setPhoneNumber( "555-555-5555" );

        //TODO - @michael - use the adapter lib!!!! https://gitlab.consulting.redhat.com/na-business-automation-practice/business-automation-api-adapters
        StatelessKieSession session = KCONTAINER.newStatelessKieSession( "cajun-navy-ksession" );

        List<Command<?>> commands = new ArrayList<>();
        commands.add( CommandFactory.newInsertElements( Arrays.asList( i1, i2, responder ) ) );
        commands.add( CommandFactory.newFireAllRules() );
        commands.add( CommandFactory.newGetObjects( new ClassObjectFilter( Mission.class ), "mission" ) );

        Command<?> batch = CommandFactory.newBatchExecution( commands );
        ExecutionResults results = (ExecutionResults) session.execute( batch );
        System.err.println( results.getValue( "mission" ) );

    }

}
