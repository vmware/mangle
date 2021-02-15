import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';
import { CommonConstants } from 'src/app/common/common.constants';
@Component({
    selector: 'db-fault-endpoint',
    templateUrl: './db-endpoint.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class DbEndpointComponent {

    constructor() { }

    @Input() faultFormData: any;
    @Input() endpoints: any;
    @Input() supportedEpTypes: any;
    @Input() supportedDbTypes: any;
    showSelectRandomEndpoint: boolean = false;
    @Output() displayEndpointFieldsEvent = new EventEmitter<string>();
    @Output() dockerContainersEvent = new EventEmitter<string>();
    public searchedEndpoints: any = [];

    ngOnInit() {
        if (this.faultFormData.randomEndpoint != null) {
            this.showSelectRandomEndpoint = true;
        }
    }

    public searchEndpoint(searchKeyWord: any) {
        this.searchedEndpoints = [];
        for (var i = 0; i < this.endpoints.length; i++) {
            if (this.endpoints[i].name.indexOf(searchKeyWord) > -1) {
                this.searchedEndpoints.push(this.endpoints[i]);
            }
        }
    }

    public setEndpointVal(endpointVal: any) {
        this.faultFormData.endpointName = endpointVal;
    }

    public setRandomEndpointVal(randomEndpointVal: any) {
        this.faultFormData.randomEndpoint = randomEndpointVal;
    }

    public displayEndpointFields(endpointVal: any) {
        this.displayEndpointFieldsEvent.emit(endpointVal);
    }

    public getDockerContainers(endpointVal: any) {
        this.dockerContainersEvent.emit(endpointVal);
    }

    public setShowSelectRandomEndpoint(endpointType: any) {
        console.log(endpointType + ":" + CommonConstants.ENDPOINT_GROUP);
        if (endpointType == CommonConstants.ENDPOINT_GROUP) {
            this.showSelectRandomEndpoint = true;
            this.faultFormData.randomEndpoint = true;
        } else {
            this.showSelectRandomEndpoint = false;
        }
    }

}

