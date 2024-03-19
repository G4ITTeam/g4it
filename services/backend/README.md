# Green IT

A java web application using **Spring Boot 3.1.2** and running with **JRE 17**.


#### On Integration

##### Urls

- Azure Portal : https://portal.azure.com/
- Front end : https://main-inventory-g4it-inventory.1f6d283158e541f58996.westeurope.aksapp.io/overgreen/inventories
- Back end : https://main-inventory-g4it-inventory.1f6d283158e541f58996.westeurope.aksapp.io/api/
- Swagger : https://main-inventory-g4it-inventory.1f6d283158e541f58996.westeurope.aksapp.io/api/swagger-ui/index.html

##### DataBase Access

A pod containing a PgAdmin is available, and it is possible to connect to it!

After selecting the Kubernetes service **aksg4it1dev01**, do port forwarding :
```shell
kubectl -n g4it-inventory port-forward svc/pgadmin-service 8080:80
```

You can now access to pgAdmin on url http://localhost:8080/

| Parameter         | Value                                                                                                       |
|-------------------|-------------------------------------------------------------------------------------------------------------|
| User              | admin-g4it@soprasteria.com                                                                                  |
| Password          | You can ask this value to a colleague ;)                                                                    |
| Database Password | You can find in secret on Azure Portal (Configuration > Secrets > password (Clic on the eye to decode value |

##### Logs

It is possible to display the logs of a pod from a Windows Terminal

After selecting the Kubernetes service **aksg4it1dev01**, list pods:

```shell
kubectl -n g4it-inventory get po
```

Ouput Example
```shell
NAME                                                 READY   STATUS    RESTARTS   AGE
api-event-calcul-77869666f8-xdfdt                    1/1     Running   0          8h
api-event-donneesentrees-7774468fc5-b95qr            1/1     Running   0          8h
api-event-enrichissement-85cd896679-mlqpf            1/1     Running   0          8h
api-event-indicateurs-7cbc65b75f-8rckj               1/1     Running   0          8h
api-exposition-donneesentrees-bbd8dd4b5-4zkpn        1/1     Running   0          8h
api-referentiel-b7b98bdc-vmx9g                       1/1     Running   0          8h
main-inventory-overgreen-backend-578fd8c468-pl46t    1/1     Running   0          115m
main-inventory-overgreen-frontend-64676dd755-8js97   1/1     Running   0          115m
numecoeval-kafka-0                                   1/1     Running   0          2d18h
pgadmin-0                                            1/1     Running   0          2d19h
```

Display backend logs :
```shell
kubectl -n g4it-inventory logs -f main-inventory-overgreen-backend-578fd8c468-pl46t
```

## Contributing

### Definition of Ready

- [ ] The story is linked to an epic
- [ ] The story is positioned in the user journey
- [ ] Models are defined and formalized
- [ ] The nominal test case is formalized with the associated dataset
- [ ] Limit cases have been considered (initialization, error cases, authorizations, permissions) and are documented
- [ ] A technical referent is identified on the story
- [ ] The story has been shared in Refinement Backlog and we have a technical design draft
- [ ] Dependencies are identified (stories / enablers)
- [ ] The story has been encrypted by the Dev Team

### Definition of Done

- [ ] The implementation respects the management rules and models
- [ ] Unit tests are implemented and pass
- [ ] The story has been tested locally
- [ ] The story has been tested on the integration environment
- [ ] There was at least one approval on the merge request (== code review performed)
- [ ] The code is merged with an undegraded quality gate (code coverage > 75%)
- [ ] The structuring elements are documented (at the discretion of the reviewer)
