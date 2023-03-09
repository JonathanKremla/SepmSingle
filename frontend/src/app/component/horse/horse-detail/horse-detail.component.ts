import {Component, OnInit} from '@angular/core';
import {Horse} from '../../../dto/horse';
import {Sex} from '../../../dto/sex';
import {HorseService} from '../../../service/horse.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {ConfirmDeleteDialogComponent} from '../../confirm-delete-dialog/confirm-delete-dialog.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss']
})
export class HorseDetailComponent implements OnInit {

  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date('invalid'),
    sex: Sex.female,
    mother: undefined,
    father: undefined
  };

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService,
    private modelService: NgbModal
  ) {
  }

  ngOnInit(): void {
    this.getHorse();
    //is needed so newly fetched horse is displayed after navigating from detail view to other detail view directly
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

  getHorse() {
    this.service.getById(Number(this.route.snapshot.paramMap.get('id'))).subscribe({
      next: data => {
        console.log('received horse', data);
        this.horse = data;
      },
      error: error => {
        this.notification.error('Error fetching horse', error.message.message);
        console.error('Error fetching horse', error.message.message);
        this.router.navigate(['/horses']);
      }
    });
  }

  getFatherName() {
    if (this.horse.father != null) {
      return this.horse.father.name;
    }
  }

  getMotherName() {
    if (this.horse.mother != null) {
      return this.horse.mother.name;
    }
  }

  getOwnerName() {
    if (this.horse.owner != null) {
      return this.horse.owner.firstName + ' ' + this.horse.owner.lastName;
    }
  }

  getMotherId() {
    if (this.horse.mother !== null && this.horse.mother !== undefined) {
      return this.horse.mother.id;
    }
  }

  getFatherId() {
    if (this.horse.father !== null && this.horse.father !== undefined) {
      return this.horse.father.id;
    }

  }

  public deleteHorse() {
    const modalRef = this.modelService.open(ConfirmDeleteDialogComponent);
    modalRef.componentInstance.horse = this.horse;

    modalRef.result.then((deleted: boolean) => {
      if (deleted) {
        this.router.navigate(['/horses']);
      }
    });
  }
}
