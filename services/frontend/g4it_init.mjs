import fs from "fs";

let content = fs.readFileSync("src/environments/environment.ts", "utf-8");

const clientId = process.env.AZURE_CLIENT_ID;
if (clientId === undefined) {
    console.error("You must set the env variable AZURE_CLIENT_ID");
    console.error("KO")
} else {
    fs.writeFileSync(
        "src/environments/environment.local.ts",
        content.replaceAll("${AZURE_CLIENT_ID}", clientId),
    );
    console.log("Written file src/environments/environment.local.ts successfully");
    console.log("OK")
}
