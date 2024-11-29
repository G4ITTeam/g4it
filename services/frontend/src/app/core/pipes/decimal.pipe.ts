import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: "decimals",
})
export class DecimalsPipe implements PipeTransform {
    transform(value: number): string {
        if (value === undefined) return "";
        if (value < 0.01 && value != 0) {
            return value.toExponential(2);
        } else if (value >= 1000) {
            return value.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, " ");
        } else {
            return value.toFixed(2).replace(".00", "");
        }
    }
}
