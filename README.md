# microservice-pub
## 1. Git
```bash
* git init

* git add README.md

* git commit -m "first commit"

* git branch -M main

* git remote add origin https://github.com/bomc/spp.git

* git push -u origin main
```

## 2. Build, Run and Deploy on Kubernetes

```bash
# in /publish
gradle jibDockerBuild


```

## 3. Swagger OpenAPI
local:
* http://localhost:8081/api-docs

* http://localhost:8081/webjars/swagger-ui/index.html?configUrl=/api-docs/swagger-config#/

minikube:

```bash
minikube service list
```

e.g. output:

| --- | --- | --- | --- |
| NAMESPACE | NAME | TARGET PORT | URL |
| --- | --- | --- | --- |
| bomc-consumer        | consumer-service                 | No node port                |
| bomc-publish         | publish-service-nodeport-ingress | http://192.168.99.104:30194 |
| --- | --- | --- | --- |

Invoke with given address from above:

* http://192.168.99.104:30194/webjars/swagger-ui/index.html?configUrl=/api-docs/swagger-config#/

## 4. REST urls
* curl -X POST "http://localhost:8081/api/metadata/annotation-validation" -H  "accept: */*" -H  "X-B3-TraceId: 82f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 11e3ac9a4f6e3b90" -H  "Content-Type: application/json" -d "{\"id\":\"42\",\"name\":\"bomc\"}"


* curl -X GET "http://localhost:8081/api/metadata/" -H  "accept: application/json" -H  "X-B3-TraceId: 70f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 15e3ac9a4f6e3b90"


* curl -X GET "http://localhost:8081/api/metadata/42" -H  "accept: application/json" -H  "X-B3-TraceId: 60f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 05e3ac9a4f6e3b90"


* curl -X PUT "http://localhost:8081/api/metadata/" -H  "accept: */*" -H  "X-B3-TraceId: 80f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 45e3ac9a4f6e3b90" -H  "Content-Type: application/json" -d "{\"id\":\"42\",\"name\":\"test\"}"


* curl -X DELETE "http://localhost:8081/api/metadata/42" -H  "accept: application/json" -H  "X-B3-TraceId: 70f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 25e3ac9a4f6e3b90"

## 5. Actuator
On local machine:
* curl -v GET http://localhost:8082/actuator/info

If running with nodeport:
curl -v GET http://192.168.99.104:30194/actuator/info | jq

If running with ingress:
* curl -v GET http://bomc.ingress.org/bomc/actuator/info | jq

## 6. Minikube - docker
```bash
minikube start

minikube start --vm-driver=virtualbox --cpus 2 --memory 10240 --disk-size=25GB

minikube addons list

minikube addons enable metrics-server

# Using 'top' after enablling metrics-server. 
kubectl top pods -n bomc-consumer

minikube addons enable ingress
```

### 6.1 GIT bash
```bash
# Add this line to .bash_profile if you want to use minikube's daemon by default (or if you do not want to set this every time you open a new terminal).
eval $(minikube docker-env)

eval $(docker-machine env -u)
```

### 6.2 Windows cmd
```bash
# List env variables
minikube docker-env

@FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env') DO @%i
```

```bash
minikube ssh
```

```bash
docker ps
```

### 6.3 Delete Minikube (Windows)
```bash
minikube stop & REM stops the VM
```

```bash
minikube delete & REM deleted the VM
```

Then delete the .minikube and .kube directories usually under:

`C:\users\{user}\.minikube`

and

`C:\users\{user}\.kube`

Or if you are using chocolatey:

```bash
C:\ProgramData\chocolatey\bin\minikube stop
C:\ProgramData\chocolatey\bin\minikube delete
choco uninstall minikube
choco uninstall kubectl
```

### 6.4 Docker commands
####Removing untagged images
```bash
docker image rm $(docker images | grep "^<none>" | awk "{print $3}")
```

####Remove all stopped containers.
```bash
docker container rm $(docker ps -a -q)
```

## 7. Tools
### 7.1 Gradle
```bash
gradle jibDockerBuild
```

```bash
gradle build
```

### 7.2 Dive
A tool for exploring a docker image, layer contents, and discovering ways to shrink the size of your Docker/OCI image.

```bash
# NOTE: On windows run it in cmd box.
dive localhost:5000/bomc/consumer:v.1.0.0-1-g5eb028a
```

