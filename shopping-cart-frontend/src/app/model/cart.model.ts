export interface CartItem {
  productName: string;
  quantity: number;
  pricePerUnit: number;
  totalPrice: number;
}

export interface CartState {
  items: CartItem[];
  subtotal: number;
  tax: number;
  total: number;
}

export interface ApiErrorResponse {
  message: string;
  details?: string;
}
