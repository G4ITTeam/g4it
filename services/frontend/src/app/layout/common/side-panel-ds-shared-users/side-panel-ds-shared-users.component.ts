import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { TranslateModule } from "@ngx-translate/core";
import { SidebarModule } from "primeng/sidebar";
import { DigitalServiceUserInfo } from "src/app/core/interfaces/digital-service.interfaces";

@Component({
    selector: "app-side-panel-ds-shared-users",
    standalone: true,
    imports: [SidebarModule, CommonModule, TranslateModule],
    templateUrl: "./side-panel-ds-shared-users.component.html",
})
export class SidePanelDsSharedUsersComponent {
    @Input({ required: true }) visible!: boolean;
    @Input({ required: true }) creator?: DigitalServiceUserInfo;
    @Input({ required: true }) members!: DigitalServiceUserInfo[];

    @Output() sidenavClose: EventEmitter<boolean> = new EventEmitter();

    sidenavClosed(): void {
        this.sidenavClose.emit(false);
    }
}
