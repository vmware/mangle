import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {ClarityModule} from "@clr/angular";
import {VcenterRoutingModule} from "./vcenter-routing.module";
import {SharedModule} from "src/app/shared/shared.module";
import {VcenterStateComponent} from "./VM/state/vcenter-state.component";
import {VcenterDiskComponent} from "./VM/disk/vcenter-disk.component";
import {VcenterNicComponent} from "./VM/nic/vcenter-nic.component";
import {VmComponent} from "./VM/vm.component";
import {RouterModule} from "@angular/router";
import {VcenterHostComponent} from "./Host/state/vcenter-host.component";

@NgModule({
  declarations: [VcenterStateComponent, VcenterNicComponent, VcenterDiskComponent, VmComponent, VcenterHostComponent],
  imports: [
    FormsModule,
    RouterModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    VcenterRoutingModule,
    SharedModule
  ],
  providers: []
})

export class VcenterModule {
}
