// Máscaras de CPF e CNPJ. O backend aceita com ou sem máscara e guarda só os caracteres.

export function maskCpf(value: string) {
  const d = value.replace(/\D/g, '').slice(0, 11)
  return d
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2')
}

// O CNPJ alfanumérico (2026) aceita letras nas 12 primeiras posições.
export function maskCnpj(value: string) {
  const c = value.replace(/[^0-9a-z]/gi, '').toUpperCase().slice(0, 14)
  const parts = [c.slice(0, 2), c.slice(2, 5), c.slice(5, 8), c.slice(8, 12), c.slice(12, 14)]
  let out = parts[0]
  if (parts[1]) out += '.' + parts[1]
  if (parts[2]) out += '.' + parts[2]
  if (parts[3]) out += '/' + parts[3]
  if (parts[4]) out += '-' + parts[4]
  return out
}
