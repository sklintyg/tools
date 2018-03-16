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

import groovy.xml.StreamingMarkupBuilder

class AnonymiseraEndastPersonnummerXmlForSRS {

    AnonymiseraConsistentPersonId anonymiseraPersonId
    AnonymiseraHsaId anonymiseraHsaId

    AnonymiseraEndastPersonnummerXmlForSRS(AnonymiseraConsistentPersonId anonymiseraPersonId, AnonymiseraHsaId anonymiseraHsaId) {
        this.anonymiseraPersonId = anonymiseraPersonId
        this.anonymiseraHsaId = anonymiseraHsaId
    }

    String anonymiseraIntygsXml(String s) {
        anonymiseraIntygsXml(s, null)
    }

    String anonymiseraIntygsXml(String s, String personId) {
        def slurper = new XmlSlurper()
        slurper.keepIgnorableWhitespace = true
        def intyg = slurper.parseText(s)
        intyg.declareNamespace(ns1: 'urn:riv:insuranceprocess:healthreporting:mu7263:3',
                               ns2: 'urn:riv:insuranceprocess:healthreporting:2',
                               ns3: 'urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3')
        anonymizeXml(intyg, personId)
        def outputBuilder = new StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        return (s.startsWith('<?xml') ? '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' : "") + outputBuilder.bind{  mkp.yield intyg }
    }

    private void anonymizeXml(def intyg, String personId) {
        intyg.'ns3:lakarutlatande'.'ns1:patient'.'ns2:person-id'.@extension = anonymiseraPersonId.anonymisera(personId)
        String personalId = intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:personal-id'.@extension
        intyg.'ns3:lakarutlatande'.'ns1:skapadAvHosPersonal'.'ns2:personal-id'.@extension = anonymiseraHsaId.anonymisera(personalId)
    }

    private void anonymizeNode(def node) {
        node?.replaceBody AnonymizeString.anonymize(node.toString())
    }

    private void anonymizeDateNode(def node) {
        node?.replaceBody anonymiseraDatum.anonymiseraDatum(node.toString())
    }
}
