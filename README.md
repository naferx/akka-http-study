# akka-http-study


curl -i -X POST -H "Content-type:application/json" http://localhost:8082/productojson -d '{"id": 123, "nombre": "hey" }'

curl -i -X POST -H "Content-type:application/octet-stream" --data-binary "@producto.txt" http://localhost:8082/productoproto

curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Host: localhost" -H "Origin: http://www.websocket.org" -H "Sec-WebSocket-Version: 13" -H 'Sec-WebSocket-Key: +onQ3ZxjWlkNa0na6ydhNg==' http://localhost:8082/greeter