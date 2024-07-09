import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { FileDescription } from "../../interfaces/file-system.interfaces";

const endpoint = Constants.ENDPOINTS.templateFiles;

@Injectable({
    providedIn: "root",
})
export class TemplateFileService {
    constructor(private http: HttpClient) {}

    getTemplateFiles(): Observable<FileDescription[]> {
        return this.http.get<FileDescription[]>(endpoint);
    }

    downloadTemplateFile(fileName: string): Observable<any> {
        if (fileName.includes(".xlsx")) {
            return this.http.get(`${endpoint}/${fileName}`, {
                responseType: "blob",
                headers: { Accept: "application/vnd.ms-excel" },
            });
        }
        if (fileName.includes(".zip")) {
            return this.http.get(`${endpoint}/${fileName}`, {
                responseType: "blob",
                headers: { Accept: "application/zip" },
            });
        }
        return this.http.get(`${endpoint}/${fileName}`, {
            responseType: "blob",
            headers: { Accept: "text/csv" },
        });
    }
}
