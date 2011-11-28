package au.org.emii.aatams.detection

import au.org.emii.aatams.*

import org.codehaus.groovy.grails.commons.ConfigurationHolder as GrailsConfig

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.joda.time.*
import java.util.TimeZone

/**
 * Implementation of the detection/surgery matching algorithm as outlined here:
 * 
 * http://redmine.emii.org.au/issues/379
 * 
 * Essentially, input is a set of detection parameters (as recorded by a receiver
 * and exported with the VUE application), output is a detection record plus
 * 0 or more DetectionSurgeries.
 */
class DetectionFactoryService 
{
    static transactional = true

    static final String DATE_AND_TIME_COLUMN = "Date and Time (UTC)"
    static final String RECEIVER_COLUMN = "Receiver"
    static final String TRANSMITTER_COLUMN = "Transmitter"
    static final String TRANSMITTER_NAME_COLUMN = "Transmitter Name"
    static final String TRANSMITTER_SERIAL_NUMBER_COLUMN = "Transmitter Serial"
    static final String SENSOR_VALUE_COLUMN = "Sensor Value"
    static final String SENSOR_UNIT_COLUMN = "Sensor Unit"
    static final String STATION_NAME_COLUMN = "Station Name"
    static final String LATITUDE_COLUMN = "Latitude"
    static final String LONGITUDE_COLUMN = "Longitude"
    
    static final String TRANSMITTER_ID_DELIM = "-"
    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z"
    
	def detectionValidatorService
	
    /**
     * Creates a detection given a map of parameters (which originate from a line
     * in a CSV upload file).
     */
    RawDetection newDetection(downloadFile, params) throws FileProcessingException 
    {
        def nativeParams = toNativeParams(params)

        RawDetection detection = initDetection(downloadFile, nativeParams)
        assert(detection)
        
        if (!detection.isValid())
        {
            detection.save()
            return detection
        }
        
        matchToTags(detection)
        
        return detection
    }
    
    Collection<RawDetection> rescanForSurgery(Surgery surgery)
    {
        DeviceStatus retiredStatus = DeviceStatus.findByStatus(DeviceStatus.RETIRED, [cache:true])
        if (surgery.tag.status == retiredStatus)
        {
            return []
        }
        
        def updatedDetections = []
        
        ValidDetection.findAllByTransmitterId(surgery.tag.codeName, [cache:true]).each
        {
            detection ->
            
            if (surgery.isInWindow(detection.timestamp))
            {
                updatedDetections.add(detection)
//                DetectionSurgery.newSavedInstance(surgery, detection, tag)
				(new DetectionSurgery(surgery:surgery,
									 tag:surgery.tag,
									 detection:detection)).save()
            }
        }
        
        return updatedDetections
    }
    
    private RawDetection initDetection(downloadFile, nativeParams)
    {
        assert(detectionValidatorService)
        return detectionValidatorService.validate(downloadFile, nativeParams)
    }
    
    private static Map toNativeParams(params)
    {
        def timestamp = new Date().parse(DATE_FORMAT, params[DATE_AND_TIME_COLUMN] + " " + "UTC")
        
        def retMap =
               [timestamp:timestamp,
                receiverName:params[RECEIVER_COLUMN],
                transmitterId:params[TRANSMITTER_COLUMN],
                transmitterName:params[TRANSMITTER_NAME_COLUMN],
                transmitterSerialNumber:params[TRANSMITTER_SERIAL_NUMBER_COLUMN],
                sensorValue:params[SENSOR_VALUE_COLUMN],
                sensorUnit:params[SENSOR_UNIT_COLUMN],
                stationName:params[STATION_NAME_COLUMN]] 

        // Latitude and longitude are optional.
        if ((params[LATITUDE_COLUMN] != null) && (params[LONGITUDE_COLUMN] != null))
        {
            Point location = new GeometryFactory().createPoint(new Coordinate(params[LONGITUDE_COLUMN], params[LATITUDE_COLUMN]))
            location.setSRID(4326)
            retMap.location = location
        }
        
        return retMap
    }
    
    private void matchToTags(detection)
    {
        assert(detection)
        
        DeviceStatus retiredStatus = DeviceStatus.findByStatus(DeviceStatus.RETIRED, [cache:true])
        
        def tags = Tag.findAllByCodeNameAndStatusNotEqual(detection.transmitterId, retiredStatus, [cache:true]) 
        tags.each
        {
            tag -> 

            tag.surgeries.each
            {
                surgery ->
                
                if (!surgery.isInWindow(detection.timestamp))
                {
                    return
                }

//                DetectionSurgery.newSavedInstance(surgery, detection, tag)
				(new DetectionSurgery(surgery:surgery,
									 tag:tag,
									 detection:detection)).save()
	
            }
        }

        def sensors = Sensor.findAllByCodeNameAndStatusNotEqual(detection.transmitterId, retiredStatus, [cache:true]) 
        sensors.each
        {
            sensor -> sensor.tag.surgeries.each
            {
                surgery ->

                if (!surgery.isInWindow(detection.timestamp))
                {
                    return
                }
                
//                DetectionSurgery.newSavedInstance(surgery, detection, tag)
				(new DetectionSurgery(surgery:surgery,
									 tag:sensor,
									 detection:detection)).save()
            }
        }
    }
    
    private void createDetectionSurgery(surgery, tag, detection)
    {
//                DetectionSurgery.newSavedInstance(surgery, detection, tag)
		(new DetectionSurgery(surgery:surgery,
							 tag:tag,
							 detection:detection)).save()
    }
}
