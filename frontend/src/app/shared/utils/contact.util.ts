export function phoneDigits(phone?: string): string {
  return (phone || '').replace(/\D/g, '');
}
