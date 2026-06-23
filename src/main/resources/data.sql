INSERT IGNORE INTO cupon (id_cupon, codigo, tipo_descuento, valor_descuento, monto_minimo, fecha_inicio, fecha_vencimiento, limite_uso, usos_actuales, estado) VALUES
(1001, 'BIENVENIDA10', 'PORCENTAJE', 10.00, 10000.00, '2026-06-01', '2026-12-31', 500, 12, 'ACTIVO'),
(1002, 'ENVIOGRATIS', 'MONTO', 3990.00, 25000.00, '2026-06-01', '2026-09-30', 300, 25, 'ACTIVO'),
(1003, 'VIP5000', 'MONTO', 5000.00, 40000.00, '2026-06-01', '2026-08-31', 150, 7, 'ACTIVO');

INSERT IGNORE INTO carrito_compra (id_carrito, id_cliente, fecha_creacion, estado, subtotal) VALUES
(1001, 2001, '2026-06-20 09:15:00', 'ABIERTO', 74980.00),
(1002, 2002, '2026-06-21 13:40:00', 'CONFIRMADO', 42990.00),
(1003, 2003, '2026-06-22 10:10:00', 'ABIERTO', 29990.00);

INSERT IGNORE INTO detalle_carrito (id_detalle_carrito, id_carrito, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1001, 1001, 1001, 1, 34990.00, 34990.00),
(1002, 1001, 1002, 1, 39990.00, 39990.00),
(1003, 1002, 1003, 1, 42990.00, 42990.00),
(1004, 1003, 1004, 1, 29990.00, 29990.00);

INSERT IGNORE INTO pedido (id_pedido, id_cliente, id_sucursal, id_direccion, id_pago, fecha_pedido, subtotal, descuento, total, estado) VALUES
(1001, 2001, 3001, 4001, 5001, '2026-06-20 10:30:00', 74980.00, 7498.00, 67482.00, 'CONFIRMADO'),
(1002, 2002, 3002, 4002, 5002, '2026-06-21 15:05:00', 42990.00, 3990.00, 39000.00, 'PAGADO'),
(1003, 2003, 3001, 4003, NULL, '2026-06-22 11:20:00', 29990.00, 0.00, 29990.00, 'CREADO');

INSERT IGNORE INTO detalle_pedido (id_detalle_pedido, id_pedido, id_producto, cantidad, precio_unitario, descuento, subtotal) VALUES
(1001, 1001, 1001, 1, 34990.00, 3499.00, 31491.00),
(1002, 1001, 1002, 1, 39990.00, 3999.00, 35991.00),
(1003, 1002, 1003, 1, 42990.00, 3990.00, 39000.00),
(1004, 1003, 1004, 1, 29990.00, 0.00, 29990.00);

INSERT IGNORE INTO historial_estado_pedido (id_historial, id_pedido, estado_anterior, estado_nuevo, fecha_cambio, observacion) VALUES
(1001, 1001, 'CREADO', 'CONFIRMADO', '2026-06-20 10:35:00', 'Cliente confirma compra'),
(1002, 1002, 'CREADO', 'CONFIRMADO', '2026-06-21 15:10:00', 'Pedido confirmado'),
(1003, 1002, 'CONFIRMADO', 'PAGADO', '2026-06-21 15:20:00', 'Pago registrado'),
(1004, 1003, NULL, 'CREADO', '2026-06-22 11:20:00', 'Pedido creado desde carrito');

INSERT IGNORE INTO uso_cupon (id_uso_cupon, id_pedido, id_cupon, fecha_uso, descuento_aplicado, estado) VALUES
(1001, 1001, 1001, '2026-06-20 10:30:00', 7498.00, 'APLICADO'),
(1002, 1002, 1002, '2026-06-21 15:05:00', 3990.00, 'APLICADO');