### 7.3 Versioning
*Version v.1.0.0-1-g5eb028a means:*

v.1.0.0 -> last tag

1 -> number of commits since the last tag

g5eb028a -> hash of the last commit

### 7.4 Simple deployment to Minikube with kustomize

> From /deployment directory

```bash
# check kubernetes resources with kustomize (namespace, service deployment).
\deployment\kustomize build

# The -k option, which will direct kubectl to process the kustomization file.
kubectl apply -k .
```

### 7.5 Check deployment with kubectl - namespace, service and deployment
```bash
kubectl get pods -n bomc -o wide
kubectl get pods -n bomc -o yaml
kubectl describe pod consumer-66dc5c8d7d-ccs2x -n bomc

kubectl get deployments -n bomc
kubectl describe deployment consumer -n bomc

# Access the Init Container status programmatically by reading the status.initContainerStatuses field on the Pod Spec:
kubectl get pod nginx -n bomc --template '{{.status.initContainerStatuses}}'

# Accessing logs from Init Containers 
# Pass the Init Container name along with the Pod name to access its logs.
kubectl logs <pod-name> -c <init-container-2> -n <namespace>
```

### 7.6 Call application via NodePort
NodePort will use the cluster IP and expose’s the service via a static port.

```bash
# Expose consumer service
# 1. Read deployment name (-> 'Name') from deployment resource:
kubectl describe deployment consumer -n bomc
# 2.Expose port with expose command:
#   –type=NodePort makes the Service available from outside of the cluster. It will be available at <NodeIP>:<NodePort>
#   The command creates a service object that exposes the deployment 'consumer'
kubectl expose deployment consumer -n bomc --type=NodePort --name=consumer-nodeport

minikube service list

# Shows the address of the service.
minikube service consumer -n bomc --url

# This command will start the default browser, opening <NodeIP>:<NodePort>.
minikube service consumer -n bomc
```

| NAMESPACE | NAME | TARGET PORT | URL |
| --- | --- | --- | --- |
| bomc | consumer | - | http://192.168.99.102:31633 |

* Opening service bomc/consumer in default browser...

```bash
# Invoke application:
curl -X POST "http://192.168.99.102:31633/api/metadata/annotation-validation" -H  "accept: */*" -H  "X-B3-TraceId: 82f198ee56343ba864fe8b2a57d3eff7" -H  "X-B3-ParentSpanId: 11e3ac9a4f6e3b90" -H  "Content-Type: application/json" -d "{\"id\":\"42\",\"name\":\"bomc\"}"

# or in Browser OpenAPI:

http://192.168.99.100:30117/webjars/swagger-ui/index.html?configUrl=/api-docs/swagger-config
```

### 7.7 Open shell in running container
```bash
kubectl exec -it consumer-66dc5c8d7d-ccs2x -n bomc -- sh

kubectl exec -it consumer-66dc5c8d7d-ccs2x -n bomc bash
```

> NOTE: GIT bash on windows:
>
> ```bash
> winpty kubectl exec -it consumer-66dc5c8d7d-ccs2x -n bomc -- sh
> ```

### 7.8 Check if service is available and correct configured
Services are abstract interfaces (host + port) to a workload that may consist of several pods.

#### Step 1: Check if the Service exists
```bash
kubectl get svc -n bomc
```

#### Step 2: Test Your Service from inside a pod.
```bash
#works for http services
wget <servicename>:<httpport>

#Confirm there is a DNS entry for the service!
nslookup <servicename>
```

Alternatively, forward port local machine and test locally.

```bash
kubectl port-forward <service_name> 8000:8080 -n bomc
```

Address the service as localhost:8000.

#### Step 3: Check if the Service Is Actually Targeting Relevant Pods
K8s services route inbound traffic to one of the pods, based on the label selector. Traffic is routed to targeted pods by their IP.

```bash
kubectl get pods -l app=consumer -n bomc
```

So, check if the service is bound to those pods.

```bash
kubectl describe service <service-name> | grep Endpoints
```

The IPs of all the pods related to the workload listed. If not, go to step 4.

#### Step 4: Check Pod Labels
Make sure the selector in the K8s service matches the pods’ labels!

```bash
kubectl get pods --show-labels -n bomc

kubectl describe svc <service_name> -n bomc
```

