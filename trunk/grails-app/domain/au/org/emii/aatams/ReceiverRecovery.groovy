package au.org.emii.aatams

import au.org.emii.aatams.util.GeometryUtils

import com.vividsolutions.jts.geom.Point
import org.joda.time.*
import org.joda.time.contrib.hibernate.*

/**
 * Receiver recovery is the process of retrieving a receiver from the field and
 * either (a) downloading data from the receiver and immediately redeploying it
 * or (b) returning the receiver to the office for downloading and storage for
 * future redeployment.
 */
class ReceiverRecovery 
{
    static transients = ['scrambledLocation']

    /**
     * Every recovery must have a (chronologically) preceding deployment.
     */
    static belongsTo = [deployment: ReceiverDeployment, recoverer: ProjectRole]
    static mapping =
    {
        recoveryDateTime type: PersistentDateTimeTZ,
        {
            column name: "recoveryDateTime_timestamp"
            column name: "recoveryDateTime_zone"
        }

        comments type: 'text'
    }
    
    DateTime recoveryDateTime = new DateTime(Person.defaultTimeZone())
    Point location
    DeviceStatus status
    
    String comments

    static constraints =
    {
        recoveryDateTime()
        location()
        status()
        recoverer()
        deployment()
        comments(nullable:true)
    }
    
    static searchable =
    {
        deployment(component:true)
    }
    
    String toString()
    {
        return String.valueOf(deployment?.receiver) + " recovered on " + String.valueOf(recoveryDateTime)
    }

    /**
     * Non-authenticated users can only see scrambled locations.
     */
    Point getScrambledLocation()
    {
        return GeometryUtils.scrambleLocation(location)
    }
}