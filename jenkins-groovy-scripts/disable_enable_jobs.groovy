//
// Intygstjänster - disable or enable jobs with Groovy
//
// Run script here: https://build-inera.nordicmedtest.se/jenkins/script
//

import jenkins.model.*

// Pattern to search for. Regular expression.
def jobPattern = "intyg-common|intyg-infra|intyg-infra-spring5|intyg-intygstjanst|intyg-minaintyg|intyg-webcert|intyg-rehabstod|intyg-logsender|intyg-privatlakarportal|intyg-statistik|intyg-refdata|intyg-intygsbestallning|intyg-intygsadmin"

// Should we be disabling or enabling jobs? "disable" or "enable", case-insensitive.
def disableOrEnable = "disable"

def lcFlag = disableOrEnable.toLowerCase()

if (lcFlag.equals("disable") || lcFlag.equals("enable")) { 
    def matchedJobs = Jenkins.instance.items.findAll { job ->
        job.name =~ /$jobPattern/
    }

    matchedJobs.each { job ->
        if (lcFlag.equals("disable")) { 
            println "Disabling matching job ${job.name}"
            job.doDisable()
        } else if (lcFlag.equals("enable")) {
            println "Enabling matching job ${job.name}"
            job.doEnable()
        }
    }
} else {
    println "disableOrEnable parameter ${disableOrEnable} is not a valid option."
}
