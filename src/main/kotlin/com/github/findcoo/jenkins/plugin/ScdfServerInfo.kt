package com.github.findcoo.jenkins.plugin

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.domains.DomainRequirement
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Item
import hudson.security.ACL
import hudson.util.ListBoxModel
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import hudson.model.Descriptor
import jenkins.model.Jenkins

import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder

import hudson.model.queue.Tasks
import hudson.model.Queue
import org.kohsuke.stapler.QueryParameter


class ScdfServerInfo

@DataBoundConstructor
constructor(
  var url: String,
  var credentialsId: String? = null,
  var tokenUri: String? = null,
  var scope: String? = null
) : AbstractDescribableImpl<ScdfServerInfo>() {

  @Extension
  class DescriptorImpl : Descriptor<ScdfServerInfo>() {
    override fun getDisplayName(): String = "Spring cloud dataflow - Server configuration"

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
