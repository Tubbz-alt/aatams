package au.org.emii.aatams.data

import au.org.emii.aatams.*

import au.org.emii.aatams.detection.*
import au.org.emii.aatams.notification.*

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.ParseException
import com.vividsolutions.jts.io.WKTReader

import org.apache.shiro.crypto.hash.Sha256Hash
import org.joda.time.*
import org.joda.time.format.DateTimeFormat

import shiro.*

/**
 * Set up data used in development.
 * 
 * @author jburgess
 */
class DevelopmentDataInitialiser extends AbstractDataInitialiser
{
    DevelopmentDataInitialiser(def service)
    {
        super(service)
    }
    
    void execute()
    {
        initData()
    }
    
    def initData()
    {
        Notification receiverRecoveryCreate =
            new Notification(key:"RECEIVER_RECOVERY_CREATE",
                             htmlFragment:"Click here to create a receiver recovery",
                             anchorSelector:"td.rowButton > [href*='/receiverRecovery/create']:first").save(failOnError:true)

        Notification register =
            new Notification(key:"REGISTER",
                             htmlFragment:"Click here to register to user AATAMS",
                             anchorSelector:"#userlogin > [href\$='/person/create']",
                             unauthenticated:true).save(failOnError:true)
        
        //
        // Addresses.
        //
        Address csiroStreetAddress =
            new Address(streetAddress:'12 Smith Street',
                        suburbTown:'Hobart',
                        state:'TAS',
                        country:'Australia',
                        postcode:'7000').save()

        Address csiroPostalAddress =
            new Address(streetAddress:'34 Queen Street',
                        suburbTown:'Melbourne',
                        state:'VIC',
                        country:'Australia',
                        postcode:'3000').save()

        //
        // Organisations.
        //
        Organisation csiroOrg = 
            new Organisation(name:'CSIRO', 
                             department:'CMAR',
                             phoneNumber:'1234',
                             faxNumber:'1234',
                             streetAddress:csiroStreetAddress,
                             postalAddress:csiroPostalAddress,
                             status:EntityStatus.ACTIVE).save(failOnError: true)

        Address imosStreetAddress =
            new Address(streetAddress:'12 Smith Street',
                        suburbTown:'Hobart',
                        state:'TAS',
                        country:'Australia',
                        postcode:'7000').save()

        Address imosPostalAddress =
            new Address(streetAddress:'34 Queen Street',
                        suburbTown:'Melbourne',
                        state:'VIC',
                        country:'Australia',
                        postcode:'3000').save()

        Organisation imosOrg = 
            new Organisation(name:'IMOS', 
                             department:'eMII',
                             phoneNumber:'5678',
                             faxNumber:'5678',
                             streetAddress:imosStreetAddress,
                             postalAddress:imosPostalAddress,
                             status:EntityStatus.PENDING).save(failOnError: true)

        Address imosStreetAddress2 =
            new Address(streetAddress:'12 Smith Street',
                        suburbTown:'Hobart',
                        state:'TAS',
                        country:'Australia',
                        postcode:'7000').save()

        Address imosPostalAddress2 =
            new Address(streetAddress:'34 Queen Street',
                        suburbTown:'Melbourne',
                        state:'VIC',
                        country:'Australia',
                        postcode:'3000').save()

        Organisation imosOrg2 = 
            new Organisation(name:'IMOS 2', 
                             department:'AATAMS',
                             phoneNumber:'5678',
                             faxNumber:'5678',
                             streetAddress:imosStreetAddress2,
                             postalAddress:imosPostalAddress2,
                             status:EntityStatus.PENDING).save(failOnError: true)


        //
        // Projects.
        //
        Project sealCountProject =
            new Project(name:'Seal Count',
                        description:'Counting seals',
                        status:EntityStatus.ACTIVE).save(failOnError: true)

        Project tunaProject =
            new Project(name:'Tuna',
                        description:'Counting tuna',
                        status:EntityStatus.ACTIVE).save(failOnError: true)

        Project whaleProject =
            new Project(name:'Whale',
                        description:'Whale counting',
                        status:EntityStatus.ACTIVE).save(failOnError: true)

        OrganisationProject csiroSeals =
            new OrganisationProject(organisation:csiroOrg,
                                    project:sealCountProject)
                                
        sealCountProject.addToOrganisationProjects(csiroSeals)
        csiroOrg.addToOrganisationProjects(csiroSeals)
        sealCountProject.save(failOnError:true)
        csiroOrg.save(failOnError:true)
        
        OrganisationProject csiroTuna =
            new OrganisationProject(organisation:csiroOrg,
                                    project:tunaProject)
                                
        tunaProject.addToOrganisationProjects(csiroTuna)
        csiroOrg.addToOrganisationProjects(csiroTuna)
        tunaProject.save(failOnError:true)
        csiroOrg.save(failOnError:true)

        //
        // Security/people.
        //
        SecRole sysAdmin = new SecRole(name:"SysAdmin")
        sysAdmin.addToPermissions("*:*")
        sysAdmin.save(failOnError: true)
            
        //
        // People.
        //
        Person jonBurgess =
            new Person(username:'jkburges',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'Jon Burgess',
                       organisation:imosOrg,
                       phoneNumber:'1234',
                       emailAddress:'jkburges@utas.edu.au',
                       status:EntityStatus.ACTIVE,
                       defaultTimeZone:DateTimeZone.forID("Australia/Hobart"))
        jonBurgess.addToRoles(sysAdmin)
        jonBurgess.save(failOnError: true)

        Person joeBloggs =
            new Person(username:'jbloggs',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'Joe Bloggs',
                       organisation:csiroOrg,
                       phoneNumber:'1234',
                       emailAddress:'jbloggs@blah.au',
                       status:EntityStatus.ACTIVE,
                       defaultTimeZone:DateTimeZone.forID("Australia/Perth"))
                   
        Person johnCitizen =
            new Person(username:'jcitizen',
                       passwordHash:new Sha256Hash("password").toHex(),
                       name:'John Citizen',
                       organisation:csiroOrg,
                       phoneNumber:'5678',
                       emailAddress:'jcitizen@blah.au',
                       status:EntityStatus.ACTIVE,
                       defaultTimeZone:DateTimeZone.forID("Australia/Adelaide"))
        
        csiroOrg.addToPeople(joeBloggs)
        csiroOrg.addToPeople(johnCitizen)
        csiroOrg.save(failOnError:true)
        

        //
        // Project Roles.
        //
        ProjectRoleType principalInvestigator = ProjectRoleType.findByDisplayName(ProjectRoleType.PRINCIPAL_INVESTIGATOR)
        if (!principalInvestigator)
        {
            principalInvestigator =
                new ProjectRoleType(displayName:ProjectRoleType.PRINCIPAL_INVESTIGATOR).save(failOnError: true)
        }
        
        ProjectRoleType administrator = ProjectRoleType.findByDisplayName('Administrator')
        if (!administrator)
        {
            administrator =
                new ProjectRoleType(displayName:'Administrator').save(failOnError: true)
        }
        
        ProjectRole tunaAdmin =
            new ProjectRole(project:tunaProject,
                            person: joeBloggs,
                            roleType: administrator,
                            access:ProjectAccess.READ_WRITE)
        tunaProject.addToProjectRoles(tunaAdmin).save(failOnError:true)           
        joeBloggs.addToProjectRoles(tunaAdmin).save(failOnError:true, flush:true)   // flush required to keep compass happy
        permissionUtilsService.setPermissions(tunaAdmin)
        
        ProjectRole sealProjectInvestigator =
            new ProjectRole(project:sealCountProject,
                            person: joeBloggs,
                            roleType: principalInvestigator,
                            access:ProjectAccess.READ_WRITE)
                        
        sealCountProject.addToProjectRoles(sealProjectInvestigator).save(failOnError:true, flush:true)
        joeBloggs.addToProjectRoles(sealProjectInvestigator).save(failOnError:true, flush:true)
        permissionUtilsService.setPermissions(sealProjectInvestigator)

        ProjectRole sealAdmin =
            new ProjectRole(project:sealCountProject,
                            person: johnCitizen,
                            roleType: administrator,
                            access:ProjectAccess.READ_ONLY).save(failOnError: true, flush:true)
        permissionUtilsService.setPermissions(sealAdmin)

        ProjectRole tunaWrite =
            new ProjectRole(project:tunaProject,
                            person: johnCitizen,
                            roleType: administrator,
                            access:ProjectAccess.READ_WRITE).save(failOnError: true, flush:true)
        permissionUtilsService.setPermissions(tunaWrite)


        //
        // Devices.
        //
        DeviceManufacturer vemco = 
            new DeviceManufacturer(manufacturerName:'Vemco').save(failOnError: true)

        DeviceModel vemcoVR2 =
            new ReceiverDeviceModel(modelName:'VR2', manufacturer:vemco).save(failOnError: true)
        assert(!vemcoVR2.hasErrors())
        DeviceModel vemcoVR2W =
            new ReceiverDeviceModel(modelName:'VR2W', manufacturer:vemco).save(failOnError: true)
        
        DeviceModel vemcoV8 =
            new TagDeviceModel(modelName:'V8', manufacturer:vemco).save(failOnError: true)

        DeviceStatus newStatus = new DeviceStatus(status:'NEW').save(failOnError: true)
        DeviceStatus deployedStatus = new DeviceStatus(status:'DEPLOYED').save(failOnError: true)
        DeviceStatus recoveredStatus = new DeviceStatus(status:'RECOVERED').save(failOnError: true)

        Receiver rx1 =
            new Receiver(codeName:'VR2W-101336',
                         serialNumber:'12345678',
                         status:deployedStatus,
                         model:vemcoVR2,
                         organisation:csiroOrg,
                         comment:'RX 1 belonging to CSIRO').save(failOnError: true)
        
        Receiver rx2 =
            new Receiver(codeName:'VR2W-101337',
                         serialNumber:'87654321',
                         status:deployedStatus,
                         model:vemcoVR2,
                         organisation:csiroOrg).save(failOnError: true)

        Receiver rx3 =
            new Receiver(codeName:'VR2W-101338',
                         serialNumber:'1111r',
                         status:newStatus,
                         model:vemcoVR2,
                         organisation:imosOrg).save(failOnError: true)
                     
        Receiver rx4 =
            new Receiver(codeName:'VR2W-101344',
                         serialNumber:'4444r',
                         status:newStatus,
                         model:vemcoVR2,
                         organisation:imosOrg).save(failOnError: true)
                     
        Receiver rx5 =
            new Receiver(codeName:'VR2W-101355',
                         serialNumber:'5555r',
                         status:newStatus,
                         model:vemcoVR2,
                         organisation:imosOrg).save(failOnError: true)

        Receiver rx6 =
            new Receiver(codeName:'VR2W-103355',
                         serialNumber:'103355',
                         status:newStatus,
                         model:vemcoVR2W,
                         organisation:imosOrg).save(failOnError: true)
         
		// CodeMaps.
		createCodeMaps()
			
		def a69_1303 = CodeMap.findByCodeMap('A69-1303')	
			 				             
        //
        // Tags.
        //
        TransmitterType pinger =
            new TransmitterType(transmitterTypeName:"PINGER").save(failOnError:true)
        Tag tag1 =
            new Tag(codeName:'A69-1303-62339',
                    serialNumber:'62339',
                    codeMap:a69_1303,
                    pingCode:'62339',
                    model:vemcoV8,
                    project:sealCountProject,
                    status:deployedStatus,
                    transmitterType:pinger)
		a69_1303.addToTags(tag1)
		

        Tag tag2 =
            new Tag(codeName:'A69-1303-46601',
                    serialNumber:'46601',
                    codeMap:a69_1303,
                    pingCode:'46601',
                    model:vemcoV8,
                    project:sealCountProject,
                    status:deployedStatus,
                    transmitterType:pinger)
		a69_1303.addToTags(tag2)
					
        Tag tag3 =
            new Tag(codeName:'A69-1303-11111',
                    serialNumber:'1111',
                    codeMap:a69_1303,
                    pingCode:'11111',
                    model:vemcoV8,
                    project:sealCountProject,
                    status:newStatus,
                    transmitterType:pinger)
		a69_1303.addToTags(tag3)
			
        // Bug #352 - this tag won't be selectable if animal release project
        // set to "tuna".
        Tag tag5 =
            new Tag(codeName:'A69-1303-33333',
                    serialNumber:'3333',
                    codeMap:CodeMap.findByCodeMap('A69-1303'),
                    pingCode:'3333',
                    model:vemcoV8,
                    project:tunaProject,
                    status:newStatus,
                    transmitterType:pinger,
                    expectedLifeTimeDays:100)
		a69_1303.addToTags(tag5)
					
        Tag tag6 =
            new Tag(codeName:'A69-1303-44444',
                    serialNumber:'4444',
                    codeMap:a69_1303,
                    pingCode:'4444',
                    model:vemcoV8,
                    project:tunaProject,
                    status:newStatus,
                    transmitterType:pinger)
		a69_1303.addToTags(tag6)
			
        Tag orphanTag =
            new Tag(codeName:'A69-1303-55555',
                    serialNumber:'5555',
                    codeMap:a69_1303,
                    pingCode:'5555',
                    model:vemcoV8,
                    status:newStatus,
                    transmitterType:pinger)
		a69_1303.addToTags(orphanTag)
		a69_1303.save(failOnError:true)
			
        TransmitterType depth =
            new TransmitterType(transmitterTypeName:"DEPTH").save(failOnError:true)
        TransmitterType temp =
            new TransmitterType(transmitterTypeName:"TEMP").save(failOnError:true)

		def a69_1105 = CodeMap.findByCodeMap('A69-1303')	
        Sensor sensor1 =
            new Sensor(codeName:'A69-1105-64000',
                    serialNumber:'64000',
                    codeMap:a69_1105,
                    pingCode:'64000',
                    model:vemcoV8,
                    project:sealCountProject,
                    status:newStatus,
                    tag:tag1,
                    transmitterType:depth,
                    unit:'m',
                    slope:1,
                    intercept:0)
		a69_1105.addToTags(sensor1)
		
        Sensor sensor2 =
            new Sensor(codeName:'A69-1105-65000',
                    serialNumber:'65000',
                    codeMap:a69_1105,
                    pingCode:'65000',
                    model:vemcoV8,
                    project:sealCountProject,
                    status:newStatus,
                    tag:tag1,
                    transmitterType:temp,
                    unit:'k',
                    slope:1,
                    intercept:0)
		a69_1105.addToTags(sensor2)
		a69_1105.save(failOnError:true)
			
        //
        // Installation data.
        //
        InstallationConfiguration array =
            new InstallationConfiguration(type:'ARRAY').save(failOnError:true)
        InstallationConfiguration curtain =
            new InstallationConfiguration(type:'CURTAIN').save(failOnError:true)

        Installation bondiLine =
            new Installation(name:'Bondi Line',
                             configuration:curtain,
                             project:sealCountProject).save(failOnError:true)
                         
        Installation ningalooArray =
            new Installation(name:'Ningaloo Array',
                             configuration:array,
                             project:tunaProject).save(failOnError:true)
                         
        Installation heronCurtain =
            new Installation(name:'Heron Island Curtain',
                             configuration:curtain,
                             project:sealCountProject).save(failOnError:true)

        WKTReader reader = new WKTReader();

        Point location = (Point)reader.read("POINT(30.1234 30.1234)")
        location.setSRID(4326)
        
        InstallationStation bondiSW1 = 
            new InstallationStation(installation:bondiLine,
                                    name:'Bondi SW1',
                                    curtainPosition:1,
                                    location:location).save(failOnError:true)

        InstallationStation bondiSW2 = 
            new InstallationStation(installation:bondiLine,
                                    name:'Bondi SW2',
                                    curtainPosition:2,
                                    location:(Point)reader.read("POINT(-10.1234 -10.1234)")).save(failOnError:true)

        InstallationStation bondiSW3 = 
            new InstallationStation(installation:bondiLine,
                                    name:'Bondi SW3',
                                    curtainPosition:3,
                                    location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)
                                
        InstallationStation ningalooS1 = 
            new InstallationStation(installation:ningalooArray,
                                    name:'Ningaloo S1',
                                    curtainPosition:1,
                                    location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)

        InstallationStation ningalooS2 = 
            new InstallationStation(installation:ningalooArray,
                                    name:'Ningaloo S2',
                                    curtainPosition:2,
                                    location:(Point)reader.read("POINT(20.1234 20.1234)")).save(failOnError:true)

        InstallationStation heronS1 =
            new InstallationStation(installation:heronCurtain,
                                    name:'Heron S1',
                                    curtainPosition:1,
                                    location:(Point)reader.read("POINT(12.34 -42.30)")).save(failOnError:true)

        InstallationStation heronS2 =
            new InstallationStation(installation:heronCurtain,
                                    name:'Heron S2',
                                    curtainPosition:2,
                                    location:(Point)reader.read("POINT(76.02 -20.1234)")).save(failOnError:true)
        

        //
        //  Receiver Deployments.
        //
        MooringType concreteMooring = new MooringType(type:'CONCRETE BLOCK').save(failOnError:true)

        ReceiverDeployment rx1Bondi =
            new ReceiverDeployment(station:bondiSW1,
                                   receiver:rx1,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2010-02-15T12:34:56+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:12f,
                                   depthBelowSurfaceM:5f,
                                   receiverOrientation:ReceiverOrientation.UP,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)

        ReceiverDeployment rx2Bondi =
            new ReceiverDeployment(station:bondiSW2,
                                   receiver:rx2,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2011-05-15T14:12:00+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:16f,
                                   depthBelowSurfaceM:7.4f,
                                   receiverOrientation:ReceiverOrientation.DOWN,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(20.1234 20.1234)")).save(failOnError:true)

        ReceiverDeployment rx3Ningaloo =
            new ReceiverDeployment(station:ningalooS1,
                                   receiver:rx3,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2011-05-15T12:34:56+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:12f,
                                   depthBelowSurfaceM:5f,
                                   receiverOrientation:ReceiverOrientation.UP,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)
                               
        ReceiverDeployment rx4Heron =
            new ReceiverDeployment(station:heronS1,
                                   receiver:rx4,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2011-05-15T12:34:56+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:12f,
                                   depthBelowSurfaceM:5f,
                                   receiverOrientation:ReceiverOrientation.UP,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(10.1234 10.1234)"),
                                   comments:"This was fun to deploy").save(failOnError:true)

        ReceiverDeployment rx5Heron =
            new ReceiverDeployment(station:heronS2,
                                   receiver:rx5,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2011-05-15T12:34:56+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:12f,
                                   depthBelowSurfaceM:5f,
                                   receiverOrientation:ReceiverOrientation.UP,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)

        ReceiverDeployment rx6Heron =
            new ReceiverDeployment(station:heronS2,
                                   receiver:rx6,
                                   deploymentNumber:1,
                                   deploymentDateTime:new DateTime("2000-05-15T12:34:56+10:00"),
                                   acousticReleaseID:"asdf",
                                   mooringType:concreteMooring,
                                   bottomDepthM:12f,
                                   depthBelowSurfaceM:5f,
                                   receiverOrientation:ReceiverOrientation.UP,
                                   batteryLifeDays:90,
                                   location:(Point)reader.read("POINT(10.1234 10.1234)")).save(failOnError:true)
        
        //
        // Animals and Animal Releases etc.
        //
        CaabSpecies whiteShark = new CaabSpecies(scientificName:"Carcharodon carcharias", commonName:"White Shark", spcode:"37010003").save(failOnError:true)
        CaabSpecies blueFinTuna = new CaabSpecies(scientificName:"Thunnus maccoyii", commonName:"Southern Bluefin Tuna", spcode:"37441004").save(failOnError:true)
        CaabSpecies blueEyeTrevalla = new CaabSpecies(scientificName:"Hyperoglyphe antarctica", commonName:"Blue-eye Trevalla", spcode:"37445001").save(failOnError:true)

        Sex male = new Sex(sex:'MALE').save(failOnError:true)
        Sex female = new Sex(sex:'FEMALE').save(failOnError:true)

        Animal whiteShark1 = new Animal(species:whiteShark,
                                        sex:male).save(failOnError:true)
        Animal whiteShark2 = new Animal(species:whiteShark,
                                        sex:male).save(failOnError:true)
        Animal blueFinTuna1 = new Animal(species:blueFinTuna,
                                         sex:female).save(failOnError:true)

        AnimalMeasurementType length = new AnimalMeasurementType(type:'LENGTH').save(failOnError:true)
        AnimalMeasurementType weight = new AnimalMeasurementType(type:'WEIGHT').save(failOnError:true)
        MeasurementUnit metres = new MeasurementUnit(unit:'m').save(failOnError:true)
        MeasurementUnit kg = new MeasurementUnit(unit:'kg').save(failOnError:true)

        SurgeryTreatmentType antibiotic = new SurgeryTreatmentType(type:'ANTIBIOTIC').save(failOnError:true)
        SurgeryTreatmentType anesthetic = new SurgeryTreatmentType(type:'ANESTHETIC').save(failOnError:true)
        SurgeryType internal = new SurgeryType(type:'INTERNAL').save(failOnError:true)
        SurgeryType external = new SurgeryType(type:'EXTERNAL').save(failOnError:true)

        CaptureMethod net = new CaptureMethod(name:'NET').save(failOnError:true)
        CaptureMethod line = new CaptureMethod(name:'LINE').save(failOnError:true)
        CaptureMethod longLine = new CaptureMethod(name:'LONG LINE').save(failOnError:true)

        AnimalRelease whiteShark1Release =
            new AnimalRelease(project:tunaProject,
                              surgeries:[],
                              measurements:[],
                              animal:whiteShark1,
                              captureLocality:'Neptune Islands',
                              captureLocation:(Point)reader.read("POINT(10.1234 20.1234)"),
                              captureDateTime:new DateTime("2011-05-15T14:10:00"),
                              captureMethod:net,
                              releaseLocality:'Neptune Islands',
                              releaseLocation:(Point)reader.read("POINT(30.1234 40.1234)"),
                              releaseDateTime:new DateTime("2011-05-15T14:15:00"),
                              embargoDate:Date.parse("yyyy-MM-dd hh:mm:ss", "2015-05-15 12:34:56")).save(failOnError:true)

        AnimalRelease whiteShark2Release =
            new AnimalRelease(project:tunaProject,
                              surgeries:[],
                              measurements:[],
                              animal:whiteShark2,
                              captureLocality:'Neptune Islands',
                              captureLocation:(Point)reader.read("POINT(10.1234 20.1234)"),
                              captureDateTime:new DateTime("2011-05-15T14:10:00"),
                              captureMethod:net,
                              releaseLocality:'Neptune Islands',
                              releaseLocation:(Point)reader.read("POINT(30.1234 40.1234)"),
                              releaseDateTime:new DateTime("2011-05-15T14:15:00"),
                              embargoDate:Date.parse("yyyy-MM-dd hh:mm:ss", "2015-05-15 12:34:56")).save(failOnError:true)
                          
        AnimalRelease blueFinTuna1Release =
            new AnimalRelease(project:tunaProject,
                              surgeries:[],
                              measurements:[],
                              animal:blueFinTuna1,
                              captureLocality:'Neptune Islands',
                              captureLocation:(Point)reader.read("POINT(10.1234 20.1234)"),
                              captureDateTime:new DateTime("2011-05-15T14:10:00"),
                              captureMethod:net,
                              releaseLocality:'Neptune Islands',
                              releaseLocation:(Point)reader.read("POINT(30.1234 40.1234)"),
                              releaseDateTime:new DateTime("2011-05-15T14:15:00")).save(failOnError:true)
                          
        AnimalMeasurement whiteShark1Length = 
            new AnimalMeasurement(release:whiteShark1Release,
                                  type:length,
                                  value:2.5f,
                                  unit:metres,
                                  estimate:false).save(failOnError:true)

        AnimalMeasurement whiteShark1Weight = 
            new AnimalMeasurement(release:whiteShark1Release,
                                  type:weight,
                                  value:200f,
                                  unit:kg,
                                  estimate:true).save(failOnError:true)


        Surgery surgery1 = 
            new Surgery(release:whiteShark1Release,
                        tag:tag1,
                        timestamp:new DateTime("2011-05-15T14:12:00"),
                        type:external,
                        treatmentType:antibiotic)
        tag1.addToSurgeries(surgery1).save(failOnError:true)
        whiteShark1Release.addToSurgeries(surgery1).save(failOnError:true)

        Surgery surgery2 = 
            new Surgery(release:whiteShark1Release,
                        tag:tag2,
                        timestamp:new DateTime("2011-05-15T14:13:00"),
                        type:external,
                        treatmentType:antibiotic)
        tag2.addToSurgeries(surgery2).save(failOnError:true)
        whiteShark1Release.addToSurgeries(surgery2).save(failOnError:true)
        
        Surgery surgery3 = 
            new Surgery(release:whiteShark2Release,
                        tag:tag1,   // Can't really have a tag on two different animals.
                        timestamp:new DateTime("2011-05-15T14:12:00"),
                        type:external,
                        treatmentType:antibiotic)
        tag1.addToSurgeries(surgery3).save(failOnError:true)
        whiteShark2Release.addToSurgeries(surgery3).save(failOnError:true)
        
        Surgery surgery4 = 
            new Surgery(release:blueFinTuna1Release,
                        tag:tag1,   // Can't really have a tag on two different animals.
                        timestamp:new DateTime("2011-05-15T14:12:00"),
                        type:external,
                        treatmentType:antibiotic)
        tag1.addToSurgeries(surgery4).save(failOnError:true)
        blueFinTuna1Release.addToSurgeries(surgery4).save(failOnError:true)
        
        // Receiver Recovery.
        ReceiverRecovery recovery1 =
            new ReceiverRecovery(recoveryDateTime: new DateTime("2013-07-25T12:34:56"),
                                 location:(Point)reader.read("POINT(10.1234 10.1234)"),
                                 status:recoveredStatus,
                                 recoverer:sealProjectInvestigator,
                                 deployment:rx1Bondi,
                                 batteryLife:12.5f,
                                 batteryVoltage:3.7f).save(failOnError:true)

        ReceiverRecovery recovery2 =
            new ReceiverRecovery(recoveryDateTime: new DateTime("2013-05-17T12:54:56"),
                                 location:(Point)reader.read("POINT(20.1234 20.1234)"),
                                 status:recoveredStatus,
                                 recoverer:sealProjectInvestigator,
                                 deployment:rx2Bondi,
                                 batteryLife:12.5f,
                                 batteryVoltage:3.7f).save(failOnError:true)
                             
        ReceiverRecovery recovery3 =
            new ReceiverRecovery(recoveryDateTime: new DateTime("2023-05-17T12:54:56"),
                                 location:(Point)reader.read("POINT(20.1234 20.1234)"),
                                 status:recoveredStatus,
                                 recoverer:sealProjectInvestigator,
                                 deployment:rx6Heron,
                                 batteryLife:12.5f,
                                 batteryVoltage:3.7f).save(failOnError:true)
                             
        createExportWithDetections("export1.csv", jonBurgess, rx1Bondi, rx1, tag1, surgery1, 10)
        createExportWithDetections("export3.csv", jonBurgess, rx2Bondi, rx2, tag2, surgery4, 3)
		createExportWithDetections("export4.csv", jonBurgess, rx3Ningaloo, rx3, tag3, surgery4, 3)
		createExportWithDetections("export5.csv", jonBurgess, rx4Heron, rx4, tag5, null, 3)
		
        ReceiverDownloadFile export2 = 
            new ReceiverDownloadFile(type:ReceiverDownloadFileType.DETECTIONS_CSV,
                                     path:"/tmp/export2.csv",
                                     name:"export2.csv",
                                     importDate:new DateTime("2013-05-17T12:54:56").toDate(),
                                     status:FileProcessingStatus.PROCESSED,
                                     errMsg:"",
                                     requestingUser:jonBurgess).save(failOnError:true)
                                 
        10.times
        {
            ReceiverEvent event =
                new ReceiverEvent(timestamp:new Date(),
                                  receiverDeployment:rx2Bondi,
                                  description:"desc",
                                  data:"123",
                                  unit:"m")
                              
            export2.addToEvents(event)
        }
        export2.save(failOnError:true)
    }

	private void createExportWithDetections(String exportName, Person uploader, ReceiverDeployment deployment, Receiver receiver, Tag tag, Surgery surgery, int numDetections)
	{
        ReceiverDownloadFile export = 
            new ReceiverDownloadFile(type:ReceiverDownloadFileType.DETECTIONS_CSV,
                                     path:"/tmp/export.csv",
                                     name:exportName,
                                     importDate:new DateTime("2013-05-17T12:54:56").toDate(),
                                     status:FileProcessingStatus.PROCESSED,
                                     errMsg:"",
                                     requestingUser:uploader).save(failOnError:true)
                                 
        createDetections(deployment, receiver, tag, export, surgery, numDetections)
        export.save(failOnError:true)
	}
	
	private void createDetections(ReceiverDeployment rx1Bondi, Receiver rx1, Tag tag1, ReceiverDownloadFile export1, Surgery surgery1, int numDetections) 
	{
		numDetections.times
		{
			ValidDetection detection =
					new ValidDetection(receiverDeployment:rx1Bondi,
					timestamp:new DateTime("2011-05-17T12:54:00").plusSeconds(it).toDate(),
					receiverName:rx1.codeName,
					transmitterId:tag1.codeName,
					receiverDownload:export1)

			if (surgery1)
			{	
				DetectionSurgery detSurgery =
						new DetectionSurgery(surgery:surgery1,
						detection:detection,
						tag:tag1)
				detection.addToDetectionSurgeries(detSurgery)
			}
			export1.addToDetections(detection)
		}
	}
	
	private void createCodeMaps()
	{
		["A69", "A180"].each
		{
			freq ->
			
			["1303", "1601", "9001", "9003", "1206", "1105", "9002", "9004"].each
			{
				codeSpace ->
				
				CodeMap codeMap = new CodeMap(codeMap:freq + "-" + codeSpace).save(failOnError:true, flush:true)
			}
		}
	}
}
