package com.github.findcoo.jenkins.plugin

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import hudson.Extension
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.model.Item
import hudson.model.queue.Tasks
import hudson.security.ACL
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.BuildStepMonitor
import hudson.tasks.Notifier
import hudson.tasks.Publisher
import hudson.util.ListBoxModel
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.QueryParameter
import jenkins.model.Jenkins
import hudson.model.Queue


class ScdfTaskNotifier
@DataBoundConstructor
constructor(
  val url: String,
  var tokenUri: String? = null,
  var credentialsId: String? = null,
  val taskName: String,
  val properties: String? = null,
  val arguments: String? = null
) : Notifier() {

  override fun perform(build: AbstractBuild<*, *>, launcher: Launcher, listener: BuildListener?): Boolean {
    val credentials = credentialsId?.let {
      CredentialsProvider.findCredentialById(it, ScdfCredentials::class.java, build)
    }

    ScdfTaskExecutor(listener!!.logger).executeTask(
      url,
      credentials?.clientId,
      credentials?.clientSecret,
      credentials?.scope,
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

    override fun getDisplayName(): String = "Execute SCDF task"

    override fun isApplicable(jobType: Class<out AbstractProject<*, *>>): Boolean = true

    fun doFillCredentialsIdItems(@AncestorInPath item: Item?, @QueryParameter credentialsId: String, @QueryParameter uri: String?): ListBoxModel {
      val result = StandardListBoxModel()
      if (item == null) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
          return result.includeCurrentValue(credentialsId)
        }
      } else {
        if (!item.hasPermission(Item.EXTENDED_READ)
          && !item.hasPermission(CredentialsProvider.USE_ITEM)
        ) {
          return result.includeCurrentValue(credentialsId)
        }
      }
      return result //
        .includeEmptyValue() //
        .includeMatchingAs(
          if (item is Queue.Task) Tasks.getAuthenticationOf(item as Queue.Task) else ACL.SYSTEM,
          item,
          StandardCredentials::class.java,
          URIRequirementBuilder.fromUri(uri).build(),
          CredentialsMatchers.anyOf(
            CredentialsMatchers.instanceOf(ScdfCredentials::class.java)
          )
        )
        .includeCurrentValue(credentialsId)
    }
  }
}