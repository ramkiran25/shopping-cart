import { Routes } from "@angular/router";
import { CartComponent } from "./components/cart/cart";

export const routes: Routes = [
  { path: "", redirectTo: "cart", pathMatch: "full" },
  { path: "cart", component: CartComponent },
];
