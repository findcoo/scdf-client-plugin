package com.github.findcoo.jenkins.plugin

import hudson.Extension
import jenkins.model.GlobalConfiguration
import jenkins.model.Jenkins
import org.kohsuke.stapler.DataBoundSetter

@Extension
class ScdfGlobalConfiguration() : GlobalConfiguration() {
  var servers: List<ScdfServerInfo> = listOf()
    set(value) {
      field = value
      save()
    }

  init {
    load()
  }

  companion object {
    fun get(): ScdfGlobalConfiguration {
      return Jenkins.get().getDescriptorByType(ScdfGlobalConfiguration::class.java)
    }
  }
}
