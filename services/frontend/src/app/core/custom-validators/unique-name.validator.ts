import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function uniqueNameValidator(existingNames: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        const name = control.value;
        if (
            existingNames
                ?.map((n) => removeBlankSpaces(n))
                .includes(removeBlankSpaces(name))
        ) {
            return { uniqueName: true };
        }
        return null;
    };
}

export function removeBlankSpaces(str: string): string {
    return (str || "").replaceAll(" ", "");
}
