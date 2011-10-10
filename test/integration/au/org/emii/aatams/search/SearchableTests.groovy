package au.org.emii.aatams.search

import au.org.emii.aatams.*

import org.joda.time.*
import grails.test.*

class SearchableTests extends GrailsUnitTestCase 
{
//    def searchableService
    def elasticSearchService
    
    protected void setUp() 
    {
//        log.info("before super.setUp()")
        super.setUp()
        
//        log.info("before unindex()")
//        searchableService.unindex()
//        log.info("before index()")
        assert(elasticSearchService)
//        elasticSearchService.index()
//        log.info("after index()")
    }

    protected void tearDown() 
    {
        super.tearDown()
    }

    void testNothing()
    {}
    
    void testSearchOrganisationByName() 
    {
        def csiro = Organisation.findByName("CSIRO")
        def imos = Organisation.findByName("IMOS")
        
        def searchResult = Organisation.search("CSIRO")
        
        assertTrue(searchResult.searchResults*.name.contains(csiro.name))
        assertFalse(searchResult.searchResults*.name.contains(imos.name))
    }
    
    void testSearchProjects()
    {
        def sealCount = Project.findByName('Seal Count')
        def searchResult = Project.search(sealCount.name)
        
        assertTrue(searchResult.searchResults*.id.contains(sealCount.id))
    }

    void testSearchProjectsByOrgName()
    {
        def sealCount = Project.findByName('Seal Count')
        def whale = Project.findByName('Whale')
        def searchResult = Project.search('CSIRO')
        
        assertTrue(searchResult.searchResults*.id.contains(sealCount.id))
        assertFalse(searchResult.searchResults*.id.contains(whale.id))
    }
    
    void testSearchPeopleByProjectName()
    {
        def joeBloggs = Person.findByUsername('jbloggs')
        
        def searchResult = Person.search('Seal Count')
        assertTrue(searchResult.searchResults*.id.contains(joeBloggs.id))
    }
    
    void testSearchPeopleByOrganisationName()
    {
        def joeBloggs = Person.findByUsername('jbloggs')
        
        def searchResult = Person.search('CSIRO')
        assertTrue(searchResult.searchResults*.id.contains(joeBloggs.id))
    }
    
    void testSearchDeploymentsByReceiver()
    {
        def rx1 = Receiver.findByCodeName('VR2W-101336')
        def rx2 = Receiver.findByCodeName('VR2W-101337')

        def rx1Bondi = ReceiverDeployment.findByReceiver(rx1)
        def rx2Bondi = ReceiverDeployment.findByReceiver(rx2)
        
        def searchResult = ReceiverDeployment.search(rx1.codeName)
        
        assertTrue(searchResult.searchResults*.id.contains(rx1Bondi.id))
        assertFalse(searchResult.searchResults*.id.contains(rx2Bondi.id))
    }
    
    void testSearchDeploymentsByStations()
    {
        def bondiSW1 = InstallationStation.findByName('Bondi SW1')
        def bondiSW2 = InstallationStation.findByName('Bondi SW2')
        
        def rx1Bondi = ReceiverDeployment.findByStation(bondiSW1)
        def rx2Bondi = ReceiverDeployment.findByStation(bondiSW2)

        def searchResult = ReceiverDeployment.search(bondiSW1.name)
        
        assertTrue(searchResult.searchResults*.id.contains(rx1Bondi.id))
        assertFalse(searchResult.searchResults*.id.contains(rx2Bondi.id))
    }
    
    void testSearchRecoveriesByReceiver()
    {
        def rx1 = Receiver.findByCodeName('VR2W-101336')

        def rx1BondiRecovery = ReceiverRecovery.findByRecoveryDateTime(new DateTime("2013-07-25T12:34:56"))
        def rx2BondiRecovery = ReceiverRecovery.findByRecoveryDateTime(new DateTime("2013-05-17T12:54:56"))
        
        def searchResult = ReceiverRecovery.search(rx1.codeName)
        
        assertTrue(searchResult.searchResults*.id.contains(rx1BondiRecovery.id))
        assertFalse(searchResult.searchResults*.id.contains(rx2BondiRecovery.id))
    }
    
    void testSearchExportsByReceiverViaDetection()
    {
        def rx1 = Receiver.findByCodeName('VR2W-101336')

        def export1 = ReceiverDownloadFile.findByName("export1.csv")
        def export2 = ReceiverDownloadFile.findByName("export2.csv")
        
        def searchResult = ReceiverDownloadFile.search(rx1.codeName)
        
        assertTrue(searchResult.searchResults*.id.contains(export1.id))
        assertFalse(searchResult.searchResults*.id.contains(export2.id))
    }
    

    void testSearchExportsByTag()
    {
        def tag1 = Tag.findByCodeName('A69-1303-62339')
        
        def export1 = ReceiverDownloadFile.findByName("export1.csv")
        def export2 = ReceiverDownloadFile.findByName("export2.csv")

        def searchResult = ReceiverDownloadFile.search(tag1.serialNumber)
        
        assertTrue(searchResult.searchResults*.id.contains(export1.id))
        assertFalse(searchResult.searchResults*.id.contains(export2.id))
    }

    void testSearchExportsByReceiverViaEvent()
    {
        def rx2 = Receiver.findByCodeName('VR2W-101337')

        def export1 = ReceiverDownloadFile.findByName("export1.csv")
        def export2 = ReceiverDownloadFile.findByName("export2.csv")
        
        def searchResult = ReceiverDownloadFile.search(rx2.codeName)
        
        assertFalse(searchResult.searchResults*.id.contains(export1.id))
        assertTrue(searchResult.searchResults*.id.contains(export2.id))
    }
}
