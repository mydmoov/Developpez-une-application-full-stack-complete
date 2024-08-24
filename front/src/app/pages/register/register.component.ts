import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SessionService } from 'src/app/services/session.service';
import { AuthService} from "../../features/auth/services/auth.service";
import {RegisterRequest} from "../../features/auth/interfaces/registerRequest.interface";
import {AuthValid} from "../../features/auth/interfaces/authValid.interface";

import { User } from 'src/app/interfaces/user.interface';
import {PasswordValidator} from "../../validators/password.validator";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  public hide = true;
  public onError = false;

  public form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    name: ['', [Validators.required, Validators.min(3)]],
    password: ['', [Validators.required, PasswordValidator.strongPassword()]]
  });

  constructor(private authService: AuthService,
              private fb: FormBuilder,
              private router: Router,
              private sessionService: SessionService) { }


  public register(): void {
    const registerRequest = this.form.value as RegisterRequest;
    this.authService.register(registerRequest).subscribe(
      (response: AuthValid) => {
        // Redirigez vers la page de connexion après l'inscription réussie
        this.router.navigate(['/login']);
      },
      error => this.onError = true
    );
  }
}
