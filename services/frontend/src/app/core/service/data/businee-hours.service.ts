import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BusinessHours } from "../../interfaces/business-hours.interface";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";

const endpoint = environment.apiEndpoints.businessHours;

@Injectable({
    providedIn: "root",
})
export class BusineeHoursService {
    constructor(private http: HttpClient) {}

    getBusinessHours(): Observable<BusinessHours[]> {
        return this.http.get<BusinessHours[]>(`${endpoint}`);
    }
}
