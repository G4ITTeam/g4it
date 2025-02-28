import { Result } from "axe-core";
/** the global error count */
let numberError = 0;
/** the critical error count */
let numberCritical = 0;
/** the serious error count */
let numberSerious = 0;
/** the moderate error count */
let numberModerate = 0;
/** the minor error count */
let numberMinor = 0;
/** the map to save the global error [html, type[]] */
let errorMap = new Map<string, string[]>();
/** array to save the number of error by id and impact, the first element is the total error*/
let typeError: { Type: string; Number: number; Impact: String }[] = [
    { Type: "Total", Number: 0, Impact: "" },
];
/** the map to save for each type the page and the number of error*/
let typeErrorPage = new Map<string, { Page: string; Number: number; Impact: String }[]>();
/** the current page tested */
let page = "";
/** the name of the current scenario */
let scenario = "";
/** The current date */
let date = "";
/** The description of error already analysed */
let description: any;

/**
 * Init the value of the report with the json if it already exist
 *
 * @param scenarioName - The scenario name
 */
export function init(scenarioName: string, mode: string = "one-report-by-test") {
    let today = new Date();
    let dd = String(today.getDate()).padStart(2, "0");
    let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
    let yyyy = today.getFullYear();

    date = mm + "-" + dd + "-" + yyyy;

    if (mode === "one-report-by-test") {
        cy.writeFile("cypress/report/" + date + "/result.json", JSON.stringify({}));
    }

    cy.task("readFileMaybe", "cypress/report/" + date + "/result.json").then((file) => {
        if (!file) return;
        let json = JSON.parse(file as string);
        if (json.numberError) {
            numberError = json.numberError;
            numberCritical = json.numberCritical;
            numberSerious = json.numberSerious;
            numberModerate = json.numberModerate;
            numberMinor = json.numberMinor;
            errorMap = new Map(json.errorMap);
            typeError = json.typeError;
            typeErrorPage = new Map(json.typeErrorPage);
        }
    });

    cy.task("readFileMaybe", "cypress/report/description.json").then((file) => {
        if (!file) return;
        description = JSON.parse(file as string);
    });
    scenario = scenarioName + " : ";
}

/**
 * Save the result of the scenario in a json.
 * Call after the scenario
 */
export function save(mode: string = "one-report-by-test") {
    cy.writeFile(
        "cypress/report/" + date + "/result.json",
        JSON.stringify({
            numberError: numberError,
            numberCritical: numberCritical,
            numberSerious: numberSerious,
            numberModerate: numberModerate,
            numberMinor: numberMinor,
            errorMap: Array.from(errorMap.entries()),
            typeError: typeError,
            typeErrorPage: Array.from(typeErrorPage.entries()),
        }),
    );

    if (mode === "one-report-by-test") {
        report();
    }
}

/**
 * Add a logger for the accessibility test and init the counter of the number of error to create the report.
 *
 * @param violations - The list of error identified by cypress
 */
export function reportA11yViolations(violations: Result[]) {
    violations.forEach(({ id, impact, description, nodes, help }) => {
        let htmlElement = "";
        let counter = 0;
        nodes.forEach(({ html }, index) => {
            // check if the error already exist for the html element (avoid duplicate error)
            if (errorMap.has(html) && errorMap.get(html)?.includes(id)) return;
            else {
                counter++;
                typeError.at(0)!.Number++;
                if (typeError.some((el) => el.Type === id)) {
                    typeError.at(
                        typeError
                            .map((x) => {
                                return x.Type;
                            })
                            .indexOf(id),
                    )!.Number++;
                } else {
                    typeError.push({ Type: id, Number: 1, Impact: impact! });
                }

                //increase the count of the total error and the impact of the error
                numberError++;
                switch (impact) {
                    case "critical":
                        numberCritical++;
                        break;
                    case "serious":
                        numberSerious++;
                        break;
                    case "moderate":
                        numberModerate++;
                        break;
                    default:
                        numberMinor++;
                }
                htmlElement += ", html element" + index + " : " + html;
                if (errorMap.has(html)) errorMap.get(html)?.push(id);
                else errorMap.set(html, [id]);
            }
            if (typeErrorPage.has(id)) {
                let element = typeErrorPage
                    .get(id)
                    ?.find((el) => el.Page === scenario + page);
                if (element) element.Number++;
                else {
                    typeErrorPage.get(id)?.push({
                        Page: scenario + page,
                        Number: counter,
                        Impact: impact!,
                    });
                }
            } else {
                typeErrorPage.set(id, [
                    { Page: scenario + page, Number: counter, Impact: impact! },
                ]);
            }
        });
        if (id) {
            cy.log(`RG2A - ${impact?.toUpperCase()} -  ${id} - ${description}
                ${htmlElement} -- fix : ${help}`);
        }
    });
}

