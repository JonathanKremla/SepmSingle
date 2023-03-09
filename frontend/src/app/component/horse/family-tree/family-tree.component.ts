import {Component, OnInit} from '@angular/core';
import {HorseService} from '../../../service/horse.service';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {HorseFamilyTree} from '../../../dto/horse';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-family-tree',
  templateUrl: './family-tree.component.html',
  styleUrls: ['./family-tree.component.scss']
})
export class FamilyTreeComponent implements OnInit {

  generations = Number(this.route.snapshot.queryParamMap.get('generations'));
  initialized = false;
  horseTree: HorseFamilyTree = {
    name: '',
    dateOfBirth: undefined
  };

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService
  ) {
  }

  ngOnInit(): void {
    const queryParams: Params = {generations: this.generations.toString()};
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams,
        replaceUrl: true
      });
    if (this.generations >= 0) {
      this.loadTree();
    }
  }

  loadTreeAfterDeletion(event: any): void{
    this.loadTree();
  }

  loadTree() {
    const queryParams: Params = {generations: this.generations.toString()};
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams,
        replaceUrl: true
      });
    this.initialized = true;
    this.service.getFamilyTree(Number(this.route.snapshot.paramMap.get('id')), this.generations).subscribe({
      next: data => {
        this.horseTree = data;
      },
      error: error => {
        this.notification.error(error.error.message);
        console.log(error);
        this.router.navigate(['/horses']);
      }
    });
  }
}
