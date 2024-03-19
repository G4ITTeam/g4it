/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import * as echarts from "echarts/core";
import { PieChart, BarChart } from "echarts/charts";
import {
    GridComponent,
    TooltipComponent,
    LegendComponent,
    PolarComponent,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

echarts.use([
    PieChart,
    BarChart,
    GridComponent,
    TooltipComponent,
    PolarComponent,
    LegendComponent,
    CanvasRenderer,
]);

export default echarts;
