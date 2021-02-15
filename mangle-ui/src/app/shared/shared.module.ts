import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedComponent } from './shared.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { CronComponent } from './cron/cron.component';
import { EventsComponent } from './events/events.component';
import { AlertComponent } from './alerts/alert.component';
import { CredentialComponent } from './credential/credential.component';
import { JvmArgsComponent } from './jvmproperties/jvmargs.component';
import { DockerArgsComponent } from './dockerargs/dockerargs.component';
import { K8sArgsComponent } from './k8sargs/k8sargs.component';
import { InjectionHomeDirComponent } from './injectionhomedir/injectionhomedir.component';
import { TimeoutmsComponent } from './timeoutms/timeoutms.component';
import { ScheduleComponent } from './schedule/schedule.component';
import { EndpointComponent } from './endpoint/endpoint.component';
import { TagsComponent } from './tags/tags.component';
import { FaultTagsComponent } from './faulttags/faulttags.component';
import { NotifierSelectorComponent } from './notifierselector/notifierselector.component';
import { FaultNotifiersComponent } from './faultnotifiers/faultnotifiers.component';
import { ActionsComponent } from './actions/actions.component';
import { DbEndpointComponent } from './db-endpoint/db-endpoint.component';

@NgModule({
  declarations: [
    SharedComponent,
    AlertComponent,
    EventsComponent,
    CronComponent,
    CredentialComponent,
    JvmArgsComponent,
    DockerArgsComponent,
    K8sArgsComponent,
    InjectionHomeDirComponent,
    TimeoutmsComponent,
    ScheduleComponent,
    EndpointComponent,
    TagsComponent,
    FaultTagsComponent,
    NotifierSelectorComponent,
    FaultNotifiersComponent,
    ActionsComponent,
    DbEndpointComponent
  ],
  exports: [
    CronComponent,
    EventsComponent,
    AlertComponent,
    CredentialComponent,
    JvmArgsComponent,
    DockerArgsComponent,
    K8sArgsComponent,
    InjectionHomeDirComponent,
    TimeoutmsComponent,
    ScheduleComponent,
    EndpointComponent,
    TagsComponent,
    FaultTagsComponent,
    NotifierSelectorComponent,
    FaultNotifiersComponent,
    ActionsComponent,
    DbEndpointComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule
  ]
})
export class SharedModule { }
