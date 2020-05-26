![CI](https://github.com/spinnaker-plugin-examples/kubernetesCRDHandler/workflows/CI/badge.svg)
![Latest Kork](https://github.com/spinnaker-plugin-examples/kubernetesCRDHandler/workflows/Latest%20Kork/badge.svg?branch=master)
![Latest Clouddriver](https://github.com/spinnaker-plugin-examples/kubernetesCRDHandler/workflows/Latest%20Clouddriver/badge.svg?branch=master)

This plugin demonstrates how to create a Kubernetes CRD handler for Spinnaker. There are a few existing CRD integrations in Spinnaker,
but none that allow you to exercise custom code when evaluating a manifest's stability.

When a Spinnaker stage performs CRUD on a Kubernetes manifest, Orca polls Clouddriver to ask 
if the manifest is stable before moving on to the next stage.

Clouddriver's `KubernetesHandler`s define stability for each Kubernetes kind (`deployment`, `daemonset`, etc.). 
A handler inspects a Kubernetes manifest and decides if it has reached its intended state. 
Typically it will check a set of fields in the manifest's `status`: e.g., does the desired number of pods
in this replica set match the number of actual, healthy, running pods?

This plugin exercises the Spring capabilities of Spinnaker plugins. There are no PF4J extension points or dependencies;
it relies on Clouddriver's implementation details rather than an API contract. Do not rely on this plugin or 
this style of plugin for your production workloads. In the future, we may establish a PF4J extension point for Kubernetes handlers. 

## Setup

This example uses a CRD of type `MyCRD`. The CRD definition can be found under `crd/MyCRD.yaml`, and an example instance
can be found under `crd/MyCRDInstance.yaml`.

When testing out this plugin (or your own variant), you may find it helpful to demonstrate that the following work:

1). Submit the CRD definition to your Kubernetes cluster: `kubectl apply -f crd/MyCRD.yaml`.

2). Manually create an instance of that CRD: `kubectl apply -f crd/MyCRDInstance.yaml`. Verify that Clouddriver is caching your resource.

3). Create a pipeline with a Deploy Manifest stage that deploys a `MyCRD` instance (be sure to give the instance a different name than
the one used in step #2). Verify that your pipeline successfully deploys the resource.

4). Update the `MyCRD` instance in your pipeline to a state that your handler will never mark as stable. For the `MyCRD` resource, this
means marking the manifest's `status.ready` property as `false`.

## Usage

1) Run `./gradlew releaseBundle`
2) Put the `/build/distributions/<project>-<version>.zip` into the [configured plugins location for your service](https://pf4j.org/doc/packaging.html).
3) Configure the Spinnaker service. Put the following in the service yml to enable the plugin and configure the extension.

```
spinnaker:
  extensibility:
    plugins:
      Armory.KubernetesCRDHandler:
        enabled: true
```

Or use the [examplePluginRepository](https://github.com/spinnaker-plugin-examples/examplePluginRepository) to avoid copying the plugin `.zip` artifact.

To debug the plugin inside Clouddriver using IntelliJ Idea follow these steps:

1) Run `./gradlew releaseBundle` in the plugin project.
2) Copy the generated `.plugin-ref` file under `build` in the plugin project submodule for the service to the `plugins` directory under root in the Spinnaker service that will use the plugin .
3) Link the plugin project to the service project in IntelliJ (from the service project use the `+` button in the Gradle tab and select the plugin build.gradle).
4) Configure the Spinnaker service the same way specified above.
5) Create a new IntelliJ run configuration for the service that has the VM option `-Dpf4j.mode=development` and does a `Build Project` before launch.
6) Debug away...
