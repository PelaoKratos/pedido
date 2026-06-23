# Microservicio Pedido

Pedido concentra el flujo de compra del cliente: carrito, detalle del pedido, cupones y cambios de estado. Es uno de los servicios centrales porque conecta clientes, productos, sucursales y pagos.

## Que gestiona

- Pedidos y sus detalles.
- Carritos de compra.
- Productos agregados al carrito.
- Cupones y uso de cupones.
- Historial de estados del pedido.

## Configuracion local

```properties
spring.application.name=pedido
server.port=8086
spring.datasource.url=jdbc:mysql://localhost:3307/pedido_bd?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
```

Base de datos:

```sql
CREATE DATABASE pedido_bd;
```

## Microservicios que consulta

```properties
microservices.cliente.url=http://localhost:8082/api/v1/clientes
microservices.sucursal.url=http://localhost:8083/api/v1/sucursales
microservices.pago.url=http://localhost:8087/api/v1/pagos
microservices.producto.url=http://localhost:8084/api/v1/productos
```

Estas validaciones ayudan a que el pedido no se cree con referencias inexistentes.

## Endpoints principales

Pedidos:

- `GET /api/pedidos`
- `GET /api/pedidos/{id}`
- `GET /api/pedidos/cliente/{idCliente}`
- `GET /api/pedidos/estado/{estado}`
- `GET /api/pedidos/{id}/historial`
- `POST /api/pedidos`
- `PUT /api/pedidos/{id}`
- `DELETE /api/pedidos/{id}`

Carritos:

- `GET /api/carritos`
- `GET /api/carritos/{id}`
- `GET /api/carritos/cliente/{idCliente}`
- `POST /api/carritos`
- `POST /api/carritos/{id}/productos`
- `DELETE /api/carritos/{id}/productos/{idProducto}`
- `DELETE /api/carritos/{id}`

Cupones:

- `GET /api/cupones`
- `GET /api/cupones/{id}`
- `GET /api/cupones/codigo/{codigo}`
- `GET /api/cupones/estado/{estado}`
- `POST /api/cupones`
- `PUT /api/cupones/{id}`

## Ejecutar

```powershell
mvn spring-boot:run
```

## Probar

```powershell
mvn test
```
