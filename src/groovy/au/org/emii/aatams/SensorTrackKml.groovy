package au.org.emii.aatams

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import au.org.emii.aatams.detection.ValidDetection
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Data
import de.micromata.opengis.kml.v_2_2_0.Document
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Icon
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark
import de.micromata.opengis.kml.v_2_2_0.Style
import de.micromata.opengis.kml.v_2_2_0.gx.Track

/**
 * @author jburgess
 *
 */
class SensorTrackKml extends Kml
{
	private final TreeMap<String, TreeSet<ValidDetection>> detsBySensor
	private final String serverURL
	
	public SensorTrackKml(detections, serverURL)
	{
		this.serverURL = serverURL
		this.detsBySensor = new TreeMap<String, TreeSet<ValidDetection>>()
		loadDetections(detections)
		refresh()
	}
	
	private void loadDetections(dets)
	{
		dets.each
		{
			det ->
			
			def detsForSingleSensor = detsBySensor[det.transmitterId]
			
			if (!detsForSingleSensor)
			{
				detsForSingleSensor = new TreeSet<ValidDetection>([compare: {a, b -> a.timestamp <=> b.timestamp} ] as Comparator)
				detsBySensor[det.transmitterId] = detsForSingleSensor
			}
			
			detsForSingleSensor.add(det)
		}
	}
	
	private void refresh()
	{
		Document doc = createAndSetDocument()
		
		if (detsBySensor.isEmpty())
		{
			return
		}
		
		insertDefaultDetectionStyle(doc)
		
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime()
			
		detsBySensor.each 
		{
			transmitterId, detsForSingleSensor ->
			
			Placemark placemark = new Placemark()
			placemark.setName(transmitterId)
			placemark.setDescription(getDescription())
			placemark.setStyleUrl("#defaultDetectionStyle")
			
			Track track = new Track()
			track.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
	
			track.setWhen(detsForSingleSensor.collect {
				
				DateTime dt = new DateTime(it.timestamp)
				return fmt.print(dt)
			})

			track.setCoord(detsForSingleSensor.collect {
				
				def station = it.receiverDeployment.station
				return String.valueOf(station.longitude) + " " + String.valueOf(station.latitude)
			})

			placemark.setGeometry(track)
			doc.getFeature().add(placemark)
		}
	}
	
	private void insertDefaultDetectionStyle(Document doc)
	{
		doc.createAndAddStyle()
			.withId("defaultDetectionStyle")
			.withIconStyle(new IconStyle().withScale(1.0).withHeading(0.0).withIcon(new Icon().withHref("files/fish.png")))
			.withLineStyle(new LineStyle().withColor("ffface87").withWidth(4))
	}
	
	// TODO: refactor to GSP template.
	private String getDescription()
	{
		return '''<div>
                    <link rel="stylesheet" type="text/css" href="files/main.css" />
                    <div class="description">
                    
                        <!--  "Header" data. -->
                        <div class="dialog">
                            <table>
                                <tbody>
                                
                                    <tr class="prop">
                                        <td valign="top" class="name">Link to the Data</td>
                                        <td valign="top" class="value"><a href="''' + serverURL + '''/detection/list?filter.in=transmitterId&filter.in=$[name]">Detections for $[name]</a></td>
                                    </tr>
                                    
                                </tbody>
                            </table>
                        </div>
                       
                    </div>
                </div>'''
	}
}