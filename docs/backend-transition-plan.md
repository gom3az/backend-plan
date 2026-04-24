# 6-Month Backend Transition Plan
## Senior Mobile Developer → Senior Backend Engineer

**Created**: 2026-04-24
**Approach**: Project-First, Three Phases
**Weekly Commitment**: 15-25 hours/week
**Target**: Senior Backend Engineer role

---

## Profile Summary

| Attribute | Value |
|-----------|-------|
| Starting point | Senior mobile developer (5+ years) |
| Target role | Senior Backend Engineer |
| Existing knowledge | Kotlin/Spring basics, Databases, REST APIs |
| Gap area | Docker/Cloud infrastructure |
| Learning budget | Free resources only |
| Time commitment | Heavy (15-25 hrs/week) |

---

## Overall Structure

| Phase | Months | Core Theme | Portfolio Piece |
|-------|--------|------------|-----------------|
| 1 | 1-2 | **REST API Mastery** | Task Management Service |
| 2 | 3-4 | **Event-Driven Systems** | Notification Pipeline |
| 3 | 5-6 | **Cloud-Native Full Stack** | Deployed User Auth Service |

---

## Phase 1 — Months 1-2: REST API Mastery

**Goal**: Build a production-quality task management API. This is your baseline project — everything later builds on this.

### Month 1 — Spring Boot + Database Foundations

---

#### Week 1: Spring Boot Project Setup + REST Fundamentals

