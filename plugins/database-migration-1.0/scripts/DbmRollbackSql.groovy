/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

includeTargets << new File("$databaseMigrationPluginDir/scripts/_DatabaseMigrationCommon.groovy")

target(dbmRollbackSql: 'Writes SQL to roll back the database to the state it was in when the tag was applied to STDOUT') {
    depends dbmInit

    String tag = argsList[0]
    if (!tag) {
        errorAndDie "The $hyphenatedScriptName script requires a tag"
    }

    if (!okToWrite(1)) return

    doAndClose {
        liquibase.rollback tag, contexts, newOutputStreamWriter(1)
    }
}

setDefaultTarget dbmRollbackSql