package com.github.findcoo.jenkins.plugin

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.domains.DomainRequirement
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.model.Item
import hudson.security.ACL
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.BuildStepMonitor
import hudson.tasks.Notifier
import hudson.tasks.Publisher
import hudson.util.ListBoxModel
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ScdfTaskNotifier
@DataBoundConstructor
constructor(
    private val servers: List<ScdfServerInfo> = listOf(),
    private val taskName: String,
    private val properties: String? = null,
    private val arguments: String? = null
) : Notifier() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ScdfTaskNotifier::class.java)
    }

    override fun perform(build: AbstractBuild<*, *>, launcher: Launcher, listener: BuildListener?): Boolean {
        if (credentialId != null) {
            val credential = CredentialsProvider.findCredentialById(credentialId, ScdfCredentials::class.java, build)
            ScdfTaskExecutor(listener!!.logger).executeTask(
                url,
                credential!!.clientId,
                credential.clientSecret,
                credential.scope,
                tokenUri,
                taskName,
                properties,
                arguments
            )
        }

        ScdfTaskExecutor(listener!!.logger).executeTask(
            url,
            null,
            null,
            null,
            tokenUri,
            taskName,
            properties,
            arguments
        )
        return true
    }

    override fun getRequiredMonitorService(): BuildStepMonitor = BuildStepMonitor.NONE

    @Extension
    class ScdfTaskNotifierDescriptor : BuildStepDescriptor<Publisher>() {

        companion object {
            private val log: Logger = LoggerFactory.getLogger(ScdfTaskNotifierDescriptor::class.java)
        }

        override fun getDisplayName(): String = "Execute SCDF task"

        override fun isApplicable(jobType: Class<out AbstractProject<*, *>>): Boolean = true

        fun doFillCredentialIdItems(@AncestorInPath owner: Item): ListBoxModel {
            return StandardListBoxModel()
                .includeMatchingAs(
                    ACL.SYSTEM,
                    owner,
                    StandardCredentials::class.java,
                    listOf<DomainRequirement>(),
                    CredentialsMatchers.instanceOf(ScdfCredentials::class.java)
                )
        }
    }
}