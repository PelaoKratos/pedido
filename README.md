# Microservicio Pedido

Gestiona pedidos, sus productos asociados y el historial de cambios de estado.

## Endpoints principales

- `GET /api/pedidos`
- `GET /api/pedidos/{id}`
- `GET /api/pedidos/cliente/{idCliente}`
- `GET /api/pedidos/estado/{estado}`
- `POST /api/pedidos`
- `PUT /api/pedidos/{id}`
- `PATCH /api/pedidos/{id}/estado`
- `PATCH /api/pedidos/{id}/confirmacion`
- `PATCH /api/pedidos/{id}/cancelacion`
- `PATCH /api/pedidos/{id}/cupon/{idCupon}`
- `PATCH /api/pedidos/{id}/cupon/{idUsoCupon}/anulacion`
- `DELETE /api/pedidos/{id}`

## Cupones

- `GET /api/cupones`
- `GET /api/cupones/{id}`
- `GET /api/cupones/codigo/{codigo}`
- `GET /api/cupones/estado/{estado}`
- `POST /api/cupones`
- `PUT /api/cupones/{id}`
- `PATCH /api/cupones/{id}/estado`
