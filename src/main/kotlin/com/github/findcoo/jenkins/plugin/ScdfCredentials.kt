package com.github.findcoo.jenkins.plugin

import com.cloudbees.plugins.credentials.CredentialsDescriptor
import com.cloudbees.plugins.credentials.CredentialsNameProvider
import com.cloudbees.plugins.credentials.NameWith
import hudson.Extension
import org.kohsuke.stapler.DataBoundConstructor


@NameWith(value = ScdfCredentialProvider::class)
class ScdfCredentials

@DataBoundConstructor
constructor(
    val _credentialsId: String,
    val _description: String,
    val clientId: String,
    val clientSecret: String,
    val scope: String
) : com.cloudbees.plugins.credentials.impl.BaseStandardCredentials(_credentialsId, _description) {

    @Extension
    class Descriptor : CredentialsDescriptor() {

        override fun getDisplayName(): String {
            return "Spring cloud dataflow Oauth2 client credential"
        }
    }
}

class ScdfCredentialProvider : CredentialsNameProvider<ScdfCredentials>() {
    override fun getName(credentials: ScdfCredentials): String {
        return credentials._credentialsId
    }
}