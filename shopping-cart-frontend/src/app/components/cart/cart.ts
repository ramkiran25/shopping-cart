import { Component, OnInit, inject, signal } from "@angular/core";
import { CommonModule, CurrencyPipe } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { HttpErrorResponse } from "@angular/common/http";

// Adjust this import path if CartService is in src/app/services/cart.service.ts
import { CartService } from "../../services/cart.service";

@Component({
  selector: "app-cart",
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule],
  templateUrl: "./cart.html", // Change to ./cart.component.html if that is your exact filename
  styleUrls: ["./cart.css"], // Change to ./cart.component.css if that is your exact filename
})
export class CartComponent implements OnInit {
  protected readonly cartService = inject(CartService);

  productName = signal<string>("");
  quantity = signal<number>(1);

  ngOnInit(): void {
    this.cartService.fetchCart();
  }

  onAddItem(): void {
    const name = this.productName().trim();
    const qty = this.quantity();

    if (!name || qty <= 0) return;

    this.cartService
      .addProduct(this.cartService.activeCartId(), name, qty)
      .subscribe({
        next: () => {
          this.productName.set("");
          this.quantity.set(1);
          this.cartService.fetchCart();
        },
        error: (err: HttpErrorResponse) => {
          const msg =
            err.error?.message || err.message || "Failed to add item.";
          this.cartService.errorMessage.set(msg);
        },
      });
  }

  onClearCart(): void {
    this.cartService.clearCart(this.cartService.activeCartId()).subscribe({
      next: () => this.cartService.fetchCart(),
      error: (err: HttpErrorResponse) => {
        const msg =
          err.error?.message || err.message || "Failed to clear cart.";
        this.cartService.errorMessage.set(msg);
      },
    });
  }
}
