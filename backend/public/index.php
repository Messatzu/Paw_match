<?php

header('Content-Type: application/json; charset=utf-8');

$respuesta = [
    'aplicacion' => 'Paw Match',
    'estado' => 'ok'
];

echo json_encode($respuesta);

