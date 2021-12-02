import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FaultComponent } from './fault.component';
import { CpuComponent } from './application/cpu/cpu.component';
import { CpuInfraComponent } from './infra/cpu/cpu.component';
import { MemoryComponent } from './application/memory/memory.component';
import { MemoryInfraComponent } from './infra/memory/memory.component';
import { DiskioInfraComponent } from './infra/diskio/diskio.component';
import { KillprocessComponent } from './infra/killprocess/killprocess.component';
import { StopServiceComponent } from './infra/stopservice/stopservice.component';
import { DockerStateChangeComponent } from './infra/docker/state/docker-state-change.component';
import { DeleteK8SResourceComponent } from './infra/k8s/delete/delete-k8s-resource.component';
import { K8SResourceNotReadyComponent } from './infra/k8s/ready/k8s-resource-not-ready.component';
import { K8SServiceUnavailableComponent } from './infra/k8s/service-unavailable/k8s-service-unavailable.component';
import { CustomFaultComponent } from './custom/custom-fault.component';
import { PacketDelayComponent } from './infra/network/packet-delay/packet-delay.component';
import { PacketDropComponent } from './infra/network/packet-drop/packet-drop.component';
import { PacketDuplicateComponent } from './infra/network/packet-duplicate/packet-duplicate.component';
import { PacketCorruptionComponent } from './infra/network/packet-corruption/packet-corruption.component';
import { FilehandlerComponent } from './application/filehandler/filehandler.component';
import { FilehandlerInfraComponent } from './infra/filehandler/filehandler.component';
import { ThreadLeakComponent } from './application/threadleak/threadleak.component';
import { JavaMethodLatencyComponent } from './application/javamethodlatency/javamethodlatency.component';
import { SpringServiceLatencyComponent } from './application/springservicelatency/springservicelatency.component';
import { SpringServiceExceptionComponent } from './application/springserviceexception/springserviceexception.component';
import { KillJVMComponent } from './application/killjvm/killjvm.component';
import { SimulateJavaExceptionComponent } from './application/simulatejavaexception/simulatejavaexception.component';
import { DiskSpaceComponent } from './infra/diskspace/diskspace.component';
import { KernelPanicComponent } from './infra/kernelpanic/kernelpanic.component';
import { NetworkPartitionComponent } from './infra/networkpartition/networkpartition.component';
import { DbConnectionLeakComponent } from './db/connectionleak/connectionleak.component';
import { DbTransactionErrorComponent } from './db/transactionerror/transactionerror.component';
import { EventsComponent } from '../../shared/events/events.component';
import { ClockSkewComponent } from './infra/clockskew/clockskew.component';
import { DbTransactionLatencyComponent } from './db/transactionlatency/transactionlatency.component';
import { RedisDbDelayComponent } from './db/redis/delay/redis-delay.component';
import { RedisDbReturnErrorComponent } from './db/redis/returnerror/redis-return-error.component';
import { RedisDbReturnEmptyComponent } from './db/redis/returnempty/redis-return-empty.component';
import { RedisDbDropConnectionComponent } from './db/redis/dropconnection/redis-drop-connection.component';
import { DrainK8SNodeComponent } from './infra/k8s/drain/drain-k8s-nodes.component';

const routes: Routes = [
  {
    path: '', component: FaultComponent, children: [
      { path: 'cpu', component: CpuComponent },
      { path: 'cpu-infra', component: CpuInfraComponent },
      { path: 'memory', component: MemoryComponent },
      { path: 'memory-infra', component: MemoryInfraComponent },
      { path: 'diskio-infra', component: DiskioInfraComponent },
      { path: 'killprocess', component: KillprocessComponent },
      { path: 'stopservice', component: StopServiceComponent },
      { path: 'docker-state-change', component: DockerStateChangeComponent },
      { path: 'delete-k8s-resource', component: DeleteK8SResourceComponent },
      { path: 'drain-k8s-node', component: DrainK8SNodeComponent },
      { path: 'k8s-resource-not-ready', component: K8SResourceNotReadyComponent },
      { path: 'k8s-service-unavailable', component: K8SServiceUnavailableComponent },
      { path: 'custom-fault', component: CustomFaultComponent },
      { path: 'packet-delay', component: PacketDelayComponent },
      { path: 'packet-drop', component: PacketDropComponent },
      { path: 'packet-duplicate', component: PacketDuplicateComponent },
      { path: 'packet-corruption', component: PacketCorruptionComponent },
      { path: 'filehandler-leak', component: FilehandlerComponent },
      { path: 'filehandler-leak-infra', component: FilehandlerInfraComponent },
      { path: 'thread-leak', component: ThreadLeakComponent },
      { path: 'java-method-latency', component: JavaMethodLatencyComponent },
      { path: 'spring-service-latency', component: SpringServiceLatencyComponent },
      { path: 'spring-service-exception', component: SpringServiceExceptionComponent },
      { path: 'kill-jvm', component: KillJVMComponent },
      { path: 'simulate-java-exception', component: SimulateJavaExceptionComponent },
      { path: 'disk-space', component: DiskSpaceComponent },
      { path: 'kernel-panic', component: KernelPanicComponent },
      { path: 'network-partition', component: NetworkPartitionComponent },
      { path: 'db-connection-leak', component: DbConnectionLeakComponent },
      { path: 'db-transaction-error', component: DbTransactionErrorComponent },
      { path: 'db-transaction-latency', component: DbTransactionLatencyComponent },
      { path: 'redis-db-delay', component: RedisDbDelayComponent },
      { path: 'redis-db-return-error', component: RedisDbReturnErrorComponent },
      { path: 'redis-db-return-empty', component: RedisDbReturnEmptyComponent },
      { path: 'redis-db-drop-connection', component: RedisDbDropConnectionComponent },
      { path: 'events', component: EventsComponent },
      { path: 'clock-skew', component: ClockSkewComponent },
      { path: "vcenter", loadChildren: "./infra/vcenter/vcenter.module#VcenterModule"},
      { path: 'aws', loadChildren: './infra/aws/aws.module#AwsModule' },
      { path: 'azure', loadChildren: './infra/azure/azure.module#AzureModule' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class FaultRoutingModule {
}
