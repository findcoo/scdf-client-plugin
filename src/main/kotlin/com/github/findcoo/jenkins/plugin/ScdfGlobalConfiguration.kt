package com.github.findcoo.jenkins.plugin

import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.domains.DomainRequirement
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Item
import hudson.security.ACL
import hudson.util.ListBoxModel
import jenkins.model.GlobalConfiguration
import jenkins.model.GlobalConfigurationCategory
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

@Extension
class ScdfGlobalConfiguration() : GlobalConfiguration() {
  var servers: List<ScdfServerInfo> = listOf()
    @DataBoundSetter
    set(value: List<ScdfServerInfo>) {
      field = value
    }

  init {
    this.load()
  }

  override fun getCategory(): GlobalConfigurationCategory {
    return GlobalConfigurationCategory.get(ScdfGlobalConfigurationCategory::class.java)
  }

  companion object

    fun get(): ScdfGlobalConfiguration {
      return Jenkins.get().getDescriptorByType(ScdfGlobalConfiguration::class.java)
    }
  }
}

class ScdfGlobalConfigurationCategory : GlobalConfigurationCategory() {
  override fun getDisplayName(): String {
    return "Spring cloud dataflow Client Configuration"
  }

  override fun getShortDescription(): String {
    return "Spring cloud dataflow Client Configuration"
  }
}


class ScdfServerInfo

@DataBoundConstructor
constructor(
  var url: String,
  var credentialId: String? = null,
  var tokenUri: String? = null
) : AbstractDescribableImpl<ScdfServerInfo>() {

  class DescriptorImpl : hudson.model.Descriptor<ScdfServerInfo>() {
    override fun getDisplayName(): String = "Spring cloud dataflow - Server configuration"

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