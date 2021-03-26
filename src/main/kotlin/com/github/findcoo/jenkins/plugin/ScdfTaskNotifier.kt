package com.github.findcoo.jenkins.plugin

import com.cloudbees.plugins.credentials.CredentialsProvider
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.BuildStepMonitor
import hudson.tasks.Notifier
import hudson.tasks.Publisher
import hudson.util.ListBoxModel
import org.kohsuke.stapler.DataBoundConstructor
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ScdfTaskNotifier
@DataBoundConstructor
constructor(
    val url: String,
    val taskName: String,
    val properties: String? = null,
    val arguments: String? = null
) : Notifier() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ScdfTaskNotifier::class.java)
    }

    override fun perform(build: AbstractBuild<*, *>, launcher: Launcher, listener: BuildListener?): Boolean {
        val server = ScdfGlobalConfiguration.get().servers.filter { it.url == url }.first()
        val credentialsId = server.credentialsId

        if (credentialsId != null) {
            val credential = CredentialsProvider.findCredentialById(credentialsId, ScdfCredentials::class.java, build)
            ScdfTaskExecutor(listener!!.logger).executeTask(
                url,
                credential!!.clientId,
                credential.clientSecret,
                credential.scope,
                server.tokenUri,
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
            server.tokenUri,
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

        fun doFillUrlItems(): ListBoxModel {
          val items = ListBoxModel()
          ScdfGlobalConfiguration.get().servers.forEach {
            items.add(it.url)
          }
          return items
        }
    }
}