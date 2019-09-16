import { Component, OnInit } from '@angular/core';
import { SharedService } from '../shared.service';

@Component({
    selector: 'app-events',
    templateUrl: './events.component.html'
})
export class EventsComponent implements OnInit {

    constructor(private sharedService: SharedService) { }

    public appEvents: any;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getAppEvents();
    }

    public getAppEvents() {
        this.isLoading = true;
        this.sharedService.getAppEvents().subscribe(
            res => {
                if (res.response != null) {
                    this.appEvents = res.response;
                    this.isLoading = false;
                } else {
                    this.appEvents = [];
                    this.isLoading = false;
                }
            }, err => {
                this.appEvents = [];
                this.isLoading = false;
            });
    }

}
