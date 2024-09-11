import { Injectable } from "@angular/core";
import { Constants } from "src/constants";

@Injectable({
    providedIn: "root",
})
export class FilterService {
    getUpdateSelectedValues(
        selectedValues: string[],
        allPossibleValues: string[],
        selection: string,
    ): string[] {
        // The trick is : selectedValues is already updated
        // We only have to handle the Constants.ALL value manually...
        // Case 1: user toggles the Constants.ALL value
        if (selection === Constants.ALL) {
            // case 1.1 : Select All Countries
            if (selectedValues.includes(Constants.ALL)) return [...allPossibleValues];

            // case 1.2 : Deselect All Countries
            return [];
        }
        // Case 2: user toggles a value other than Constants.ALL
        if (selectedValues.includes(Constants.ALL)) {
            // case 2.1 : All Countries were selected, and we deselect one.
            // we have to deselect Constants.ALL as well
            let result = [...selectedValues];
            result.splice(result.indexOf(Constants.ALL), 1);
            return result;
        }
        if (selectedValues.length === allPossibleValues.length - 1) {
            // case 2.2 : All Countries but one were selected, and we select missing one.
            // we have to select Constants.ALL as well
            return [...allPossibleValues];
        }
        // in all other cases, we just have to let the selectedCountries as is
        return [...selectedValues];
    }
}
