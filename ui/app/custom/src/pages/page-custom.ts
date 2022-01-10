import {css, html, TemplateResult, unsafeCSS} from "lit";
import {customElement, property, query} from "lit/decorators.js";

import manager, {AssetModelUtil, Util} from "@openremote/core";
import {AppStateKeyed, Page, PageProvider} from "@openremote/or-app";
import {EnhancedStore} from "@reduxjs/toolkit";
import { Asset, AssetQuery, AttributeEvent, ReadAttributeEvent, SharedEvent} from "@openremote/model";

export function pageCustomProvider(store: EnhancedStore<AppStateKeyed>): PageProvider<AppStateKeyed> {
    return {
        name: "customPage",
        routes: [
            "custom1"
        ],
        pageCreator: () => {
            const page = new PageCustom(store);
            return page;
        }
    };
}


@customElement("page-custom")
export class PageCustom extends Page<AppStateKeyed>  {

    static get styles() {
        // language=CSS
        return css`
            :host {
                display: flex;
                align-items: start;
                flex-direction: column;
                width: 100%;
                --or-icon-fill: var(--or-app-color4);
            }
        `;
    }

    get name(): string {
        return "custom";
    }

    constructor(store: EnhancedStore<AppStateKeyed>) {
        super(store);
    }

    protected render(): TemplateResult | void {
        return html`
            <p>This is an example custom page with a custom app translation for 'customString' <b><or-translate value="customString"></or-translate></p></b>
        `
    }

    stateChanged(state: AppStateKeyed): void {

    }
}
