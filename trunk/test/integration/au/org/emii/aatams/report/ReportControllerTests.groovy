package au.org.emii.aatams.report

import grails.test.*

import au.org.emii.aatams.*

import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.SecurityUtils
 
import org.codehaus.groovy.grails.plugins.jasper.*
import org.apache.commons.lang.StringUtils

import javax.servlet.ServletContext

/**
 * Report formats are CSV, so that we can easily compare controller output to
 * an expected CSV output. (as opposed to PDF).
 */
class ReportControllerTests extends ControllerUnitTestCase 
{
    def reportInfoService
    def reportQueryExecutorService
    
    def jasperService
    
    protected void setUp() 
    {
        super.setUp()
        
        mockLogging(ReportController, true)
        
        /**
         * Setup security manager.
         */
        def subject = [ getPrincipal: { "jkburges" },
                        isAuthenticated: { true },
                        isPermitted: { true }
                      ] as Subject

        ThreadContext.put( ThreadContext.SECURITY_MANAGER_KEY, 
                            [ getSubject: { subject } ] as SecurityManager )

        SecurityUtils.metaClass.static.getSubject = { subject }
        
        controller.metaClass.servletContext = 
            [ getRealPath: {System.getProperty("user.dir") + "/web-app" + it }] as ServletContext

        controller.params.pdf = "PDF"
        controller.params._format = "CSV"
		controller.params._type = "report"
    }

    protected void tearDown() 
    {
        super.tearDown()
    }

    void testExecuteReceiverNoFilter() 
    {
        controller.params._name = "receiver"
        controller.params."filter.organisation.name" = null
        controller.params._file = "receiverList"
        controller.params.filter = 
                    ["eq.organisation.name":null, 
                     eq:[organisation:[name:null]]]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverNoFilter")
    }
    
    void testExecuteReceiverFilterByOrg() 
    {
        controller.params._name = "receiver"
        controller.params._file = "receiverList"
        controller.params.filter = 
                    [eq:[organisation:[name:"IMOS"]],
				     "eq.organisation.name":"IMOS"]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverFilterByOrg")
    }

    void testExecuteInstallationStationNoFilter() 
    {
        controller.params._name = "installationStation"
        controller.params._file = "installationStationList"
        controller.params.filter = 
                    [eq:[installation:[project:[name:null]]],
					 "eq.installation.project.name":null]
					
        controller.execute()
        
        checkResponse("testExecuteInstallationStationNoFilter")
    }
    
    void testExecuteInstallationStationByProject() 
    {
        controller.params._name = "installationStation"
        controller.params._file = "installationStationList"
        controller.params.filter = 
                    [eq:[installation:[project:[name:"Seal Count"]]],
					"eq.installation.project.name":"Seal Count"]
					
        controller.execute()
        
        checkResponse("testExecuteInstallationStationByProject")
    }

    void testExecuteReceiverDeploymentNoFilter() 
    {
        controller.params._name = "receiverDeployment"
        controller.params._file = "receiverDeploymentList"
        controller.params.filter = 
            [eq:[station:[installation:[project:[name:null], name:null]]],
			 "eq.station.installation.project.name":null]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverDeploymentNoFilter")
    }
    
    void testExecuteReceiverDeploymentByProject() 
    {
        controller.params._name = "receiverDeployment"
        controller.params._file = "receiverDeploymentList"
        controller.params.filter = 
            [eq:[station:[installation:[project:[name:"Seal Count"], name:null]]],
			"eq.station.installation.project.name":"Seal Count"]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverDeploymentByProject")
    }
    
    void testExecuteReceiverDeploymentByInstallation() 
    {
        controller.params._name = "receiverDeployment"
        controller.params._file = "receiverDeploymentList"
        controller.params.filter = 
            [eq:[station:[installation:[project:[name:null], name:"Ningaloo Array"]]],
			 "eq.station.installation.project.name":null]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverDeploymentByInstallation")
    }

    void testExecuteReceiverDeploymentByProjectAndInstallation() 
    {
        controller.params._name = "receiverDeployment"
        controller.params._file = "receiverDeploymentList"
        controller.params.filter = 
            [eq:[station:[installation:[project:[name:"Seal Count"], name:"Heron Island"]]],
			 "eq.station.installation.project.name":"Seal Count",
			 "eq.station.installation.name":"Heron Island"]
                 
        controller.execute()
        
        checkResponse("testExecuteReceiverDeploymentByProjectAndInstallation")
    }
    
