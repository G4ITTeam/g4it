import { Component, inject } from "@angular/core";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-spinner",
    templateUrl: "./spinner.component.html",
    styleUrls: ["./spinner.component.css"],
})
export class SpinnerComponent {
    protected store = inject(GlobalStoreService);
}
