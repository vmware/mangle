import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../../app-routing.module';
import { EndpointComponent } from './endpoint.component';
import { MachineComponent } from './machine/machine.component';
import { KubernetesComponent } from './kubernetes/kubernetes.component';
import { DockerComponent } from './docker/docker.component';
import { VcenterComponent } from './vcenter/vcenter.component';
import { EndpointCredentialsComponent } from './credentials/endpoint-credentials.component';

@NgModule({
  declarations: [EndpointComponent, MachineComponent, KubernetesComponent, DockerComponent, VcenterComponent, EndpointCredentialsComponent],
  imports: [
	  BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
	  AppRoutingModule,
	  CommonModule
  ]
})
export class EndpointModule { }
