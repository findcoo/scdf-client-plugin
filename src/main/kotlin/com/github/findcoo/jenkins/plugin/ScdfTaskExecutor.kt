package com.github.findcoo.jenkins.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate
import org.springframework.cloud.dataflow.rest.util.HttpClientConfigurer
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import java.io.PrintStream
import java.net.URI

class ScdfTaskExecutor(private val log: PrintStream? = null) {

  companion object {
    private const val DEFAULT_REGISTRATION_ID = "default"
  }

  fun executeTask(
    url: String,
    clientId: String? = null,
    clientSecret: String? = null,
    scope: String? = null,
    tokenUri: String? = null,
    taskName: String,
    properties: String? = null,
    arguments: String? = null
  ) {
    val template = DataFlowTemplate.prepareRestTemplate(
      RestTemplateBuilder().messageConverters(
        MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().modules(KotlinModule()).build())
      ).build()
    )

    if (clientId != null) {
      val clientRegistrationRepository =
        clientRegistrationRepository(clientId, clientSecret!!, scope!!, tokenUri!!)
      template.interceptors.add(
        clientCredentialsTokenResolvingInterceptor(
          clientRegistrationRepository,
          clientId
        )
      )
    }

    val executionId = DataFlowTemplate(URI(url), template).taskOperations()
      .launch(taskName, parseProperties(properties), parseArguments(arguments), null)

    log?.println("Properties: ${parseProperties(properties)}")
    log?.println("Arguments: ${parseArguments(arguments)}")
    log?.println("Excution ID: ${executionId}")
  }

  private fun parseProperties(properties: String?): Map<String, String> {
    if (properties == null) return mapOf()
    return properties.split(",")
      .fold(mutableMapOf<String, String>()) { map: MutableMap<String, String>, it: String ->
        val propTuple = it.split("=")
        if (propTuple.size == 2) {
          map.set(propTuple[0], propTuple[1])
        }
        return map
      }
  }

  private fun parseArguments(arguments: String?): List<String>? {
    if (arguments == null) return listOf()
    return arguments.split(",")
  }

  private fun clientCredentialsTokenResolvingInterceptor(
    clientRegistrationRepository: ClientRegistrationRepository,
    clientId: String
  ): ClientHttpRequestInterceptor? {
    val principal = createAuthentication(clientId)
    val authorizedClientService: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(
      clientRegistrationRepository
    )
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository, authorizedClientService
    )
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials().build()
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    val authorizeRequest = OAuth2AuthorizeRequest
      .withClientRegistrationId(DEFAULT_REGISTRATION_ID).principal(principal).build()
    return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
      val authorizedClient = authorizedClientManager.authorize(authorizeRequest)
      request.headers.setBearerAuth(authorizedClient!!.accessToken.tokenValue)
      execution.execute(request, body!!)
    }
  }

  private fun createAuthentication(principalName: String): Authentication {
    return object : AbstractAuthenticationToken(null) {
      private val serialVersionUID = -2038812908189509872L
      override fun getCredentials(): Any {
        return ""
      }

      override fun getPrincipal(): Any {
        return principalName
      }
    }
  }

  private fun clientRegistrationRepository(
    clientId: String,
    clientSecret: String,
    scope: String,
    tokenUri: String
  ): InMemoryClientRegistrationRepository {
    val clientRegistration = ClientRegistration
      .withRegistrationId(DEFAULT_REGISTRATION_ID)
      .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
      .tokenUri(tokenUri)
      .clientId(clientId)
      .clientSecret(clientSecret)
      .scope(scope)
      .build()
    return InMemoryClientRegistrationRepository(clientRegistration)
  }
}