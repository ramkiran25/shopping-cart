import { Component, signal } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { CartComponent } from "./components/cart/cart";

@Component({
  selector: "app-root",
  imports: [RouterOutlet, CartComponent],
  templateUrl: "./app.html",
  styleUrl: "./app.css",
})
export class App {
  protected readonly title = signal("shopping-cart-frontend");
}