/**
 * Set the title of the page to test.
 *
 * @param page - The page to test
 */
export function setPage(nextPage: string) {
    page = nextPage;
}

/**
 * Create  the report with a global counter of error.
 * Reset the global count for the other scenario.
 *
 * @param type - The type of the test
 */
export function report() {
    let result = [
        { Type: "Error", Number: numberError },
        { Type: "Critical", Number: numberCritical },
        { Type: "Serious", Number: numberSerious },
        { Type: "Moderate", Number: numberModerate },
        { Type: "Minor", Number: numberMinor },
    ];

    const html = `
    <html>
        <head>
            <link rel="stylesheet" href="../result.css">
        </head>
        <body>
            ${buildHtmlTable(result, date, 2)}
            ${buildHtmlTable(typeError, date, 3)}
            ${buildHtmlTableByMap(typeErrorPage, date, description)}
        </body>
    </html>`;

    cy.writeFile("cypress/report/" + date + "/result.html", html);
    numberError = numberCritical = numberSerious = numberModerate = numberMinor = 0;
    cy.writeFile("cypress/report/" + date + "/result.json", "");
}

/**
 * Create a table html from an array
 *
 * @param myList - the list to be converted into a table
 * @param date - the date of the report
 * @param size - the size of list items
 * @returns - a string of the array in html table
 */
function buildHtmlTable(myList: any, date: any, size: number) {
    const columns: string[] = [];
    const headerRow = [];

    //the head of the table
    for (let i = 1; i < myList.length; i++) {
        let rowHash = myList[i];
        for (let key in rowHash) {
            if (!columns.some((x) => x === key)) {
                columns.push(key);
                headerRow.push(`<th scope='col'>${key}</th>`);
            }
        }
    }

    const rows = [];
    // the content of the table
    for (let i = 1; i < myList.length; i++) {
        let row = "";
        for (let colIndex = 0; colIndex < columns.length; colIndex++) {
            let cellValue = myList[i][columns[colIndex]];
            if (cellValue == null) cellValue = "";
            row += "<td>" + cellValue + "</td>";
        }
        rows.push("<tr>" + row + "</tr>");
    }

    return `<table>
                <caption>Result from ${date}</caption>
                <thead>
                    <tr>${headerRow.join("")}</tr>
                </thead>
                <tbody>${rows.join("")}</tbody>
                <tfoot>
                    <tr>    
                        <th scope='row' colspan="${size - 1}">Number of error</th>
                        <td>${myList.at(0).Number || 0}</td>
                    </tr>
                </tfoot>
            </table>
           `;
}

/**
 * Create a table html from a map
 *
 * @param myList - the list to be converted into a table
 * @param date - the date of the report
 * @returns a string of the map in html table
 */
function buildHtmlTableByMap(
    myList: Map<string, { Page: string; Number: number; Impact: String }[]>,
    date: any,
    description: any,
) {
    const rows = [];
    let totalError = 0;
    for (const [key, elements] of myList) {
        let row = `<td rowspan="${elements.length}">${key}</td>`;
        for (const element of elements) {
            let findDescription = "/";
            const arrayDescription = description[String(key)];
            if (arrayDescription) {
                const findElement = arrayDescription.find(
                    (el: any) => el.Page === element.Page,
                );
                if (findElement) {
                    findDescription = findElement.Description;
                }
            }

            row += `
            <td>${element.Page}</td>
            <td>${element.Number}</td>
            <td>${element.Impact}</td>
            <td>${findDescription}</td>
            </tr>
            `;
            totalError += element.Number;
        }
        rows.push("<tr>" + row + "</tr>");
    }

    return `<table>
                <caption>Result from ${date}</caption>
                <thead>
                    <tr>
                        <th scope='col'>Type</th>
                        <th scope='col'>Page</th>
                        <th scope='col'>Number</th>
                        <th scope='col'>Impact</th>
                        <th scope='col'>Description</th>
                    </tr>
                </thead>
                <tbody>${rows.join("")}</tbody>
                <tfoot>
                    <tr>    
                        <th scope='row' colspan="4">Number of error</th>
                        <td>${totalError}</td>
                    </tr>
                </tfoot>
            </table>`;
}
