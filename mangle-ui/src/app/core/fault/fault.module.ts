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
import { AwsEC2StateComponent } from './infra/aws/ec2/state/aws-ec2-state.component';
import { AwsEC2NetworkComponent } from './infra/aws/ec2/network/aws-ec2-network.component';
import { FilehandlerComponent } from './application/filehandler/filehandler.component';
import { ThreadLeakComponent } from './application/threadleak/threadleak.component';
import { FilehandlerInfraComponent } from './infra/filehandler/filehandler.component';
import { JavaMethodLatencyComponent } from './application/javamethodlatency/javamethodlatency.component';
import { SpringServiceLatencyComponent } from './application/springservicelatency/springservicelatency.component';
import { SpringServiceExceptionComponent } from './application/springserviceexception/springserviceexception.component';
import { KillJVMComponent } from './application/killjvm/killjvm.component';
import { SimulateJavaExceptionComponent } from './application/simulatejavaexception/simulatejavaexception.component';

import { CustomFaultComponent } from './custom/custom-fault.component';

import { PacketDelayComponent } from './infra/network/packet-delay/packet-delay.component';
import { PacketDropComponent } from './infra/network/packet-drop/packet-drop.component';
import { PacketDuplicateComponent } from './infra/network/packet-duplicate/packet-duplicate.component';
import { PacketCorruptionComponent } from './infra/network/packet-corruption/packet-corruption.component';
import { DiskSpaceComponent } from './infra/diskspace/diskspace.component';
import { KernelPanicComponent } from './infra/kernelpanic/kernelpanic.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { K8SServiceUnavailableComponent } from './infra/k8s/service-unavailable/k8s-service-unavailable.component';

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
    K8SServiceUnavailableComponent,
    VcenterDiskComponent,
    VcenterNicComponent,
    VcenterStateComponent,
    AwsEC2StateComponent,
    AwsEC2NetworkComponent,
    FilehandlerComponent,
    ThreadLeakComponent,
    FilehandlerInfraComponent,
    JavaMethodLatencyComponent,
    SpringServiceLatencyComponent,
    SpringServiceExceptionComponent,
    KillJVMComponent,
    SimulateJavaExceptionComponent,
    CustomFaultComponent,
    PacketDelayComponent,
    PacketDropComponent,
    PacketDuplicateComponent,
    PacketCorruptionComponent,
    DiskSpaceComponent,
    KernelPanicComponent
  ],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    AppRoutingModule,
    CommonModule,
    SharedModule
  ]
})
export class FaultModule { }