*Objective*: Get a bare-bones Spring Boot Kotlin app running with a REST controller. Understand the Spring Boot bootstrap mechanism.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Kotlin coroutines deep dive — suspending functions, Flow, channels | [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines.html) |
| Wed | Spring Boot REST guide — Kotlin examples throughout | [Building REST Services with Spring](https://spring.io/guides/tutorials/rest/) |
| Thu | Build: Create task API project with Spring Initializr | [Spring Initializr](https://start.spring.io/) |
| Fri-Sun | Build: GET /tasks, POST /tasks endpoints returning JSON | — |

*Deliverable*: One REST endpoint returning a list of tasks as JSON. Pushed to GitHub.

---

#### Week 2: PostgreSQL + Spring Data JPA

*Objective*: Connect your app to PostgreSQL. Model a Task entity, use Spring Data JPA repositories.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | JPA internals — entity lifecycle, dirty checking, lazy vs eager fetch | [Vlad Mihalcea — Hibernate Tutorials](https://vladmihalcea.com/tutorials/hibernate/) |
| Wed | Spring Boot + JPA + PostgreSQL setup, entity modeling | [Baeldung — Spring Boot CRUD](https://www.baeldung.com/spring-boot-crud-thymeleaf) |
| Thu | Flyway migrations — versioned schema scripts | [Flyway Documentation](https://flywaydb.org/documentation/) |
| Fri-Sun | Build: Task entity, repository, CRUD endpoints backed by PostgreSQL | [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/) |

*Deliverable*: Task CRUD endpoints backed by PostgreSQL with versioned Flyway migration scripts.

---

#### Week 3: DTOs, Validation, Exception Handling

*Objective*: Build clean API contracts. Handle validation errors gracefully with proper HTTP status codes.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | DTO pattern — mapping entities to data transfer objects, Kotlin data classes | [Baeldung — MapStruct](https://www.baeldung.com/mapstruct) |
| Wed | Bean Validation — @NotNull, @Size, @Email, custom validators | [Baeldung — Bean Validation](https://www.baeldung.com/javax-validation) |
| Thu | Exception handling — @ControllerAdvice, @ExceptionHandler, error response DTOs | [Baeldung — Spring Exception Handling](https://www.baeldung.com/spring-exceptions) |
| Fri-Sun | Build: DTOs for all request/response, global exception handler, proper 400/404/500 responses | — |

*Deliverable*: All endpoints use DTOs. Invalid input returns structured 400 response. Missing resources return 404.

---

#### Week 4: Spring Security + JWT Auth

*Objective*: Secure your API with JWT authentication. Implement user registration and login.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | JWT internals — claims, signing (HS256), expiration, refresh tokens | [JWT.io — Introduction](https://jwt.io/introduction/) |
| Wed | Spring Security architecture — filter chain, UserDetailsService, PasswordEncoder | [Spring Security Reference](https://docs.spring.io/spring-security/reference/) |
| Thu | Implementing JWT auth — token generation, validation, SecurityFilterChain config | [Baeldung — Spring Security JWT](https://www.baeldung.com/spring-security-authentication) |
| Fri-Sun | Build: POST /auth/register, POST /auth/login, protected task endpoints require valid JWT | — |

*Deliverable*: POST /auth/register and POST /auth/login endpoints. All write endpoints (POST/PUT/DELETE /tasks) require valid JWT.

---

### Month 2 — API Quality + Observability

---

#### Week 5: Pagination, Filtering, Sorting

*Objective*: Build production-quality query endpoints. Handle large datasets gracefully.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | REST query design — cursor vs offset pagination, filter param conventions | [Stripe — Working with APIs](https://stripe.com/blog/working-with-apis) |
| Wed | Spring Data pagination — Pageable, Page, Slice, Sort | [Baeldung — Pagination in Spring](https://www.baeldung.com/spring-data-pagination) |
| Thu | Specification pattern — dynamic filtering with JPA Specifications | [Baeldung — JPA Specifications](https://www.baeldung.com/rest-api-search-specification) |
| Fri-Sun | Build: GET /tasks?page=0&size=20&status=open&sort=dueDate,asc with full filtering | — |

*Deliverable*: Paginated, filterable, sortable task list endpoint.

---

#### Week 6: Testing with Testcontainers + Integration Tests

*Objective*: Write tests that spin up a real PostgreSQL container. Build real confidence in your code.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Why Testcontainers — real databases in tests, parity between local/CI/prod | [Testcontainers — Getting Started](https://testcontainers.com/getting-started/) |
| Wed | Testing Spring Boot REST API with Testcontainers | [Testcontainers — Testing Spring Boot REST API](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/) |
| Thu | Replacing H2 with real database for testing | [Testcontainers — Replace H2 with Real Database](https://testcontainers.com/guides/replace-h2-with-real-database-for-testing/) |
| Fri-Sun | Build: Integration tests for all CRUD endpoints + auth flow, run in GitHub Actions | [Testcontainers — Java Guide](https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/) |

*Deliverable*: Full integration test suite. GitHub Actions run tests on every push.

---

#### Week 7: Caching + API Documentation

*Objective*: Add Redis caching for read endpoints. Document your API with OpenAPI/Swagger.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Caching patterns — cache-aside, write-through, TTL, cache invalidation strategies | [Baeldung — Spring Cache Tutorial](https://www.baeldung.com/spring-cache-tutorial) |
| Wed | Redis + Spring Boot — Lettuce client, @Cacheable, @CacheEvict | [Baeldung — Redis with Spring](https://www.baeldung.com/spring-data-redis) |
| Thu | OpenAPI 3 / Swagger — springdoc-openapi, annotating endpoints, generating client SDKs | [Baeldung — Swagger/OpenAPI Documentation](https://www.baeldung.com/spring-rest-openapi-documentation) |
| Fri-Sun | Build: Cache GET /tasks with 60s TTL, add Swagger UI at /swagger-ui | — |

*Deliverable*: Swagger UI accessible at /swagger-ui. GET /tasks responses cached with configurable TTL.

---

#### Week 8: Dockerize + Portfolio Polish

*Objective*: Containerize your app. Write a portfolio-quality README that showcases your work.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Multi-stage Dockerfiles for Spring Boot — small image footprint, layered JARs | [Docker — Containerizing Spring Boot](https://docs.docker.com/language/java/) |
| Wed | Docker Compose — local dev with PostgreSQL + Redis services | [Docker Compose Samples](https://docs.docker.com/compose/samples-for-dev/) |
| Thu | Writing a portfolio README — architecture overview, setup instructions, API docs, badges | [GitHub README Best Practices](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-readmes) |
| Fri | Deploy to Railway free tier — PostgreSQL + Redis add-ons | [Railway Documentation](https://docs.railway.app/) |
| Sat-Sun | Polish README, ensure CI passes, verify live endpoint works | — |

*Deliverable*: GitHub repo with working Dockerfile, Docker Compose, comprehensive README, live deployed API at a public URL.

---

## Phase 2 — Months 3-4: Event-Driven Systems

**Goal**: Build a notification pipeline — handles user events, queues work, processes asynchronously. Your Phase 1 API now emits events instead of directly sending notifications.

### Month 3 — Messaging + Async Processing

---

#### Week 9: RabbitMQ Fundamentals

*Objective*: Understand message brokers. Build a producer/consumer with RabbitMQ.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Message broker concepts — queues, exchanges, bindings, routing keys | [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials/tutorial-one-python.html) |
| Wed | Spring AMQP — RabbitTemplate, @RabbitListener, exchange/queue configuration | [Spring AMQP Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.amqp) |
| Thu | Dead letter queues, message acknowledgment, retry strategies | [RabbitMQ — Dead Letter Exchanges](https://www.rabbitmq.com/docs/dlx) |
| Fri-Sun | Build: Add RabbitMQ to Phase 1 app. Task completion publishes event to exchange. | — |

*Deliverable*: Phase 1 API publishes task.completed events to RabbitMQ exchange.

---

#### Week 10: Kafka Fundamentals

*Objective*: Learn Apache Kafka — partitions, consumer groups, offset management. Compare with RabbitMQ.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Kafka core concepts — topics, partitions, offsets, consumer groups | [Kafka — Getting Started](https://kafka.apache.org/documentation/#gettingStarted) |
| Wed | Spring Kafka — KafkaTemplate, @KafkaListener, serde, producer config | [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/) |
| Thu | Consumer groups, rebalancing, exactly-once semantics | [Kafka — Consumer Groups](https://kafka.apache.org/documentation/#intro_consumers) |
| Fri-Sun | Build: Replace RabbitMQ with Kafka for task events. Consumer group for notification service. | [Spring Boot + Kafka Guide](https://spring.io/projects/spring-kafka) |

*Deliverable*: Task events published to Kafka. Notification consumer in a separate consumer group.

---

#### Week 11: Consumer Groups, Retry, Idempotency

*Objective*: Build a robust consumer that handles failures gracefully.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Retry strategies — exponential backoff, dead letter topics, retry queues | [Kafka — Retry Logic](https://kafka.apache.org/documentation/) |
| Wed | Idempotency — why it matters, dedup with event IDs, database unique constraints | [Kafka — Transactions](https://kafka.apache.org/documentation/#transactions) |
| Thu | Consumer offset management — auto.commit vs manual, commit strategies | [Kafka — Consumer Configuration](https://kafka.apache.org/documentation/#consumerconfigs) |
| Fri-Sun | Build: Implement retry with backoff, dead letter topic, idempotent notification handler | — |

*Deliverable*: Notification consumer retries failed processing up to 3 times, then moves to dead letter topic. Events processed exactly once via deduplication.

---

#### Week 12: Event-Driven Architecture Patterns

*Objective*: Understand the broader patterns. Apply event sourcing or CQRS-lite to your notification domain.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Event-driven architecture patterns — event sourcing, CQRS, saga | [Martin Fowler — Event Sourcing](https://martinfowler.com/articles/201701-event-sourcing.html) |
| Wed | Event-driven architecture patterns — saga, choreography vs orchestration | [Martin Fowler — Saga Pattern](https://martinfowler.com/articles/201905-saga.html) |
| Thu | Outbox pattern — reliable event publishing from a transactional database | [ microservices.io — Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html) |
| Fri-Sun | Build: Apply outbox pattern for reliable event publishing from your Phase 1 app | — |

*Deliverable*: Reliable event publishing using the transactional outbox pattern. No events lost on database failures.

---

### Month 4 — Background Jobs + Observability

---

#### Week 13: Background Jobs + Distributed Locks

*Objective*: Schedule background work. Handle concurrent execution with distributed locks.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Spring Scheduling — @Scheduled, thread pools, cron expressions | [Baeldung — Spring Scheduling](https://www.baeldung.com/spring-scheduling) |
| Wed | Distributed locks — Redis-based locks with Redisson or Spring Integration | [Baeldung — Redis Distributed Locks](https://www.baeldung.com/redis-distributed-locks) |
| Thu | Job queues — background processing with retry, Work Queue pattern | [Redis Queue Pattern (Baeldung)](https://www.baeldung.com/spring-redis-job-queue) |
| Fri-Sun | Build: Scheduled job to send daily task summary emails. Distributed lock prevents duplicate sends. | — |

*Deliverable*: Daily digest email job with distributed locking preventing duplicate sends.

---

#### Week 14: Observability — Metrics + Tracing

*Objective*: Make your system observable. Add metrics and distributed tracing.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Observability pillars — metrics, logs, traces. The three foundations. | [OpenTelemetry — Introduction](https://opentelemetry.io/docs/concepts/observability-primer/) |
| Wed | Micrometer + Prometheus — @Timed, custom metrics, Prometheus endpoint | [Micrometer Documentation](https://micrometer.io/docs/) |
| Thu | Distributed tracing — OpenTelemetry Java agent, trace IDs across Kafka/RabbitMQ messages | [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/) |
| Fri-Sun | Build: Add Prometheus metrics to Phase 1 API. Trace IDs propagate through Kafka messages. | [Baeldung — Micrometer Prometheus](https://www.baeldung.com/micrometer) |

*Deliverable*: /actuator/prometheus endpoint exposing metrics. Trace IDs in all log lines and Kafka message headers.

---

#### Week 15: Logging + Alerting

*Objective*: Structured logging that enables debugging. Alerting that wakes you up when things break.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Structured logging — JSON logs, correlation IDs, MDC | [Baeldung — Spring Logging](https://www.baeldung.com/spring-logging) |
| Wed | Log aggregation — ELK stack (Elasticsearch, Logstash, Kibana) basics | [Elastic — Getting Started](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started.html) |
| Thu | Alerting — defining SLOs, alerting on error rate, latency | [Prometheus — Alerting](https://prometheus.io/docs/prometheus/latest/configuration/alerting/) |
| Fri-Sun | Build: JSON structured logs with trace IDs. Set up alert rules for error rate > 1%. | — |

*Deliverable*: Structured JSON logs with trace IDs. Alerting rules for error rate and latency SLOs.

---

#### Week 16: Docker Compose Multi-Service + Integration Testing

*Objective*: Run your full local stack with docker-compose. Test the entire system end-to-end.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Docker Compose — multi-container setup, networks, volumes, depends_on | [Docker Compose Documentation](https://docs.docker.com/compose/) |
| Wed | Integration testing with Testcontainers + Kafka/RabbitMQ modules | [Testcontainers — Kafka Module](https://testcontainers.com/modules/kafka/) |
| Thu | Testing message-driven systems — @EmbeddedKafka, consumer tests | [Baeldung — Embedded Kafka](https://www.baeldung.com/kafka-testing) |
| Fri-Sun | Build: Docker Compose with Phase 1 app + PostgreSQL + Redis + Kafka + consumer. Integration tests. | — |

*Deliverable*: `docker-compose up` runs the full local stack. Integration tests cover the event-driven flow.

---

## Phase 3 — Months 5-6: Cloud-Native Full Stack

**Goal**: Take a user auth service (extending Phase 1/2 work) through a full cloud deployment pipeline. This is your portfolio centrepiece.

### Month 5 — Kubernetes + CI/CD

---

#### Week 17: Kubernetes Fundamentals

*Objective*: Understand Kubernetes core concepts. Deploy your app to a local Kubernetes cluster.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Kubernetes architecture — pods, deployments, services, nodes | [Kubernetes Basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/) |
| Wed | Kubernetes deployments — rolling updates, health checks, resource limits | [Kubernetes — Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) |
| Thu | Kubernetes services — ClusterIP, NodePort, LoadBalancer, Ingress | [Kubernetes — Services](https://kubernetes.io/docs/concepts/services-networking/service/) |
| Fri-Sun | Build: Deploy Phase 1 app to minikube. Expose via LoadBalancer service. | [minikube Documentation](https://minikube.sigs.k8s.io/docs/) |

*Deliverable*: Phase 1 app running in minikube with a LoadBalancer service.

---

#### Week 18: Helm + Environment Promotion

*Objective*: Use Helm charts for config management. Implement dev/staging/prod promotion.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Helm charts — templates, values, releases, hooks | [Helm Documentation](https://helm.sh/docs/) |
| Wed | ConfigMaps and secrets — managing configuration in Kubernetes | [Kubernetes — ConfigMaps and Secrets](https://kubernetes.io/docs/concepts/configuration/) |
| Thu | Environment promotion — config per environment, image tags, rollout strategies | [ArgoCD — GitOps](https://argoproj.github.io/cd/) |
| Fri-Sun | Build: Create Helm chart for Phase 1 app. Dev/staging/prod values files. | — |

*Deliverable*: Helm chart with dev/staging/prod values files. Zero-downtime rollout strategy configured.

---

#### Week 19: GitHub Actions CI/CD

*Objective*: Build a full CI/CD pipeline — test, build, push, deploy.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | GitHub Actions — workflows, jobs, steps, matrix builds | [GitHub Actions Documentation](https://docs.github.com/en/actions) |
| Wed | Docker build/push — multi-stage builds, registry authentication | [GitHub Actions — Docker](https://docs.github.com/en/actions/publishing-images) |
| Thu | Deploying to Kubernetes from GitHub Actions — kubeconfig, helm upgrade | [GitHub Actions — Kubernetes](https://github.com/marketplace/actions/kubernetes) |
| Fri-Sun | Build: CI pipeline (test + build + push). CD pipeline (helm upgrade on main). | — |

*Deliverable*: On every push to main: tests run → Docker image built → pushed to GHCR → Helm chart deployed to staging.

---

#### Week 20: Infrastructure as Code

*Objective*: Define your infrastructure in code. Make it reproducible and version-controlled.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | IaC principles — state management, drift detection, idempotency | [Terraform Documentation](https://www.terraform.io/docs) |
| Wed | Terraform with Kubernetes — provision EKS/GKE, manage resources as code | [Terraform — Kubernetes Provider](https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs) |
| Thu | Pulumi — infrastructure as code in Kotlin/TypeScript/Python | [Pulumi Documentation](https://www.pulumi.com/docs/) |
| Fri-Sun | Build: Terraform/Pulumi script to provision a Kubernetes cluster + RDS on AWS | — |

*Deliverable*: IaC script to provision full cloud environment. Tested destroy/recreate cycle.

---

### Month 6 — Cloud Deployment + Portfolio Polish

---

#### Week 21: Cloud Deployment

*Objective*: Deploy to a real cloud provider. Go beyond the free tier preview.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | AWS EKS or GKE — managed Kubernetes, node groups, autoscaling | [AWS EKS Documentation](https://docs.aws.amazon.com/eks/) |
| Wed | Railway/Render full deployment — with persistent database and Redis | [Railway Documentation](https://docs.railway.app/) |
| Thu | Flyway migrations in production — zero-downtime deployment patterns | [Flyway — Production](https://flywaydb.org/documentation/) |
| Fri-Sun | Deploy Phase 1 app to cloud. Run Flyway migrations. Verify endpoints. | — |

*Deliverable*: Live production deployment on AWS EKS or Railway with managed PostgreSQL + Redis.

---

#### Week 22: Load Testing + Autoscaling

*Objective*: Verify your system handles load. Configure autoscaling to match demand.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | Load testing — k6 script writing, virtual users, think time | [k6 Documentation](https://k6.io/docs/) |
| Wed | Kubernetes autoscaling — HPA (Horizontal Pod Autoscaler), VPA, cluster autoscaler | [Kubernetes — HPA](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/) |
| Thu | Database connection pooling — HikariCP tuning, PgBouncer | [Baeldung — HikariCP](https://www.baeldung.com/hikaricp) |
| Fri-Sun | Build: k6 load test against staging. Configure HPA. Tune connection pool. | — |

*Deliverable*: Load test results showing latency/p99 at 100 concurrent users. HPA configured and tested.

---

#### Week 23: Security Hardening

*Objective*: Lock down your deployment. Security is a portfolio differentiator.

| Day | Topic | Resource |
|-----|-------|----------|
| Tue | HTTPS/TLS — cert-manager for Kubernetes, Let's Encrypt | [cert-manager Documentation](https://cert-manager.io/docs/) |
| Wed | Secrets management — Kubernetes Secrets vs HashiCorp Vault, external secrets operator | [Kubernetes — Secrets](https://kubernetes.io/docs/concepts/configuration/secret/) |
| Thu | IAM least privilege — service accounts, RBAC, network policies | [AWS — IAM Best Practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html) |
| Fri-Sun | Build: HTTPS enabled via cert-manager. All secrets via external secrets. Network policies restrict pod-to-pod traffic. | — |

*Deliverable*: HTTPS endpoints, secrets managed externally, network policies enforcing least privilege.

---

#### Week 24: Portfolio Polish + Interview Prep

*Objective*: Turn everything into a portfolio story. Practice system design interviews.

| Day | Topic | Resource |
|-----|-------|----------|
| Mon | Portfolio README deep-dive — architecture diagrams, decisions, trade-offs | [Stripe — API Design](https://stripe.com/blog/working-with-apis) |
| Tue | System design practice — design a notification system, design a URL shortener | [Excalidraw — Diagrams](https://excalidraw.com/) |
| Wed | Behavioral prep — stories about Phase 1-3 work, decisions, failures, learning | [GitHub Profile README Guide](https://docs.github.com/en/account-and-profile/setting-up-and-managing-your-github-profile/customizing-your-profile/managing-your-profile-readme) |
| Thu | Mock interviews — trade coding challenges for system design discussions | [Pramp — System Design](https://www.pramp.com/) |
| Fri | Polish GitHub profile — pinned repos, contribution graph, project documentation | [GitHub Profile README](https://docs.github.com/en/account-and-profile/setting-up-and-managing-your-github-profile/customizing-your-profile/managing-your-profile-readme) |
| Sat-Sun | Final deployment check, record a short walkthrough video, update LinkedIn | — |

*Deliverable*: GitHub profile with 3 pinned backend repos. LinkedIn updated with backend skills. 2-3 system design stories rehearsed.

---

## Resource Summary

### Kotlin & Spring

| Resource | URL |
|----------|-----|
| Kotlin Coroutines | https://kotlinlang.org/docs/coroutines.html |
| Building REST Services with Spring | https://spring.io/guides/tutorials/rest/ |
| Spring Boot Reference | https://docs.spring.io/spring-boot/reference/ |
| Spring Data JPA Reference | https://docs.spring.io/spring-data/jpa/reference/ |
| Spring Security Reference | https://docs.spring.io/spring-security/reference/ |
| Spring Kafka Reference | https://docs.spring.io/spring-kafka/reference/ |
| Spring Initializr | https://start.spring.io/ |

### Testing

| Resource | URL |
|----------|-----|
| Testcontainers Getting Started | https://testcontainers.com/getting-started/ |
| Testing Spring Boot REST API | https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/ |
| Testcontainers Java Guide | https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/ |
| Replace H2 with Real Database | https://testcontainers.com/guides/replace-h2-with-real-database-for-testing/ |
| Embedded Kafka Testing | https://www.baeldung.com/kafka-testing |

### Architecture & Patterns

| Resource | URL |
|----------|-----|
| Martin Fowler — Event Sourcing | https://martinfowler.com/articles/201701-event-sourcing.html |
| Martin Fowler — Saga Pattern | https://martinfowler.com/articles/201905-saga.html |
| Outbox Pattern | https://microservices.io/patterns/data/transactional-outbox.html |
| Stripe — Working with APIs | https://stripe.com/blog/working-with-apis |

### Infrastructure & Cloud

| Resource | URL |
|----------|-----|
| Docker — Spring Boot | https://docs.docker.com/language/java/ |
| Kubernetes Basics | https://kubernetes.io/docs/tutorials/kubernetes-basics/ |
| Helm Documentation | https://helm.sh/docs/ |
| GitHub Actions | https://docs.github.com/en/actions |
| Terraform | https://www.terraform.io/docs |
| cert-manager | https://cert-manager.io/docs/ |
| k6 Load Testing | https://k6.io/docs/ |

### Monitoring & Observability

| Resource | URL |
|----------|-----|
| OpenTelemetry | https://opentelemetry.io/docs/concepts/observability-primer/ |
| Micrometer | https://micrometer.io/docs/ |
| Prometheus Alerting | https://prometheus.io/docs/prometheus/latest/configuration/alerting/ |

### Reference & Learning

| Resource | URL |
|----------|-----|
| JWT.io | https://jwt.io/introduction/ |
| Vlad Mihalcea Hibernate Tutorials | https://vladmihalcea.com/tutorials/hibernate/ |
| Baeldung — Spring Boot | https://www.baeldung.com/ |
| RabbitMQ Tutorials | https://www.rabbitmq.com/tutorials/tutorial-one-python.html |
| Kafka Getting Started | https://kafka.apache.org/documentation/#gettingStarted |
| Flyway Documentation | https://flywaydb.org/documentation/ |

---

## Success Criteria at 6 Months

| # | Criteria | How to Verify |
|---|----------|---------------|
| 1 | Portfolio of 2-3 deployed backend services on GitHub | Live URLs + source repos |
| 2 | Swagger docs for every API | /swagger-ui on each deployed service |
| 3 | Full CI/CD pipeline for each project | GitHub Actions passing on every push |
| 4 | Can explain why Spring Boot auto-configures certain beans | Conversation-ready, not just tutorial-following |
| 5 | Event-driven architecture implemented in production-like setting | Phase 2 notification pipeline with Kafka |
| 6 | Cloud-deployed service with monitoring | Prometheus metrics + alerting dashboards |
| 7 | Interview-ready system design stories | 3-5 stories about decisions made during Phases 1-3 |
| 8 | GitHub profile positions you as backend engineer | Pinned repos, updated bio, contribution graph |

---

## Weekly Rhythm Template

| Day | Focus |
|-----|-------|
| Tuesday | New concept study — docs, targeted tutorials |
| Wednesday | New concept study + planning the build |
| Thursday | Start implementation — feature branch, TDD approach |
| Friday | Continue implementation |
| Saturday | Implementation + commit |
| Sunday | Code review your own work, write what-I-learned notes |

**Bi-weekly**: 1-hour retrospective — what clicked, what's fuzzy, adjust next week's focus.
