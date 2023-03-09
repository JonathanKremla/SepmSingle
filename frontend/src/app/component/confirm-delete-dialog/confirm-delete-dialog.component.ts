import {Component} from '@angular/core';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HorseService} from '../../service/horse.service';
import {Horse} from '../../dto/horse';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-confirm-content',
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrls: ['./confirm-delete-dialog.component.scss']
})
export class ConfirmDeleteDialogComponent {
  public horse!: Horse;

  constructor(
    public activeModal: NgbActiveModal,
    private service: HorseService,
    private notification: ToastrService,
    private modal: NgbModal
  ) {
  }

  public deleteHorse(): void {


    if (this.horse.id) {
      this.service.delete(this.horse.id).subscribe({
        next: value => {
          this.notification.success(`Horse ${this.horse.name} was deleted`);
          const horseDeleted = true;
          this.activeModal.close(horseDeleted);
        },
        error: error => {
          console.error('Error deleting Horse', error);
          this.notification.error(error, `Could not delete Horse ${this.horse.name}`);
        }
      });
    }
  }
}

