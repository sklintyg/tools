/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.tools.anonymisering

import org.junit.Test

import static junit.framework.Assert.assertEquals
import se.inera.intyg.schemas.contract.util.*
import java.security.MessageDigest

class AnonymiseraConsistentPersonIdTest {

    AnonymiseraConsistentPersonId anonymiseraConsistentPersonId = new AnonymiseraConsistentPersonId()

    @Test
    void testDetermineSexFromPatientId() {
        assertEquals("male", anonymiseraConsistentPersonId.determineSexFromPersonnummer("19121212-1212"))
        assertEquals("female", anonymiseraConsistentPersonId.determineSexFromPersonnummer("19121212-1202"))
    }

    @Test
    void testDetermineAgeFromPatientId() {
        assertEquals("31", anonymiseraConsistentPersonId.determineAgeFromPersonnummer("19851123-1212"))
    }

    @Test
    void testAnonymisera() {
        String persNummer = "19121212-1212"
        String expectedPersNummer = "c57913dd217a462baf479ec80c0f4ce93494dbc71df41ee1b6e746393cfced7a"
        assertEquals(expectedPersNummer, anonymiseraConsistentPersonId.anonymisera("19121212-1212"))

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(persNummer.getBytes("UTF-8"))
        byte[] digest = md.digest()
        String actualHash = String.format("%064x", new BigInteger(1, digest))

        assertEquals(actualHash, expectedPersNummer)
    }
}