#### Step 5: Confirm That Service Ports Match The Pod
Finally, make sure that the code in the pods actually listens to the targetPort that is specified for the service.

### 7.9 Adding Deployment Strategy
```yaml
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
```

This tell kubernetes how to replace old Pods by new ones. In this case the RollingUpdate (`rollingUpdate`) is used. The `maxSurge` of 1 specifies maximum number of Pods that can be created over the desired number of Pods `1` in this case. The `maxUnavailable` specifies the maximum number of Pods that can be unavailable during the update process `0` in this case.

## 8. Kubectl commands
```bash
# Get pods with a specific label 'app=consumer' 
kubectl get pods -l app=consumer -n bomc
# Get all pods with a specific name space and label 'app=consumer' 
kubectl get pods --all-namespaces -l app=consumer
# Get all services from all namespaces sorted by name
kubectl get services --all-namespaces --sort-by=.metadata.name -o wide
# List all container images in all namespaces
kubectl get pods --all-namespaces -o jsonpath="{..image}" | tr -s '[[:space:]]' '\n' | sort | uniq -c
# List environment variables from pod.
kubectl exec publisher-685c77dfc7-qs2mm -n bomc-publish -it -- env 
```

```bash
# Get pods with a specific label 'app=consumer' 
kubectl get pods -l app=consumer -o go-template='{{range .items}}{{.status.podIP}}{{"\n"}}{{end}}' -n bomc
```

```bash
kubectl get pod publisher-597764857f-vfpn8 -n bomc-publish -o json

kubectl get pod publisher-597764857f-vfpn8 -n bomc-publish -o json | jq '.status.hostIP' -r
kubectl get pod publisher-597764857f-vfpn8 -n bomc-publish -o json | jq '.status.podIP' -r
```


## 9. Microservice intercommunication inside same namespace via k8s services
The 'publish'-application invokes the 'consumer'-application via REST-request.

 publish - springboot/webclient---------->| REST-Call |---> consumer - springboot/rest-endpoint
   
#### a. Get environment variables inside consumer the pod.
Whenever a pod is created, k8s injects some env variables into the pods env. These variables can be used by containers to interact with other containers. So when a service is created, the address of the service will be injected as an env variable to all the pods that run within the *same namespace*.

The k8s conventions are:

{{SERVICE_NAME}}_SERVICE_HOST    # ClusterIP

{{SERVICE_NAME}}_SERVICE_PORT    # Port
 
```bash 
kubectl exec consumer-5d4969c4f5-dflkj -n bomc -- printenv | grep SERVICE

PUBLISH_SERVICE_SERVICE_HOST=10.109.177.190
PUBLISH_SERVICE_SERVICE_PORT_8181_TCP=8181
PUBLISH_SERVICE_SERVICE_PORT=8181
PUBLISH_SERVICE_PORT_8181_TCP=tcp://10.109.177.190:8181
PUBLISH_SERVICE_PORT_8181_TCP_ADDR=10.109.177.190
CONSUMER_SERVICE_SERVICE_HOST=10.102.64.176
CONSUMER_SERVICE_HOST=10.111.175.48
CONSUMER_SERVICE_SERVICE_PORT_8081_TCP=8081
CONSUMER_SERVICE_PORT_8081_TCP_PORT=8081
PUBLISH_SERVICE_HOST=10.97.213.196
PUBLISH_SERVICE_PORT=tcp://10.109.177.190:8181
KUBERNETES_SERVICE_PORT=443
CONSUMER_SERVICE_SERVICE_PORT=8081
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT_HTTPS=443
PUBLISH_SERVICE_PORT_8181_TCP_PROTO=tcp
PUBLISH_SERVICE_PORT_8181_TCP_PORT=8181
CONSUMER_SERVICE_PORT=tcp://10.102.64.176:8081
CONSUMER_SERVICE_PORT_8081_TCP=tcp://10.102.64.176:8081
CONSUMER_SERVICE_PORT_8081_TCP_PROTO=tcp
CONSUMER_SERVICE_PORT_8081_TCP_ADDR=10.102.64.176
```

#### b. Get environment variables from consumer-service
CONSUMER_SERVICE_SERVICE_HOST=10.102.64.176, CONSUMER_SERVICE_SERVICE_PORT=8081

#### c. Extend application property in publish application

