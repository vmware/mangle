import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {ClarityModule} from '@clr/angular';
import {ScheduledComponent} from './scheduled/scheduled.component';
import {ProcessedComponent} from './processed/processed.component';
import {RequestsComponent} from './requests.component';
import {SharedModule} from 'src/app/shared/shared.module';
import {RouterModule} from '@angular/router';
import {RequestsRoutingModule} from './requests-routing.module';

@NgModule({
  declarations: [RequestsComponent, ScheduledComponent, ProcessedComponent],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    SharedModule,
    RouterModule,
    RequestsRoutingModule
  ]
})
export class RequestsModule {
}
