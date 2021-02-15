import { Component, EventEmitter, Input, Output } from '@angular/core';
@Component({
    selector: 'app-actions',
    templateUrl: './actions.component.html'
})
export class ActionsComponent {

    @Input() addDisabled: boolean;
    @Input() editDisabled: boolean;
    @Input() deleteDisabled: boolean;
    @Input() enableDisabled: boolean;
    @Input() disabledDisabled: boolean;

    @Output() addEmitter = new EventEmitter<boolean>();
    @Output() editEmitter = new EventEmitter<boolean>();
    @Output() deleteEmitter = new EventEmitter<boolean>();
    @Output() enableDisableEmitter = new EventEmitter<boolean>();

    public constructor() { }

    public callAdd() {
        this.addEmitter.emit(true);
    }

    public callEdit() {
        this.editEmitter.emit(true);
    }

    public callDelete() {
        this.deleteEmitter.emit(true);
    }

    public callEnable() {
        this.enableDisableEmitter.emit(true);
    }

    public callDisable() {
        this.enableDisableEmitter.emit(false);
    }

}