```java

...

# Set the rest client base url in 'publish'-application to invoke the 'consumer'-application. 
bomc.web-client.base-url=http://${CONSUMER_SERVICE_SERVICE_HOST}:${CONSUMER_SERVICE_SERVICE_PORT}

...

```

## 10. Microservice intercommunication accross namespaces
K8s doesn't inject environment variables from other namespaces. Using service names like 'consumer-service' are only valid within the same namespace.

### 10.1 Using fully-qualified DNS names
Kubernetes has cluster-aware DNS service like CoreDNS running, so it is possible using fully qualified DNS names starting from cluster.local. Assume the 'consumer'-Application is running in namespace 'bomc-consumer' and has a service 'consumer-service' defined. To address using an URL shown below:

```
# base-url for communication accross different namespaces.
{{SERVICE_NAME}}.{{NAMESPACE_NAME}}.svc.cluster.local:{{PORT (is optional if port is 80)}}
# in application.properties
bomc.web-client.base-url=http://consumer-service.bomc-consumer.svc.cluster.local:8081
```

## 11. Healthcheck
### 11.1 Adding liveness probe
```yaml
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 30
            failureThreshold: 3
```

This will check to see an HTTP 200 response from the endpoint `/actuator/health` at the deployment port every 30 seconds (`periodSeconds`) after an initial delay (`initialDelaySeconds`) of 30 seconds for a maximum of 3 times (`failureThreshold`) after which it is going to restart the container for which this liveness probe is added.

### 11.2 Adding readiness probe
```yaml
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 15
            periodSeconds: 30
            failureThreshold: 3
```

This will check to see an HTTP 200 response from the endpoint `/actuator/health` at the deployment port every 30 seconds (`periodSeconds`) after an initial delay (`initialDelaySeconds`) of 15 seconds for a maximum of 3 times (`failureThreshold`) after which it is going to Pod will be marked container as `Unready` and no traffic will be sent to it for which this readiness probe is added which is the container running the application.

## 12. Requests and limits


## 13. Ingress router
Exceute the following command:

```bash
minikube addons enable ingress
```

### 13.1 Create a service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: publish-service-nodeport-ingress
  labels:
    app: publish
  namespace: bomc-publish
spec:
  selector:
    app: publish
  ports:
  - protocol: TCP
    name: 8181-tcp
    port: 8181
    targetPort: 8181
  type: NodePort
  
#
# This rewrite any characters captured by '(.*)' will be assigned to the placeholder '$2',
# which is then used as a parameter in the 'rewrite-target' annotation.
# This will results in the following rewrites:
#
# - 'bomc.ingress.org/bomc' rewrites to 'bomc.ingress.org/'
# - 'bomc.ingress.org/bomc/' rewrites to 'bomc.ingress.org/'
# - 'bomc.ingress.org/bomc/api/metadata' rewrites to 'bomc.ingress.org/api/metdata'
#
```

NOTE: the value of `type` is *NodePort*.

### 13.2 Check service address
```bash
minikube service publish-service-nodeport-ingress -n bomc-publish --url

http://192.168.99.104:30194
```

> Note: If Minikube is running locally, use `minikube ip` to get the external IP. The IP address displayed within the ingress list will be the internal IP.

### 13.3 Edit host file
on window: C:\Windows\System32\drivers\etc\hosts
add the following line to the hosts file.

```
192.168.99.104 bomc.ingress.org
```

This sends requests from `bomc.ingress.org` to Minikube:

Verify that the Ingress controller is directing traffic with a simple curl.

## 14. ConfigMap
A ConfigMap is a dictionary of configuration settings. This dictionary consists of key-value pairs of strings. Kubernetes provides these values to your containers.

The given ConfigMap:

```YAML
apiVersion: v1
kind: ConfigMap
metadata:
  name: publisher
  namespace: bomc-publish
data:
  application-k8s.properties: |-
    bomc.consumer=http://consumer-service.bomc-consumer.svc.cluster.local:8081
    bomc.github=https://api.github.com
```

### 14.1 ConfigMap with Environment Variables and `envFrom`
Expose with environment variables:

```YAML
apiVersion: v1
kind: ConfigMap
metadata:
  name: publisher
  namespace: bomc-publish
data:
  application-k8s.properties: |-
    bomc.consumer=http://consumer-service.bomc-consumer.svc.cluster.local:8081
    bomc.github=https://api.github.com
