import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HorseFamilyTree} from '../../../../dto/horse';
import {ConfirmDeleteDialogComponent} from '../../../confirm-delete-dialog/confirm-delete-dialog.component';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-family-tree-node',
  templateUrl: './family-tree-node.component.html',
  styleUrls: ['./family-tree-node.component.scss']
})
export class FamilyTreeNodeComponent implements OnInit {

  @Output() loadTreeAfterDeletion: EventEmitter<any> = new EventEmitter<any>();
  @Input() isRoot = false;

  collapsed = false;
  horseTree: HorseFamilyTree = {
    name: '',
    dateOfBirth: undefined,
  };

  constructor(
    private router: Router,
    private modal: NgbModal,
  ) {
  }


  @Input()
  set horse(value: HorseFamilyTree) {
    this.horseTree = value;
  }


  ngOnInit(): void {
  }

  public deleteHorse() {
    const modalRef = this.modal.open(ConfirmDeleteDialogComponent);
    modalRef.componentInstance.horse = this.horseTree;

    modalRef.result.then((deleted: boolean) => {
      if (deleted && !this.isRoot) {
        this.loadTreeAfterDeletion.emit();
      }
      else if(deleted && this.isRoot){
        this.router.navigate(['/horses']);
      }
    });
  }

  //used to pass deletion event from deeper nodes to parent to reload tree
  public passDeletion(event: any){
    this.loadTreeAfterDeletion.emit();
  }

  collapse() {
    this.collapsed = !this.collapsed;
  }

}
