dataSource {
    pooled = true
//    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
    dialect = 'org.hibernatespatial.postgis.PostgisDialect'
}
// environment specific settings
environments {
    development {
        dataSource {
//            dbCreate = "update" // one of 'create', 'create-drop','update'
//            url = "jdbc:hsqldb:file:devDB;shutdown=true"
//            url = "jdbc:hsqldb:mem:devDB"
            driverClassName = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:5432/aatams"
            username = "aatams"
            password = "aatams"
            
        }
    }
    test {
        dataSource {
//            dbCreate = "update"
            url = "jdbc:comp/env/jdbc/aatams_dev"
        }
    }
    production {
        dataSource {
//            dbCreate = "update"
            jndiName = "java:comp/env/jdbc/aatams_dev"
//            driverClassName = "org.postgresql.Driver"
        }
    }
}
