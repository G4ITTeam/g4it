/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, ReplaySubject, tap } from "rxjs";

import { environment } from "src/environments/environment";
import { Role } from "../../interfaces/roles.interfaces";
import { User } from "../../interfaces/user.interfaces";
const endpoint = environment.apiEndpoints.users;

@Injectable({
    providedIn: "root",
})
export class UserDataService {
    userSubject = new ReplaySubject<User>(1);
    userId: number = 1;
    isSubscriberAdmin: boolean = false;
    isOrgAdmin: boolean = false;
    constructor(private http: HttpClient) { }

    fetchUserInfo(): Observable<User> {
        return this.http.get<User>(`${endpoint}/me`).pipe(
            tap(async (user) => {
                this.userId = user.userId;
                let Adminsubscriber: any;
                let adminIndex: any;
                await user.subscribers.map(res => {
                    if (res.roles.includes(Role.SubscriberAdmin)) {
                        this.isSubscriberAdmin = true;
                        Adminsubscriber = res;
                        adminIndex = res.roles.indexOf(Role.SubscriberAdmin);
                    }
                    res.organizations.map(response => {
                        if (response.roles.includes(Role.OrganizationAdmin)) {
                            this.isOrgAdmin = true;
                        }
                    })
                })

                if (this.isSubscriberAdmin) {
                    user.subscribers.unshift(Adminsubscriber);
                    user.subscribers = [...new Set(user.subscribers)];
                }
                this.userSubject.next(user);
            }),
        );
    }
}