```

reference in Kubernetes Deployment:

```YAML
     spec:
       serviceAccountName: publisher-account
       containers:
       env:
         - name: CONSUMER_HOST_ADDRESS
           valueFrom:
             configMapKeyRef:
               name: publisher
               key: bomc.consumer
         - name: GITHUB_HOST_ADDRESS
           valueFrom:
             configMapKeyRef:
               name: publisher
               key: bomc.github
```

or reference with `envFrom`

```YAML
     spec:
       serviceAccountName: publisher-account
       containers:
       envFrom:
       - configMapRef:
           name: publisher
```

Inject in Spring Boot java code:

```JAVA
@Value("${bomc.consumer}")
private String consumerBaseUrl;

@Value("${bomc.github}")
private String githubBaseUrl;
```

### 14.2 ConfigMap with spring boot cloud fabric8
Load application properties from Kubernetes ConfigMaps and Secrets. Reload application properties when a ConfigMap or Secret changes.

Gradle dependencies

```
dependencies {
	implementation 'org.springframework.cloud:spring-cloud-starter-kubernetes-fabric8-config:2.0.1'
	
  ...
  
dependencyManagement {
	imports {
		mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
	}
}
```

Properties setting in bootstrap.properties

```PROPERTIES
###
#
# Set the app name
spring.application.name=publisher

###
#
# Configuration for config-map handling.
spring.cloud.kubernetes.config.sources[0].name=${spring.application.name}
spring.cloud.kubernetes.config.sources[0].namespace=bomc-publish

spring.cloud.kubernetes.reload.enabled=true
spring.cloud.kubernetes.reload.mode=polling
spring.cloud.kubernetes.reload.period=30000
```

Properties setting in application.properties

```PROPERTIES
management.endpoint.restart.enabled=true
```

Inject in Spring Boot java code:

```JAVA
@Value("${bomc.consumer}")
private String consumerBaseUrl;

@Value("${bomc.github}")
private String githubBaseUrl;
```

## 15 ArgoCD

### 15.1 Install ArgoCD
see [https://argoproj.github.io/argo-cd/getting_started/](https://argoproj.github.io/argo-cd/getting_started/)

```BASH
kubectl create namespace argocd

kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Download install.yaml locally and install it from local directory.
kubectl apply -n argocd -f install-argocd.yaml
```
 
### 15.2 Download Argo CD CLI (optional)
Download the latest Argo CD version from [https://github.com/argoproj/argo-cd/releases/latest](https://github.com/argoproj/argo-cd/releases/latest). 
More detailed installation instructions can be found via the CLI installation documentation.

### 15.3 Access The Argo CD API Server
#### Ingress
Follow the [ingress documentation](https://argoproj.github.io/argo-cd/operator-manual/ingress/) on how to configure Argo CD with ingress.

```YAML
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: argocd-ingress
  namespace: argocd
  labels:
    app: argocd
  annotations:
    ingress.kubernetes.io/ssl-redirect: "false"
spec:
  rules:
  - http:
      paths:
      - path: /argocd
        backend:
          serviceName: argocd-server
          servicePort: 80
# kubectl apply -f 010-argocd-ingress.yaml -n argocd
# Open a browser on 'http://localhost:8080/argocd'
```

#### Port Forwarding
Kubectl port-forwarding can also be used to connect to the API server without exposing the service.

```BASH
# The port-forward command will also now be running in the foreground of the terminal.
# Open another terminal window or tab and cd back into the working directory.
kubectl port-forward svc/argocd-server -n argocd 9001:443

# ArgoCD will be available at 
https://localhost:9001
```

#### Login to UI -> get password
ArgoCD uses the unique name of its server pod as a default password, so every installation will be different.

The following command will list the pods and format the output to provide just the line to need. 

It will have the format argocd-server-<number>-<number>.

```BASH
kubectl get pods -n argocd -l app.kubernetes.io/name=argocd-server -o name | cut -d'/' -f 2
```

## 16 Ambassador

### 16.1 Install

```bash
kubectl apply -f https://www.getambassador.io/yaml/aes-crds.yaml && kubectl wait --for condition=established --timeout=90s crd -lproduct=aes && kubectl apply -f https://www.getambassador.io/yaml/aes.yaml && kubectl -n ambassador wait --for condition=available --timeout=90s deploy -lproduct=aes
```

