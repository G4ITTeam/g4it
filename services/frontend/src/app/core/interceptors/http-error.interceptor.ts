/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpStatusCode,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { NgxSpinnerService } from "ngx-spinner";
import { Message, MessageService } from "primeng/api";
import { Observable, throwError } from "rxjs";
import { catchError, retry } from "rxjs/operators";

@Injectable({
    providedIn: "root",
})
export class HttpErrorInterceptor implements HttpInterceptor {
    handledErrorStatus = [
        HttpStatusCode.Forbidden,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.BadRequest,
        HttpStatusCode.RequestTimeout,
        HttpStatusCode.GatewayTimeout,
        HttpStatusCode.InternalServerError,
        HttpStatusCode.ServiceUnavailable,
    ];

    constructor(
        public router: Router,
        private messageService: MessageService,
        private spinner: NgxSpinnerService
    ) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            retry(1),
            catchError((returnedError) => {
                this.handleErrorPageNavigation(returnedError.status);
                return throwError(() => new Error(this.getErrorMessage(returnedError)));
            })
        );
    }

    private getErrorMessage(error: any): string {
        let errorMessage = "Unexpected problem occurred";
        if (error instanceof HttpErrorResponse) {
            errorMessage = `Error Status ${error.status}: ${error.error.error} - ${error.error.message}`;
        }
        return errorMessage;
    }

    private handleErrorPageNavigation(errorStatus: HttpStatusCode) {
        if (this.handledErrorStatus.includes(errorStatus)) {
            this.router.navigate(["/something-went-wrong", errorStatus]);
        } else if (errorStatus == HttpStatusCode.PayloadTooLarge) {
            this.addToastMessage(
                "Maximum file size exceeded",
                "The maximum file size for import is 100MB"
            );
        }
    }

    addToastMessage(title: string, message: string) {
        const errMsg: Message = {
            severity: "error",
            summary: title,
            detail: message,
            sticky: true,
        };
        this.messageService.add(errMsg);
        this.spinner.hide();
    }
}
