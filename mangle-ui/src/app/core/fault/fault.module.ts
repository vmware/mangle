import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../../app-routing.module';
import { FaultComponent } from './fault.component';
import { CpuComponent } from './application/cpu/cpu.component';
import { CpuInfraComponent } from './infra/cpu/cpu.component';
import { MemoryComponent } from './application/memory/memory.component';
import { MemoryInfraComponent } from './infra/memory/memory.component';
import { DiskioInfraComponent } from './infra/diskio/diskio.component';
import { KillprocessComponent } from './infra/killprocess/killprocess.component';
import { DockerStateChangeComponent } from './infra/docker/state/docker-state-change.component';
import { DeleteK8SResourceComponent } from './infra/k8s/delete/delete-k8s-resource.component';
import { K8SResourceNotReadyComponent } from './infra/k8s/ready/k8s-resource-not-ready.component';
import { VcenterDiskComponent } from './infra/vcenter/disk/vcenter-disk.component';
import { VcenterNicComponent } from './infra/vcenter/nic/vcenter-nic.component';
import { VcenterStateComponent } from './infra/vcenter/state/vcenter-state.component';

@NgModule({
  declarations: [
    FaultComponent,
    CpuComponent,
    CpuInfraComponent,
    MemoryComponent,
    MemoryInfraComponent,
    DiskioInfraComponent,
    KillprocessComponent,
    DockerStateChangeComponent,
    DeleteK8SResourceComponent,
    K8SResourceNotReadyComponent,
    VcenterDiskComponent,
    VcenterNicComponent,
    VcenterStateComponent
  ],
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
export class FaultModule { }
