package io.armory.plugin.example.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.KubernetesV2CachingAgentFactory
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesApiGroup
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesApiVersion
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesKind
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesManifest
import com.netflix.spinnaker.clouddriver.kubernetes.model.Manifest
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesV2Credentials
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import io.mockk.mockk
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSuccess

class MyCRDHandlerTest : JUnit5Minutests {

  fun tests() = rootContext<Fixture> {
    fixture {
      Fixture()
    }

    test("marks a MyCRD manifest with status ready=true as stable") {
      val manifest = KubernetesManifest().apply {
        apiVersion = KubernetesApiVersion.fromString("armory.io/v1alpha1")
        kind = KubernetesKind.from("MyCRD", KubernetesApiGroup.fromString("armory.io"))
        put("status", mapOf(
            "ready" to true
        ))
      }

      expectThat(subject.status(manifest)).isA<Manifest.Status>().and {
        get { stable.isState }.isEqualTo(true)
      }
    }

    test("marks a MyCRD manifest with status ready=false as unstable") {
      val manifest = KubernetesManifest().apply {
        apiVersion = KubernetesApiVersion.fromString("armory.io/v1alpha1")
        kind = KubernetesKind.from("MyCRD", KubernetesApiGroup.fromString("armory.io"))
        put("status", mapOf(
            "ready" to false
        ))
      }

      expectThat(subject.status(manifest)).isA<Manifest.Status>().and {
        get { stable.isState }.isEqualTo(false)
      }
    }

    test("marks a MyCRD manifest without a status as unstable") {
      val manifest = KubernetesManifest().apply {
        apiVersion = KubernetesApiVersion.fromString("my.namespace/v1alpha1")
        kind = KubernetesKind.from("MyCRD", KubernetesApiGroup.fromString("armory.io"))
      }

      expectThat(subject.status(manifest)).isA<Manifest.Status>().and {
        get { stable.isState }.isEqualTo(false)
      }
    }

    test("builds a caching agent factory") {
      expectCatching { subject.buildCachingAgent(creds, mapper, registry, 1, 1, 1L) }
          .isSuccess()
    }
  }

  private class Fixture {
    val subject = MyCRDHandler()
    val creds: KubernetesNamedAccountCredentials<KubernetesV2Credentials> = mockk(relaxed = true)
    val mapper: ObjectMapper = mockk(relaxed = true)
    val registry: Registry = mockk(relaxed = true)
  }
}

