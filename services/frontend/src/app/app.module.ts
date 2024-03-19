/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import {
    MSAL_GUARD_CONFIG,
    MSAL_INSTANCE,
    MSAL_INTERCEPTOR_CONFIG,
    MsalBroadcastService,
    MsalGuard,
    MsalGuardConfiguration,
    MsalInterceptor,
    MsalInterceptorConfiguration,
    MsalModule,
    MsalRedirectComponent,
    MsalService,
} from "@azure/msal-angular";
import {
    BrowserCacheLocation,
    IPublicClientApplication,
    InteractionType,
    PublicClientApplication,
} from "@azure/msal-browser";
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { TranslateHttpLoader } from "@ngx-translate/http-loader";
import { MessageService, PrimeNGConfig } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { ProgressBarModule } from "primeng/progressbar";
import { environment } from "src/environments/environment";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { ApiInterceptor } from "./core/interceptors/api-request.interceptor";
import { HttpErrorInterceptor } from "./core/interceptors/http-error.interceptor";
import { DatePipe } from "@angular/common";

// Function to load translation files using HttpClient
export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "assets/i18n/", ".json");
}

export function MSALInstanceFactory(): IPublicClientApplication {
    return new PublicClientApplication({
        ...environment.msalConfig,
        cache: {
            cacheLocation: BrowserCacheLocation.LocalStorage,
        },
        system: {
            allowNativeBroker: false,
        },
    });
}

export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
    const protectedResourceMap = new Map<string, Array<string>>();
    protectedResourceMap.set(environment.apiConfig.uri, environment.apiConfig.scopes);
    protectedResourceMap.set(environment.protectedApiRouteUrl, [environment.apiAuth]);

    return {
        interactionType: InteractionType.Redirect,
        protectedResourceMap,
    };
}

export function MSALGuardConfigFactory(): MsalGuardConfiguration {
    return {
        interactionType: InteractionType.Redirect,
        authRequest: {
            scopes: environment.apiConfig.scopes,
        },
    };
}

@NgModule({
    declarations: [AppComponent],
    imports: [
        BrowserModule,
        HttpClientModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient],
            },
        }),
        ToastModule,
        ProgressBarModule,
        MsalModule,
    ],
    providers: [
        MessageService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ApiInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true,
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: MsalInterceptor,
            multi: true,
        },
        {
            provide: MSAL_INSTANCE,
            useFactory: MSALInstanceFactory,
        },
        {
            provide: MSAL_GUARD_CONFIG,
            useFactory: MSALGuardConfigFactory,
        },
        {
            provide: MSAL_INTERCEPTOR_CONFIG,
            useFactory: MSALInterceptorConfigFactory,
        },
        MsalService,
        MsalGuard,
        MsalBroadcastService,
        DatePipe,
    ],
    bootstrap: [AppComponent, MsalRedirectComponent],
})
export class AppModule {
    constructor(private translate: TranslateService) {
        // Set the default language
        const lang = localStorage.getItem("lang") || translate.getBrowserLang() || "en";
        translate.setDefaultLang(lang);
        // Enable automatic language detection
        translate.addLangs(["en", "fr"]);
        translate.use(lang);
        document.querySelector("html")!.setAttribute("lang", lang);
    }
}
