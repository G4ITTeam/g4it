import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: "integer",
})
export class IntegerPipe implements PipeTransform {
    transform(value: number): string {
        if(value === undefined) return '';

        if(typeof value === 'string'){
            value = parseFloat(value)
        }
        if (value < 1) {
            return "< 1";
        } else {
            return value.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, " ");
        }
    }
}
