import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CoreComponent } from './core.component';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../app-routing.module';
import { EndpointModule } from './endpoint/endpoint.module';
import { FaultModule } from './fault/fault.module';
import { RequestsModule } from './requests/requests.module';

@NgModule({
  declarations: [CoreComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    AppRoutingModule,
    CommonModule,
    EndpointModule,
    FaultModule,
    RequestsModule
  ]
})
export class CoreModule { }
