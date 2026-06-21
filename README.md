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
- `DELETE /api/pedidos/{id}`
