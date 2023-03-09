import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {ConfirmDeleteDialogComponent} from '../../confirm-delete-dialog/confirm-delete-dialog.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';


export enum HorseCreateEditMode {
  create,
  edit,
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: undefined,
    sex: undefined,
    owner: undefined,
    mother: undefined,
    father: undefined
  };


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private modal: NgbModal
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      default:
        return '?';
    }

  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Edit';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }

  get modeIsEdit(): boolean {
    return !this.modeIsCreate;
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'edited';
      default:
        return '?';
    }
  }

  private get modeActionProcess(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'creating';
      case HorseCreateEditMode.edit:
        return 'editing';
      default:
        return '?';
    }
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if (this.modeIsEdit) {
      this.getHorse();
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  motherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByNameMother(this.horse.dateOfBirth, input, 5);

  fatherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByNameFather(this.horse.dateOfBirth, input, 5);

  getHorse() {
    this.service.getById(Number(this.route.snapshot.paramMap.get('id'))).subscribe({
      next: data => {
        console.log('received horse', data);
        this.horse = data;
      },
      error: error => {
        this.notification.error('Error fetching horse', error.message.message);
        console.error(error.message.message);
        this.router.navigate(['/horses']);
      }
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatMotherName(mother: Horse | null | undefined): string {
    return (mother == null)
      ? ''
      : `${mother.name}`;
  }

  public formatFatherName(father: Horse | null | undefined): string {
    return (father == null)
      ? ''
      : `${father.name}`;
  }

  public deleteHorse() {
    const modalRef = this.modal.open(ConfirmDeleteDialogComponent);
    modalRef.componentInstance.horse = this.horse;

    modalRef.result.then((deleted: boolean) => {
      if (deleted) {
        this.router.navigate(['/horses']);
      }
    });
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(this.horse);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.edit(this.horse);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          if(error.error.status !== 400) {
            for (const err of error.error.errors){
              this.notification.error(err);
            }
          }
          else{
            this.notification.error('Invalid Date of birth');
          }
          console.error('Error ' + this.modeActionProcess + ' horse');
          this.notification.error('Error(s) ' + this.modeActionProcess + ' horse: ');
        }

      });
    }
  }

}
