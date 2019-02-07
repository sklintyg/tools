//
// Intygstjänster - reset Jenkins job build numbers with Groovy
// 
// Run script here: https://build-inera.nordicmedtest.se/jenkins/script
//

import jenkins.model.*

// Pattern to search for. Regular expression.
def jobPattern = "intyg-common|intyg-infra\$|intyg-intygstjanst|intyg-minaintyg|intyg-webcert|intyg-rehabstod|intyg-logsender|intyg-privatlakarportal|intyg-statistik"

def matchedJobs = Jenkins.instance.items.findAll { job ->
    job.name =~ /$jobPattern/
}

matchedJobs.each { job ->
    //THIS WILL REMOVE ALL BUILD HISTORY
    println "Removing all build history for job '${job.name}'"
    job.builds.each() { build ->
       println "... removing ${build}"
      build.delete()
    }
    job.updateNextBuildNumber(1)
}


