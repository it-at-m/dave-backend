# Helm chart for DAVe Backend

This Helm chart installs DAVe Backend to a kubernetes cluster. 

## Overview

DAVe is a software to collect and visualize traffic data. It is composed of a number of components and this part deploys the central backend. It exposes APIs for all other components and is storing all traffic data.

## Installation

You can use Helm chart by pulling from Docker Hub or directly from source code repo. 

__From Docker Hub__

```bash
helm install oci://registry-1.docker.io/starwitorg/dave-backend-chart
```

__From Source__

```bash
helm install backend ./helm
```

## Configuration

The following table lists the configurable parameters of the DAVe Backend chart and their default values.

### General Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Container image repository | `starwitorg/dave-backend` |
| `image.pullPolicy` | Container image pull policy | `IfNotPresent` |
| `image.tag` | Container image tag | `3.0.1-SNAPSHOT-16` |
| `imagePullSecrets` | Image pull secrets | `[]` |

### App Configuration
Parameters to configure application details like Spring datasource and authentication.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `app.db_schema` | Database schema name | `dave` |
| `app.spring.profile.active` | Spring profiles to activate | `prod` |
| `app.log.level.root` | Root log level | `info` |
| `app.datasource.url` | Database connection URL | `jdbc:postgresql://postgres-chart-dave-db:5432/dave` |
| `app.datasource.username` | Database username | `dave` |
| `app.datasource.password` | Database password | `dave_pw` |
| `app.jpa.properties.hibernate.dialect` | Hibernate dialect | `org.hibernate.dialect.PostgreSQLDialect` |
| `app.jpa.properties.hibernate.format_sql` | Format SQL queries | `true` |
| `app.jpa.hibernate.naming.physical_strategy` | Hibernate naming strategy | `org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl` |
| `app.jpa.show_sql` | Show SQL queries in logs | `false` |
| `app.servlet.multipart.max_file_size` | Maximum file upload size | `10MB` |
| `app.servlet.multipart.max_request_size` | Maximum request size | `10MB` |
| `app.auth.enabled` | Enable OAuth2 authentication | `true` |
| `app.auth.client.registration.keycloak.scope` | OAuth2 scope | `openid` |
| `app.auth.client.registration.keycloak.authorization_grant_type` | OAuth2 grant type | `client_credentials` |
| `app.auth.client.registration.keycloak.client_authentication_method` | OAuth2 client auth method | `client_secret_post` |
| `app.auth.client.registration.keycloak.provider` | OAuth2 provider name | `mykeycloak` |
| `app.auth.client.registration.keycloak.client_secret` | OAuth2 client secret | `secret` |
| `app.auth.client.registration.keycloak.client_id` | OAuth2 client ID | `dave` |
| `app.auth.client.provider.mykeycloak.issuer_uri` | OAuth2 issuer URI | `http://auth.cluster.local/auth/realms/realmname` |
| `app.auth.client.provider.mykeycloak.token_uri` | OAuth2 token URI | `http://auth.cluster.local/auth/realms/realmname/protocol/openid-connect/token` |
| `app.auth.resourceserver.jwt.jwk_set_uri` | JWT JWK set URI | `http://auth.cluster.local/auth/realms/realmnam` |
| `app.auth.resourceserver.jwt.issuer_uri` | JWT issuer URI | `http://auth.cluster.local/auth/realms/realmname` |
| `app.auth.resource.user_info_uri` | OAuth2 user info URI | `http://auth.cluster.local/auth/realms/realmname/protocol/openid-connect/userinfo` |

### DAVe Configuration

Configuration to control DAVe application parameters.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `dave.zaehlung.status.updater` | Cron expression for status updates | `0 0 * 1/1 * ?` |
| `dave.email.sender.hostname` | Email sender hostname | `localhost` |
| `dave.email.address` | Email address | `test@example.com` |
| `dave.email.password` | Email password | `password` |
| `dave.email.url.adminportal` | Admin portal URL | `http://localhost:8085` |
| `dave.email.url.selfserviceportal` | Self-service portal URL | `http://localhost:8086` |
| `dave.email.receiver.update_interval` | Email receiver update interval (ms) | `50000000000` |
| `dave.email.receiver.hostname` | Email receiver hostname | `imap.de` |
| `dave.messstelle.cron` | Messstelle cron expression | `0 0 * 1/1 * ?` |
| `dave.messstelle.shedlock` | Messstelle shedlock duration | `4m` |
| `dave.reports.logo_icon` | Report logo icon path | `classpath:/pdf/images/kindl.jpg` |
| `dave.reports.logo_subtitle` | Report logo subtitle | `Landeshauptstadt<br/>München<br/><b>Mobilitätsreferat</b>` |
| `dave.map.center.lat` | Map center latitude | `52.4199491` |
| `dave.map.center.lng` | Map center longitude | `10.7171103` |
| `dave.map.center.zoom` | Map center zoom level | `12` |

### Elasticsearch Configuration
If you want to use Elasticsearch, here are the the necessary parameters.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `elasticsearch.host` | Elasticsearch host | `192.168.100.79` |
| `elasticsearch.port` | Elasticsearch port | `9200` |
| `elasticsearch.user` | Elasticsearch username | `elastic` |
| `elasticsearch.password` | Elasticsearch password | `changeme` |
| `elasticsearch.http_ca_certificate` | Elasticsearch CA certificate fingerprint | `55:F0:DF:A1:E6:25:A6:79:C5:64:10:0C:B5:4C:08:FD:34:4E:C4:B7:2A:3F:13:30:0F:91:4E:54:13:81:E4:AD` |
| `elasticsearch.connectTimeout` | Elasticsearch connect timeout | `10` |
| `elasticsearch.socketTimeout` | Elasticsearch socket timeout | `30` |

### Kubernetes Configuration

Config to control application behaviour in Kubernetes.

| Parameter | Description | Default |
|-----------|-------------|---------|
| `serviceAccount.create` | Create service account | `false` |
| `serviceAccount.annotations` | Service account annotations | `{}` |
| `serviceAccount.name` | Service account name | `""` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.ports` | Service port configuration | See values.yaml |
| `extraEnvVars` | Additional environment variables | `TZ: "Europe/Berlin"` |
| `extraVolumeMounts` | Additional volume mounts | `[]` |
| `extraVolumes` | Additional volumes | `[]` |
| `initContainers` | Init containers | `[]` |
| `credentials.existingSecret` | Existing secret for LDAP authentication | `""` |
| `serviceMonitor.enabled` | Enable Prometheus ServiceMonitor | `false` |
| `podAnnotations` | Pod annotations | `{}` |
| `deploymentAnnotations` | Deployment annotations | `{}` |
| `podSecurityContext` | Pod security context | `{}` |
| `securityContext` | Container security context | `{}` |
| `resources.limits.cpu` | CPU limit | `1500m` |
| `resources.limits.memory` | Memory limit | `3512Mi` |
| `resources.requests.cpu` | CPU request | `50m` |
| `resources.requests.memory` | Memory request | `3512Mi` |
| `autoscaling.enabled` | Enable horizontal pod autoscaling | `false` |
| `autoscaling.minReplicas` | Minimum number of replicas | `1` |
| `autoscaling.maxReplicas` | Maximum number of replicas | `100` |
| `autoscaling.targetCPUUtilizationPercentage` | Target CPU utilization | `80` |
| `nodeSelector` | Node selector | `{}` |
| `tolerations` | Tolerations | `[]` |
| `affinity` | Affinity rules | `{}` |

