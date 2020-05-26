package io.armory.plugin.example.kubernetes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.CustomKubernetesCachingAgentFactory
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.KubernetesV2CachingAgentFactory
import com.netflix.spinnaker.clouddriver.kubernetes.description.SpinnakerKind
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesApiGroup
import com.netflix.spinnaker.clouddriver.kubernetes.op.handler.KubernetesHandler
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesKind
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesManifest
import com.netflix.spinnaker.clouddriver.kubernetes.model.Manifest
import org.slf4j.LoggerFactory

class MyCRDHandler : KubernetesHandler() {

  private val logger = LoggerFactory.getLogger(MyCRDHandler::class.java)
  private val mapper = jacksonObjectMapper()

  init {
    logger.info("MyCRDHandler being initialized...")
  }

  /*
  * When a stage performs CRUD on a Kubernetes manifest, Orca
  * polls Clouddriver to ask if the manifest is stable before moving on
  * to the next stage.
  *
  * Stability is defined for each Kubernetes kind (deployment, daemonset, etc.)
  * by a KubernetesHandler. A handler inspects a Kubernetes manifest and decides if
  * it has reached its intended state. Typically it will check a set of
  * fields in the manifest's `status`: e.g., does the desired number of pods
  * in this replica set match the number of actual, healthy, running pods?
  * */
  override fun status(manifest: KubernetesManifest?): Manifest.Status {
    return manifest.status()?.let {
      if (it.ready) {
        Manifest.Status().apply {
          stable("This manifest is stable!")
        }
      } else {
        Manifest.Status().apply {
          unstable("This manifest is not stable!")
        }
      }
    } ?: Manifest.Status.noneReported()
  }

  override fun kind(): KubernetesKind {
    return KubernetesKind.from("MyCRD", KubernetesApiGroup.fromString("armory.io"))
  }

  override fun versioned(): Boolean {
    return false
  }

  override fun spinnakerKind(): SpinnakerKind {
    return SpinnakerKind.UNCLASSIFIED
  }

  override fun deployPriority(): Int {
    return DeployPriority.LOWEST_PRIORITY.value
  }

  override fun cachingAgentFactory(): KubernetesV2CachingAgentFactory {
    return KubernetesV2CachingAgentFactory { creds, mapper, registry, agentIndex, agentCount, agentInterval ->
      CustomKubernetesCachingAgentFactory.create(kind(), creds, mapper, registry, agentIndex, agentCount, agentInterval)
    }
  }

  private fun KubernetesManifest?.status(): MyCRDStatus? {
    return this?.get("status")?.let {
      mapper.convertValue(it)
    }
  }

  private data class MyCRDStatus(
      val ready: Boolean
  )
}

