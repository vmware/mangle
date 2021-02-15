import { Component, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { QueryService } from './query.service';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-query',
    templateUrl: './query.component.html'
})
export class QueryComponent implements OnInit {

    public isLoading = true;
    public allQueries: any = [];
    public alertMessage: string;
    public isErrorMessage: boolean;
    public queryFormData: any;
    public disableSubmit = false;
    public queryDatagrid = false;
    public addEdit: string;

    public selectedQuery: any;
    public selectedQueries: any = [];
    public queryForm = true;

    public addDisabled = false;
    public editDisabled = true;
    public deleteDisabled = true;
    public enableDisabled = true;
    public disabledDisabled = true;

    constructor(private queryService: QueryService) {
    }

    ngOnInit() {
        this.getAllQueries();
    }

    public getAllQueries() {
        this.isLoading = true;
        this.queryService.getAllQueries().subscribe(
            res => {
                if (res != null) {
                    this.allQueries = res.content;
                } else {
                    this.allQueries = [];                  
                }
                this.isLoading = false;
            }, err => {
                this.allQueries = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public populateQueryForm(queryData: any) {
        this.disableSubmit = false;
        this.queryFormData = queryData;
    }

    public addOrUpdateQuery(query: any, action: string) {
        if (action === 'Add') {
            this.addQuery(query);
        } else {
            this.updateQuery(query);
        }
    }

    public addQuery(query: any) {
        this.isLoading = true;
        this.queryService.addQuery(query).subscribe(
            res => {
                this.getAllQueries();
                this.isErrorMessage = false;
                this.alertMessage = query.name + MessageConstants.QUERY_CREATED;
                this.isLoading = false;
            }, err => {
                this.getAllQueries();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateQuery(query: any) {
        this.isLoading = true;
        this.queryService.updateQuery(query).subscribe(
            res => {
                this.getAllQueries();
                this.isErrorMessage = false;
                this.alertMessage = query.name + MessageConstants.QUERY_UPDATED;
                this.isLoading = false;
            }, err => {
                this.getAllQueries();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }


    public deleteQuery(query: any) {
        const selectedMetricConfigNames = [];
        if (query == undefined) {
            this.alertMessage = MessageConstants.QUERY_NOT_SELECTED;
        } else {
            if (confirm(MessageConstants.DELETE_CONFIRM + query.name + MessageConstants.QUESTION_MARK)) {
                this.isLoading = true;
                this.queryService.deleteQuery(query.name).subscribe(
                    res => {
                        this.getAllQueries();
                        this.isErrorMessage = false;
                        this.alertMessage = query.name + MessageConstants.QUERY_DELETED;
                        this.isLoading = false;
                    }, err => {
                        this.getAllQueries();
                        this.isErrorMessage = true;
                        this.alertMessage = err.error.description;
                        this.isLoading = false;
                        if (this.alertMessage === undefined) {
                            this.alertMessage = err.error.error;
                        }
                    });
            }
        }
    }

    public updateActionButtons() {
        if (this.selectedQuery !== undefined) {
            this.addDisabled = this.selectedQuery.length >= 1;
            this.editDisabled = this.selectedQuery.length === 0 || this.selectedQuery.length > 1;
            this.deleteDisabled = this.selectedQuery.length === 0;
        }
    }
}
