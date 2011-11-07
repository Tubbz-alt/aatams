package au.org.emii.aatams

import au.org.emii.aatams.util.ListUtils

class Organisation 
{
    static hasMany = [organisationProjects:OrganisationProject,
                      receivers:Receiver,
                      people:Person]
                  
    static transients = ['projects', 'totalReceivers']
    
    String name
    String department
    String phoneNumber
    String faxNumber
    Address streetAddress
    Address postalAddress
    EntityStatus status = EntityStatus.PENDING
    
    // The person requesting creation of Organisation.
//    Person requestingUser
    static hasOne = [request:Request]
    
    static constraints =
    {
        name(blank:false)
        department(blank:false)
        phoneNumber(blank:false)
        faxNumber(nullable:true)
        streetAddress()
        postalAddress()
        status()    // Default to PENDING
        organisationProjects()
        request(nullable:true)
//        requestingUser(nullable:true)
    }
    
    static mapping = 
    {
        status index:'organisation_status_index'
        sort "name"
    }
    
    static searchable = true
    
    String toString()
    {
        return name + " (" + department  + ")"
    }
    
    String getProjects()
    {
        return ListUtils.fold(organisationProjects, "project")
    }
    
    static List<Organisation> listActive()
    {
        return findAllByStatus(EntityStatus.ACTIVE)
    }
    
    Integer getTotalReceivers()
    {
        if (!receivers)
        {
            return 0
        }
        
        return receivers.size()
    }
}