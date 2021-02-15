import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {VcenterStateComponent} from "./VM/state/vcenter-state.component";
import {VcenterNicComponent} from "./VM/nic/vcenter-nic.component";
import {VcenterDiskComponent} from "./VM/disk/vcenter-disk.component";
import {VmComponent} from "./VM/vm.component";
import {VcenterHostComponent} from "./Host/state/vcenter-host.component";


const routes: Routes = [
    {
      path: "", component: VmComponent, children: [
        {path: "", redirectTo: "vm-state", pathMatch: "full"},
        {path: "vm-state", component: VcenterStateComponent},
        {path: "vm-network", component: VcenterNicComponent},
        {path: "vm-storage", component: VcenterDiskComponent},
      ],
    },
    {path: "vcenter-host", component: VcenterHostComponent}
  ]
;

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class VcenterRoutingModule {
}
