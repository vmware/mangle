import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../../app-routing.module';
import { QueryComponent } from './query/query.component';
import { ResiliencyscoreComponent } from './resiliencyscore.component';
import { ResiliencyscoreRoutingModule } from './resiliencyscore-routing-module';
import { SharedModule } from 'src/app/shared/shared.module';
import { RouterModule } from '@angular/router';
import { CalculateResiliencyScoreComponent } from './calculate-rscore/calculate-rscore.component';
import { ServiceComponent } from './service/service.component'

@NgModule({
  declarations: [ResiliencyscoreComponent, QueryComponent, CalculateResiliencyScoreComponent, ServiceComponent],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    ResiliencyscoreRoutingModule,
    SharedModule,
    RouterModule
  ],
  exports: [
    ResiliencyscoreComponent
  ]
})
export class ResiliencyscoreModule {
  
}
