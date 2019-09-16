import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
    selector: 'app-cluster',
    templateUrl: './cluster.component.html'
})
export class ClusterComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public clusterConfig: any = {
        "id": null,
        "clusterName": null,
        "validationToken": null,
        "members": []
    };

    public spinnerActive: boolean = false;

    public errorAlertMessage: string;
    public successAlertMessage: string;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    ngOnInit() {
        this.getCluster();
    }

    public getCluster() {
        this.spinnerActive = true;
        this.settingService.getCluster().subscribe(
            res => {
                delete res["_links"];
                this.clusterConfig = res;
                this.spinnerActive = false;
            }, err => {
                this.errorAlertMessage = err.error.description;
                this.spinnerActive = false;
            });
    }

}