    void testExecuteAnimalReleaseSummary()
    {
        controller.params._name = "animalReleaseSummary"
        controller.params._file = "animalReleaseSummary"
        controller.params.filter = [:]
                 
        controller.execute()
        
        checkResponse("testExecuteAnimalReleaseSummary")
    }
    
    void testExecuteTag()
    {
        controller.params._name = "tag"
        controller.params._file = "tagExtract"
        controller.params.filter = [:]
		controller.params._type = "extract"
		
        controller.execute()
        
        checkResponse("testExecuteTag")
    }
	
	void testExecuteDetectionExtract()
	{
		controller.params._name = "detection"
		controller.params._file = "detectionExtract"
		controller.params.filter = [:]
		controller.params._type = "extract"
		
		controller.execute()
		
		checkResponse("testExecuteDetection")
	}

//	void testExecuteDetectionExtractWithTimestampFilter()
//	{
//		controller.params.putAll( 
//			[CSV:'CSV', 
//			 "filter.between.timestamp_day":["17", "17"], 
//			 "filter:[between.timestamp_day":['17', '17'], 
//		     between:[timestamp_day:['17', '17'], 
// 				      timestamp_minute:['08', '10'], 
// 					  timestamp_year:['2009', '2009'], 
////					  timestamp:[date.struct, date.struct], 
//					  timestamp_hour:['14', '14'], 
//					  timestamp_month:['1', '1']], 
//			 "between.timestamp_minute":['08', '10'], 
//			 "between.timestamp_year":['2009', '2009'], 
////			 between.timestamp:[date.struct, date.struct], 
//			 "between.timestamp_hour":['14', '14'], 
//			 "between.timestamp_month":['1', '1'], 
//		 	 "filter.between.timestamp_minute":['08', '10'], 
//			 _name:'detection', 
//			 "filter.between.timestamp_year":['2009', '2009'], 
////			 "filter.between.timestamp":[date.struct, date.struct], 
//			 _type:'extract', 
//			 "filter.between.timestamp_hour":['14', '14'], 
//			 "filter.between.timestamp_month":['1', '1'], 
//			 _file:'detectionExtract'])
//		
//		controller.execute()
//		checkResponse("testExecuteDetectionExtractWithTimestampFilter")
//	}

	
//	void testExecuteDetectionWithNonMatchingFilter()
//	{
//		controller.params._name = "detection"
//		controller.params._file = "detectionExtract"
//
//		try
//		{
//			controller.params.filter = [receiverDeployment:[station:[name:"no match"]]]
//			controller.execute()
//			
//			controller.params.filter = [receiverDeployment:[station:[name:"Bondi SW2"]]]
//			controller.execute()
//		}
//		catch (Exception e)
//		{
//			fail()
//		}
//	}

    private void checkResponse(def expectedFileName)
    {
        // Compare the response content with expected.
        String expectedFilePath = constructFilePath(expectedFileName)
            
        File expectedFile = new File(expectedFilePath)
            
        // Write the content to a temp file (so we can see the test output after
        // the test has run).
        File tmpFile = File.createTempFile(expectedFileName + ".actual.csv", "")
        tmpFile.write(controller.response.contentAsString.trim())
        
        // Compare all but the last line (which includes a date, and therefore
        // won't match).
        assertEquals(
            "", 
            StringUtils.difference(removeLastLine(expectedFile.getText().trim()), 
                                   removeLastLine(controller.response.contentAsString.trim())))
    }

	private String constructFilePath(expectedFileName) {
		String expectedFilePath = \
            System.getProperty("user.dir") + \
            "/test/integration/au/org/emii/aatams/report/resources/" + \
            expectedFileName + ".expected.csv"
		return expectedFilePath
	}
    
    String removeLastLine(String s)
    {
        def lineCount = 0
        s.eachLine { lineCount ++}
        
        def retString = ""
        int index = 0
        
        s.eachLine
        {
            if (index == (lineCount - 1))
            {
                // last line
            }
            else
            {
                retString += it + '\n'
            }
            
            index++
        }
        
        return retString 
    }
}