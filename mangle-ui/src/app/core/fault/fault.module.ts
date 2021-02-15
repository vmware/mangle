import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { HttpClientModule } from "@angular/common/http";
import { ClarityModule } from "@clr/angular";
import { FaultComponent } from "./fault.component";
import { CpuComponent } from "./application/cpu/cpu.component";
import { CpuInfraComponent } from "./infra/cpu/cpu.component";
import { MemoryComponent } from "./application/memory/memory.component";
import { MemoryInfraComponent } from "./infra/memory/memory.component";
import { DiskioInfraComponent } from "./infra/diskio/diskio.component";
import { KillprocessComponent } from "./infra/killprocess/killprocess.component";
import { StopServiceComponent } from "./infra/stopservice/stopservice.component";
import { DockerStateChangeComponent } from "./infra/docker/state/docker-state-change.component";
import { DeleteK8SResourceComponent } from "./infra/k8s/delete/delete-k8s-resource.component";
import { K8SResourceNotReadyComponent } from "./infra/k8s/ready/k8s-resource-not-ready.component";
import { FilehandlerComponent } from "./application/filehandler/filehandler.component";
import { ThreadLeakComponent } from "./application/threadleak/threadleak.component";
import { FilehandlerInfraComponent } from "./infra/filehandler/filehandler.component";
import { JavaMethodLatencyComponent } from "./application/javamethodlatency/javamethodlatency.component";
import { SpringServiceLatencyComponent } from "./application/springservicelatency/springservicelatency.component";
import { SpringServiceExceptionComponent } from "./application/springserviceexception/springserviceexception.component";
import { KillJVMComponent } from "./application/killjvm/killjvm.component";
import { SimulateJavaExceptionComponent } from "./application/simulatejavaexception/simulatejavaexception.component";

import { CustomFaultComponent } from "./custom/custom-fault.component";

import { PacketDelayComponent } from "./infra/network/packet-delay/packet-delay.component";
import { PacketDropComponent } from "./infra/network/packet-drop/packet-drop.component";
import { PacketDuplicateComponent } from "./infra/network/packet-duplicate/packet-duplicate.component";
import { PacketCorruptionComponent } from "./infra/network/packet-corruption/packet-corruption.component";
import { DiskSpaceComponent } from "./infra/diskspace/diskspace.component";
import { KernelPanicComponent } from "./infra/kernelpanic/kernelpanic.component";
import { NetworkPartitionComponent } from "./infra/networkpartition/networkpartition.component";
import { SharedModule } from "src/app/shared/shared.module";
import { K8SServiceUnavailableComponent } from "./infra/k8s/service-unavailable/k8s-service-unavailable.component";
import { DbConnectionLeakComponent } from "./db/connectionleak/connectionleak.component";
import { DbTransactionErrorComponent } from "./db/transactionerror/transactionerror.component";
import { RouterModule } from "@angular/router";
import { FaultRoutingModule } from "./fault-routing.module";
import { ClockSkewComponent } from "./infra/clockskew/clockskew.component";
import { DbTransactionLatencyComponent } from "./db/transactionlatency/transactionlatency.component";
import { RedisDbDelayComponent } from "./db/redis/delay/redis-delay.component";
import { RedisDbReturnErrorComponent } from "./db/redis/returnerror/redis-return-error.component";
import { RedisDbReturnEmptyComponent } from "./db/redis/returnempty/redis-return-empty.component";
import { RedisDbDropConnectionComponent } from "./db/redis/dropconnection/redis-drop-connection.component";

@NgModule({
  declarations: [
    FaultComponent,
    CpuComponent,
    CpuInfraComponent,
    MemoryComponent,
    MemoryInfraComponent,
    DiskioInfraComponent,
    KillprocessComponent,
    StopServiceComponent,
    DockerStateChangeComponent,
    DeleteK8SResourceComponent,
    K8SResourceNotReadyComponent,
    K8SServiceUnavailableComponent,
    DbConnectionLeakComponent,
    DbTransactionErrorComponent,
    DbTransactionLatencyComponent,
    RedisDbDelayComponent,
    RedisDbReturnErrorComponent,
    RedisDbReturnEmptyComponent,
    RedisDbDropConnectionComponent,
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
    KernelPanicComponent,
    NetworkPartitionComponent,
    ClockSkewComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    FaultRoutingModule,
    CommonModule,
    SharedModule,
    RouterModule
  ],
  exports: [
    FaultComponent
  ]
})
export class FaultModule {
}
