package au.org.emii.aatams

import au.org.emii.aatams.command.*

import grails.test.*
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.converters.JSON

class ProjectControllerTests extends ControllerUnitTestCase 
{
    Project sealCountProject    
    Person joeBloggs
    Organisation activeOrg
    
    def permissionUtilsService
    protected void setUp() 
    {
        super.setUp()
        TestUtils.setupMessage(controller)

        mockLogging(PermissionUtilsService)
        permissionUtilsService = new PermissionUtilsService()
        controller.permissionUtilsService = permissionUtilsService
        
        mockConfig("grails.gorm.default.list.max = 10")
        controller.metaClass.getGrailsApplication = { -> [config: org.codehaus.groovy.grails.commons.ConfigurationHolder.config]}

        sealCountProject =
            new Project(name:'Seal Count',
                        description:'Counting seals',
                        status:EntityStatus.ACTIVE)

        Project tunaProject =
            new Project(name:'Tuna',
                        description:'Counting tuna',
                        status:EntityStatus.PENDING)

        Project whaleProject =
            new Project(name:'Whale',
                        description:'Whale counting',
                        status:EntityStatus.DEACTIVATED)

        def projectList = [sealCountProject, tunaProject, whaleProject]
        mockDomain(Project, projectList)
        projectList.each { it.save() }
        
        
        Address address =
            new Address(streetAddress:'12 Smith Street',
                        suburbTown:'Hobart',
                        state:'TAS',
                        country:'Australia',
                        postcode:'7000')
        

        Person somePerson = new Person()
        
        //
        // Organisations.
        //
        activeOrg = 
            new Organisation(name:'CSIRO', 
                             department:'CMAR',
                             phoneNumber:'1234',
                             faxNumber:'1234',
                             streetAddress:address,
                             postalAddress:address,
                             status:EntityStatus.ACTIVE)

        Organisation pendingOrg = 
            new Organisation(name:'IMOS', 
                             department:'CMAR',
                             phoneNumber:'1234',
                             faxNumber:'1234',
                             streetAddress:address,
                             postalAddress:address,
                             status:EntityStatus.PENDING)

        Organisation deactivatedOrg = 
            new Organisation(name:'SIMS', 
                             department:'CMAR',
                             phoneNumber:'1234',
                             faxNumber:'1234',
                             streetAddress:address,
                             postalAddress:address,
                             status:EntityStatus.DEACTIVATED)
                         
        def orgList = [activeOrg, pendingOrg, deactivatedOrg]
        mockDomain(Organisation, orgList)
        orgList.each { it.save() }
        
        Person jonBurgess =
            new Person(username:'jkburges',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'Jon Burgess',
                       organisation:activeOrg,
                       phoneNumber:'1234',
                       emailAddress:'jkburges@utas.edu.au',
                       status:EntityStatus.ACTIVE)

        joeBloggs =
            new Person(username:'jbloggs',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'Joe Bloggs',
                       organisation:activeOrg,
                       phoneNumber:'1234',
                       emailAddress:'jbloggs@blah.au',
                       status:EntityStatus.ACTIVE)

        Person johnCitizen =
            new Person(username:'jcitizen',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'John Citizen',
                       organisation:activeOrg,
                       phoneNumber:'5678',
                       emailAddress:'jcitizen@blah.au',
                       status:EntityStatus.PENDING)
                   
        def personList = [jonBurgess, joeBloggs, johnCitizen]
        mockDomain(Person, personList)
        personList.each { it.save() }
        
        ProjectRoleType piRoleType = new ProjectRoleType(displayName:"Principal Investigator")
        mockDomain(ProjectRoleType, [piRoleType])
        piRoleType.save()
        
        mockDomain(ProjectRole)
    }

    protected void tearDown() 
    {
        TestUtils.logout()
        super.tearDown()
    }

    void testListAsSysAdmin()
    {
        TestUtils.loginSysAdmin(this)
        
        def retVal = controller.list()
        assertEquals(3,
                     retVal.projectInstanceTotal)
    }
    
    void testListAsNonSysAdmin()
    {
        TestUtils.loginJoeBloggs(this)
        
        def retVal = controller.list()
        assertEquals(1, 
                     retVal.projectInstanceTotal)
        assertTrue(retVal.projectInstanceList.contains(sealCountProject))         
    }
    
    void testSaveAsSysAdmin()
    {
        TestUtils.loginSysAdmin(this)

        boolean mailSent = false
        
        controller.metaClass.sendCreationNotificationEmails =
        {
            mailSent = true
        }
        // Status should be set to ACTIVE and no mail sent.
        def cmd = new ProjectCreateCommand(
            name:'new project',
            organisation:activeOrg,
            person:joeBloggs,
            description:"test desc")
        mockForConstraintsTests(ProjectCreateCommand, [cmd])
        assertTrue(cmd.validate())
        
        def retVal = controller.save(cmd)
        
        assertEquals("show", redirectArgs['action'])
        assertEquals(EntityStatus.ACTIVE, Project.get(redirectArgs['id']).status) 
        assertFalse(mailSent)
    }
    
    void testSaveAsNonSysAdmin()
    {
        // Status should be set to PENDING and mail sent.
        TestUtils.loginJoeBloggs(this)

        boolean mailSent = false
        
        controller.metaClass.sendCreationNotificationEmails =
        {
            mailSent = true
        }
        
        def cmd = new ProjectCreateCommand(
            name:'new project',
            organisation:activeOrg,
            person:joeBloggs,
            description:"test desc")
        mockForConstraintsTests(ProjectCreateCommand, [cmd])
        assertTrue(cmd.validate())

        def retVal = controller.save(cmd)
        
        assertEquals("show", redirectArgs['action'])
        assertEquals(EntityStatus.PENDING, Project.get(redirectArgs['id']).status) 
        assertTrue(mailSent)
    }

	void testLookupByName()
	{
		assertLookupWithTerm(0, 'X')
		assertLookupWithTerm(1, 'S')
		assertLookupWithTerm(1, 's')
		assertLookupWithTerm(2, 'T')
		assertLookupWithTerm(2, 't')
		assertLookupWithTerm(3, 'A')
		assertLookupWithTerm(3, 'a')
	}

	private assertLookupWithTerm(expectedNumResults, term) 
	{
		controller.params.term = term
		controller.lookupByName()

		def jsonResponse = JSON.parse(controller.response.contentAsString)
		assertEquals(expectedNumResults, jsonResponse.size())
		
		// Need to reset the response so that this method can be called multiple times within a single test case.
		// Also requires workaround to avoid exception, see: http://jira.grails.org/browse/GRAILS-6483
		mockResponse?.committed = false // Current workaround
		reset()
	}
}