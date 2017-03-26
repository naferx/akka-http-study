# akka-http-study


curl -i -X POST -H "Content-type:application/json" http://localhost:8082/productojson -d '{"id": 123, "nombre": "hey" }'

curl -i -X POST -H "Content-type:application/octet-stream" --data-binary "@producto.txt" http://localhost:8082/productoproto

curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Host: localhost" -H "Origin: http://www.websocket.org" -H "Sec-WebSocket-Version: 13" -H 'Sec-WebSocket-Key: +onQ3ZxjWlkNa0na6ydhNg==' http://localhost:8082/greeter


Links
https://github.com/DanielaSfregola/akka-tutorials
https://sachabarbs.wordpress.com/2016/11/16/akka-http/
http://alexkuang.com/blog/2016/04/26/writing-an-api-client-with-akka-http/

https://gist.github.com/dcaoyuan/f2f70bed35c647a4d9a43dfdbcb6dbb8
https://gist.github.com/adamw/bd4f5aafa9d01abbc883