import { Renderer2, RendererFactory2 } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { MatomoScriptService } from "./matomo-script.service";

describe("MatomoScriptService", () => {
    let service: MatomoScriptService;
    let rendererFactory: RendererFactory2;
    let renderer: Renderer2;

    beforeEach(() => {
        rendererFactory = jasmine.createSpyObj("RendererFactory2", ["createRenderer"]);
        renderer = jasmine.createSpyObj("Renderer2", ["createElement", "appendChild"]);
        (rendererFactory.createRenderer as jasmine.Spy).and.returnValue(renderer);

        TestBed.configureTestingModule({
            providers: [
                MatomoScriptService,
                { provide: RendererFactory2, useValue: rendererFactory },
            ],
        });

        service = TestBed.inject(MatomoScriptService);
    });

    it("should append script to document head", () => {
        const matomoTagManagerUrl = "https://example.com/matomo.js";
        const scriptElement = document.createElement("script");
        (renderer.createElement as jasmine.Spy).and.returnValue(scriptElement);

        service.appendScriptToHead(matomoTagManagerUrl);

        expect(renderer.createElement).toHaveBeenCalledWith("script");
        expect(scriptElement.type).toBe("text/javascript");
        expect(scriptElement.async).toBeTrue();
        expect(scriptElement.text).toContain(matomoTagManagerUrl);
        expect(renderer.appendChild).toHaveBeenCalledWith(document.head, scriptElement);
    });
});
