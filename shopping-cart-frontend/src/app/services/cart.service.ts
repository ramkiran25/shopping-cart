import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface CartItem {
  productName: string;
  quantity: number;
  price: number;
  totalPrice: number;
}

export interface CartState {
  subtotal: number;
  tax: number;
  total: number;
  items: CartItem[];
}

@Injectable({
  providedIn: "root",
})
export class CartService {
  private readonly http = inject(HttpClient);
  // Using relative path so requests flow through proxy.conf.json (avoids CORS/port issues)
  private readonly baseUrl = "/api/v1/cart";

  // Reactive State Signals consumed by CartComponent template
  readonly cartState = signal<CartState | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly activeCartId = signal<string>("cart-1");

  /**
   * Fetches the cart state and updates cartState signal.
   */
  fetchCart(cartId: string = this.activeCartId()): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.getCartState(cartId).subscribe({
      next: (state) => {
        this.cartState.set(state);
        this.isLoading.set(false);
      },
      error: (err) => {
        const message =
          err.error?.message || err.message || "Failed to fetch cart state.";
        this.errorMessage.set(message);
        this.cartState.set(null);
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Direct RxJS methods for programmatic invocation
   */
  getCartState(cartId: string): Observable<CartState> {
    return this.http.get<CartState>(`${this.baseUrl}/${cartId}`);
  }

  addProduct(
    cartId: string,
    productName: string,
    quantity: number,
  ): Observable<string> {
    return this.http.post(`${this.baseUrl}/${cartId}/items`, null, {
      params: { productName, quantity },
      responseType: "text",
    });
  }

  clearCart(cartId: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${cartId}`, {
      responseType: "text",
    });
  }
}
