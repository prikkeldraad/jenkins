#!/usr/bin/env groovy
//package nl.prikkeldraad.jenkins

class VersionParser implements Serializable {
    public String number
    public String branch
    
    /**
    * Container to store the Jenkinsfile context
    * the context can be used to send information to the Jenkins build output
    */
    public context
    
    /**
    * @var String
    */
    private String _branch

    /**
    * @var String
    */
    private String _version

    /**
    * @var Int
    */
    private String _build
    
    /**
    * Assign version information
    * @param String branch
    * @param String version
    * @param Int build number
    */
    VersionParser(branch, version, build) {
        this._branch = branch
        this._version = version
        this._build = build
    }
    
    /**
    * Set branch prop and call parse_version
    */
    private void parse() {
        def matches = (this._branch =~ /^(master|release|develop|feature)\/?(.*)/)
        def feature = null

	// check if there is a Jira ticker number and replace - in the number with _ 
	// because else it would be part of the version numbering pattern
        try {
            feature = matches[0][2].replace("-", "_")
        } catch (java.lang.IndexOutOfBoundsException e) {
            void
        }    
        
	// check for branch names
        try {
            this.branch = matches[0][1];
        } catch (java.lang.NullPointerException | java.lang.IndexOutOfBoundsException e) {
            throw new java.lang.RuntimeException ("Cannot determine branch information, use release, develop or feature branch")
        }

        // reset matches, because it isn't serializable
        matches = null

        
        this.parse_version(feature)
    }

    /**
    * Output to Jenkins context or directly to screen
    * for testing we want a method to output to screen instead to at that time non-existing Jenkins
    */
    private void echo (str) {
        if (this.context) {
            this.context.echo str
        } else {
            print str
        }
    }
    
    /**
    * rebuild a version number
    * - remove snapshot from the name, if used
    * - check branch and add signature if not on master
    * @param String Jira issue number (optional)
    */
    private void parse_version(feature) {
        // check for SNAPSHOT in the name
        if (this._version.contains('-SNAPSHOT')) {
            // replace SNAPSHOT, we need real version numbers, not moving targets
            this._version = this._version.replace('-SNAPSHOT', '')
            this.echo("Replaced SNAPSHOT: ${this._version}")
        } else {
            this.echo("Clean version: ${this._version}")
        }
        
        switch (this.branch) {
		case 'master':
			this.number = "${this._version}-${this._build}"
			break

		case 'release':
			this.number = "${this._version}-beta-${this._build}"
			break
					
		case 'develop':
			this.number = "${this._version}-alpha-${this._build}"
			break
				
		case 'feature':
			this.number = "${this._version}-${feature}-${this._build}"
			break
        }
    }
    
    /**
    * Format version number
    *
    * %M - major number
    * %m - minor number
    * %p - patch number
    * %b - build number
    */    
    public String format(syntax) {
        def (version, build) = this.number.split("-")
        def (major, minor, patch) = version.tokenize(".")
        
        syntax = syntax.replace("%M", major)
        syntax = syntax.replace("%m", minor)
        syntax = syntax.replace("%p", patch)
        syntax = syntax.replace("%b", build)
        
        return syntax
    }    
}
