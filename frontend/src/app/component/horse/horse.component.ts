import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse, HorseSearch} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {OwnerService} from '../../service/owner.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmDeleteDialogComponent} from '../confirm-delete-dialog/confirm-delete-dialog.component';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  search = false;
  horses: Horse[] = [];
  debounceSeconds = 500;
  inputChanged: Subject<any> = new Subject<any>();
  horseSearchParams: HorseSearch = {
    name: undefined,
    description: undefined,
    bornBefore: undefined,
    owner: undefined,
    sex: undefined
  };
  bannerError: string | null = null;

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private notification: ToastrService,
    private modal: NgbModal
  ) {
    this.inputChanged.pipe(debounceTime(this.debounceSeconds), distinctUntilChanged()).subscribe(
      model => {
        this.reloadHorses();
      }
    );
  }

  ngOnInit(): void {
    this.reloadHorses();
  }

  fillParams() {
    if (this.horseSearchParams.name === '') {
      this.horseSearchParams.name = undefined;
    }
    if (this.horseSearchParams.description === '') {
      this.horseSearchParams.description = undefined;
    }
  }


  reloadHorses() {
    this.fillParams();
    this.service.getHorsesMatchingParams(this.horseSearchParams)
      .subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
  }

  public deleteHorse(horse: Horse) {
    const modalRef = this.modal.open(ConfirmDeleteDialogComponent);
    modalRef.componentInstance.horse = horse;

    modalRef.result.then((deleted: boolean) => {
      if (deleted) {
        this.reloadHorses();
      }
    });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? owner.firstName.concat(' ', owner.lastName)
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth
      ? new Date(horse.dateOfBirth).toLocaleDateString()
      : '';
  }

  displayableDescription(horse: Horse): string | undefined {
    return (horse.description && horse.description.length > 39)
      ? horse.description.substring(0, 36).padEnd(39, '...')
      : horse.description;
  }

}
