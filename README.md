# Quarkus-Gate

##### a FOO FACTORY Production 2025

## This is a quarkus features demo.

### _It is categorically not to be used as a lunar landing module, even if it could, with minimal changes._

## Quarkus Features Table

| Feature                    | Project                                     | File                                   | Note                                                                                                                              |
|----------------------------|---------------------------------------------|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| DEV-UI                     | any                                         | -                                      | ./mvnw quarkus:dev  // http://localhost:8080/q/dev-ui                                                                             |
| TESTING                    | party-service                               | test/*                                 |                                                                                                                                   |
| DB Access - active record  | party-service                               | Game                                   | PanacheEntityBase.persist()                                                                                                       |
| DB Access - repository     | camp-service                                | CampRepository                         |                                                                                                                                   |
| DB Access - rest data      | party-service                               | PartyCrudResource                      |                                                                                                                                   |
| DB Access - mongo reactive | battle-service                              | Battle                                 | extends ReactivePanacheMongoEntity                                                                                                |
| Reactive Programming       | party-service                               | GameResource                           | Uni<Game> play(Game game) // anywhere Uni/Multi is used reactive programming is involved                                          |
| Imperative Messaging       | party-service                               | PartyMemberResource                    | battleRequestEmitter                                                                                                              |
| Reactive Messaging         | match-making, battle-service, party-service | BattleRequestProcessor                 |                                                                                                                                   |
| %prod                      | *                                           |                                        | clean container pod & images // in user-service ./start-infra.txt // build * prod // run prod                                     |
| Health                     | camp-service                                | CustomHealthCheck                      | http://localhost:8083/q/health  // http://localhost:8083/q/health/well // /started /ready                                         |
| Metrics - java             | party-service                               | GameResource                           | counted games started -  http://localhost:8081/q/metrics                                                                          |
| Metrics - grafana          | -                                           | -                                      | http://localhost:3000 / admin / admin   // local test svc:  and run all in prod mode                                              |
| Metrics - prometheus       | -                                           | -                                      | http://localhost:9090/graph?g0.expr=method_counted_total&g0.tab=1&g0.display_mode=lines&g0.show_exemplars=0&g0.range_input=1h     |
| Tracing                    | Jaeger ui                                   | -                                      | http://localhost:16686                                                                                                            |
| Fault Tolerance            | user-service                                | PartyMemberClient                      | @Retry & @Fallback                                                                                                                |
| graphQL UI & gRPC          | camp-service                                | -                                      | http://localhost:8083/q/graphql-ui  {heroes{id,name,classe}}   OR http://localhost:8083/q/dev-ui/io.quarkus.quarkus-grpc/services |
| Run in Prod Mode           | user-service                                | application.properties                 | java -jar target/quarkus-app/quarkus-run.jar                                                                                      |
| Image build                | user-service                                | application.properties                 | add quarkus-container-image-podman // ./mvnw package -Dquarkus.container-image.build=true -D quarkus.container-image.push=true    |
| Deploy openshift           | user-service                                | application.properties                 | quarkus ext add openshift // build (clean package) // oc apply -f target/kubernetes/openshift.yml                                 |
| Serverless App             | hero-statistics                             | HeroStatistics, application.properties | @Funq                                                                                                                             |

##### Copyright Information

Most images are taken by https://bg3.wiki/ and follows their copyright rules https://bg3.wiki/wiki/bg3wiki:Copyrights