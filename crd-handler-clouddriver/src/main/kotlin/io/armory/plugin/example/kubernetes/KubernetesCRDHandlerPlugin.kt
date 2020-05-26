package io.armory.plugin.example.kubernetes

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin
import org.slf4j.LoggerFactory
import org.pf4j.PluginWrapper
import org.springframework.beans.factory.support.BeanDefinitionRegistry

class KubernetesCRDHandlerPlugin(wrapper: PluginWrapper) : PrivilegedSpringPlugin(wrapper) {

  override fun registerBeanDefinitions(registry: BeanDefinitionRegistry) {
    registerBean(primaryBeanDefinitionFor(MyCRDHandler::class.java), registry)
  }

  private val logger = LoggerFactory.getLogger(KubernetesCRDHandlerPlugin::class.java)

  override fun start() {
    logger.info("CustomKubernetesHandlerPlugin.start()")
  }

  override fun stop() {
    logger.info("CustomKubernetesHandlerPlugin.stop()")
  }
}
