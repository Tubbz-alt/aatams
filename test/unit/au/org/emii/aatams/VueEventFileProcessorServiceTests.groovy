package au.org.emii.aatams


import org.joda.time.DateTime;

import grails.plugin.searchable.SearchableService
import grails.test.*


class VueEventFileProcessorServiceTests extends AbstractVueEventFileProcessorServiceTests 
{
    protected void setUp() 
	{
        super.setUp()
		
		mockLogging(EventFactoryService, true)
		eventFactoryService = new EventFactoryService()
		
		mockLogging(VueEventFileProcessorService, true)
		vueEventFileProcessorService = new VueEventFileProcessorService()
		vueEventFileProcessorService.eventFactoryService = eventFactoryService
		vueEventFileProcessorService.searchableService = searchableService
		vueEventFileProcessorService.metaClass.getRecords = { getRecords(it) }
	}

    protected void tearDown() 
	{
        super.tearDown()
    }

    void testProcess() 
	{
		vueEventFileProcessorService.process(download)

		def records = getRecords(download)
		assertEquals (records.size(), ReceiverEvent.count())
		
		records.eachWithIndex
		{
			record, i ->
			
			assertEquals(record[EventFactoryService.RECEIVER_COLUMN], ReceiverEvent.list()[i].receiverDeployment.receiver.codeName)
			assertEquals(record[EventFactoryService.DESCRIPTION_COLUMN], ReceiverEvent.list()[i].description)
			assertEquals(record[EventFactoryService.DATA_COLUMN], ReceiverEvent.list()[i].data)
			assertEquals(record[EventFactoryService.UNITS_COLUMN], ReceiverEvent.list()[i].units)
		}
    }
}
