# akka-http-study


curl -i -X POST -H "Content-type:application/json" http://localhost:8082/productojson -d '{"id": 123, "nombre": "hey" }'

curl -i -X POST -H "Content-type:application/octet-stream" --data-binary "@producto.txt" http://localhost:8082/productoproto