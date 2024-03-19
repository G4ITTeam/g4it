/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
export abstract class Constants {
    static readonly COLOR = [
        "#355c7a",
        "#00baff",
        "#08e4ea",
        "#7aaacf",
        "#4c7c96",
        "#9fb4d4",
        "#0cb9bf",
        "#00a0c4",
        "#375560",
        "#6a8bb5",
        "#06d4da",
        "#00a5cc",
        "#406981",
        "#7f9ecf",
        "#0ae5eb",
        "#00c1e7",
        "#305a74",
        "#688fad",
        "#07dbe3",
        "#00b3cf",
        "#3c637f",
        "#769fc9",
        "#09e5eb",
        "#00bbd1",
        "#435c71",
        "#7eacd4",
        "#08e8ee",
        "#00c5d9",
        "#3e5f79",
        "#7791c4",
    ];
    static readonly GREEN_COLOR_SET = ["#FFF6B0", "#ADDC6F", "#2FA255", "#006734"];
    static readonly PURPLE_COLOR_SET = [
        "#E8CBFF",
        "#CC8CFF",
        "#9747FF",
        "#6C00FF",
        "#E0A0FF",
        "#B76FFF",
    ];
    static readonly YELLOW_COLOR = "#FFBD00";
    static readonly BLUE_COLOR = "#00B2FF";
    static readonly UNSPECIFIED = "(Unspecified)";

    static readonly INTEGRATION_BATCH_COMPLETED_FAILED_STATUSES = [
        "COMPLETED",
        "COMPLETED_WITH_ERRORS",
        "FAILED",
        "FAILED_HEADERS",
    ];

    static readonly EVALUATION_BATCH_COMPLETED_FAILED_STATUSES = [
        "COMPLETED",
        "COMPLETED_WITH_ERRORS",
        "FAILED",
    ];

    static readonly EVALUATION_BATCH_RUNNING_STATUSES = [
        "UNKNOWN",
        "STARTED",
        "STARTING",
        "DATA_EXTRACTION",
        "DATA_EXPOSITION_TO_NUMECOVAL",
        "CALCUL_SUBMISSION_TO_NUMECOVAL",
        "CALCUL_IN_PROGRESS",
    ];

    static readonly INVENTORY_TYPE = {
        INFORMATION_SYSTEM: "INFORMATION_SYSTEM",
        SIMULATION: "SIMULATION"
    };
}
