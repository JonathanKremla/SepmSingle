import {Component, OnInit} from '@angular/core';
import {Owner} from '../../../dto/owner';
import {NgForm, NgModel} from '@angular/forms';
import {OwnerService} from '../../../service/owner.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent implements OnInit {

  owner: Owner = {
    firstName: '',
    lastName: '',
    email: undefined
  };

  constructor(
    private service: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService
  ) {
  }

  ngOnInit(): void {
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      if (this.owner.email === '') {
        delete this.owner.email;
      }
      this.service.create(this.owner).subscribe({
        next: data => {
          this.notification.success(`Owner ${this.owner.firstName} created`);
          this.router.navigate(['/owners']);
        },
        error: error => {
          for (const err of error.error.errors){
            this.notification.error(err);
          }
          console.error('Error creating owner');
          this.notification.error('Error(s) creating owner: ');
        }
      });
    }
  }
}
