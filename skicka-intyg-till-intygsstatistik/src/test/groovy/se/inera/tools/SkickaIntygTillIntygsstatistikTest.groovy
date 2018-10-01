package se.inera.intyg.tools

import se.inera.intyg.tools.SkickaIntygTillIntygsstatistik
import org.junit.*
import static groovy.test.GroovyAssert.*

class SkickaIntygTillIntygsstatistikTest extends GroovyTestCase {


    @Test
    public void testParseArgumentsWithoutLoadingPropertiesFromFile() {
        def testee = new SkickaIntygTillIntygsstatistik()

        String[] args = [
            'DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"',
            'DB_DRIVER="com.mysql.jdbc.Driver"',
            'DB_USERNAME="abcde"',
            'DB_PASSWORD="12345"',
            'DEBUG="true"']

        def result = testee.parseArguments(args)
        assertEquals 'abcde', result.db.username
        assertEquals '12345', result.db.password
        assertEquals 'com.mysql.jdbc.Driver', result.db.driver
        assertEquals 'jdbc:mysql://localhost:3306/intyg?useCompression=true', result.db.url

        assertTrue result.DB_URL.isEmpty()
    }

    public void testParseArgumentsWhenOnlyDefaultPropertiesAreLoaded() {
        def testee = new SkickaIntygTillIntygsstatistik("app.properties")

        String[] args = []

        def result = testee.parseArguments(args)
        //assertEquals 'sa', result.db.username
        //assertEquals '', result.db.password
        //assertEquals 'org.h2.Driver', result.db.driver
        //assertEquals 'jdbc:h2:mem:dataSource', result.db.url
    }

    public void testParseArguments() {
        def testee = new SkickaIntygTillIntygsstatistik("app.properties")

        String[] args = [
            'DB_URL="jdbc:mysql://localhost:3306/intyg?useCompression=true"',
            'DB_DRIVER="com.mysql.jdbc.Driver"',
            'DB_USERNAME="abcde"',
            'DB_PASSWORD="12345"',
            'DEBUG="true"']

        def result = testee.parseArguments(args)
        assertEquals 'abcde', result.db.username
        assertEquals '12345', result.db.password
        assertEquals 'com.mysql.jdbc.Driver', result.db.driver
        assertEquals 'jdbc:mysql://localhost:3306/intyg?useCompression=true', result.db.url

        assertTrue result.DB_URL.isEmpty()
    }

    @Test
    public void testIfStringIsAllUpperCase() {
        def testee = new SkickaIntygTillIntygsstatistik()
        assertTrue testee.isUpperCase('DB_USERNAME')
        assertTrue testee.isUpperCase('INTYGSTYPER')
        assertFalse testee.isUpperCase('camleCase')
    }

    @Test
    public void testShVariableToDotNotation() {
        def testee = new SkickaIntygTillIntygsstatistik()
        assertEquals 'db.username', testee.shVariableToDotNotation('DB_USERNAME')
        assertEquals 'intygstyper', testee.shVariableToDotNotation('INTYGSTYPER')
        assertEquals 'intygsTyper', testee.shVariableToDotNotation('intygsTyper')
        assertEquals 'INTYGSTYPEr', testee.shVariableToDotNotation('INTYGSTYPEr')
    }

    @Test
    public void testIsNullOrEmpty() {
        def testee = new SkickaIntygTillIntygsstatistik()
        assertTrue testee.isNullOrEmpty(null)
        assertTrue testee.isNullOrEmpty('')
        assertTrue testee.isNullOrEmpty(' ')
        assertFalse testee.isNullOrEmpty('groovy')
    }

}