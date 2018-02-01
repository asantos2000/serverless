# NGINX Cache
**source**: <https://github.com/asantos2000/serverless/tree/master/docs/nginx>

## Arquivo de configuração do kubernetes

nginx.conf

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginxsvc
  labels:
    app: nginx
spec:
  type: NodePort
  ports:
  - port: 80
    protocol: TCP
    name: http
  selector:
   app: nginx
---
apiVersion: v1
kind: ReplicationController
metadata:
  name: nginxrc
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx
    containers:
      - name: nginx
        image: nginx
        volumeMounts:
          - name: nginx-config
            mountPath: /etc/nginx/nginx.conf
            subPath: nginx.conf
    volumes:
      - name: nginx-config
        configMap:
          name: nginx-config        

```

Este arquivo configura o serviço no Kubernetes, define o número de réplicas e monta o arquivo nginx.conf a partir do config-map.

## Configuração do cache de conteúdo do nginx
**Refs**: 

* <https://www.nginx.com/resources/admin-guide/content-caching/>
* <https://serverfault.com/questions/583570/understanding-the-nginx-proxy-cache-path-directive>

nginx.conf

```
worker_processes  1;
events {
	worker_connections 1024;
}

http {
	proxy_cache_path  /var/cache/nginx/java-config levels=1:2 keys_zone=java-config:10m max_size=1g
                 inactive=310s use_temp_path=off;
	proxy_cache_path  /var/cache/nginx/oracle-query levels=1:2 keys_zone=oracle-query:10m max_size=1g
                 inactive=310s use_temp_path=off;
	
	server {

		listen 80;
		
		root /srv/http/java-config;
		index index.html index.htm;

		server_name localhost;

		location /java-config {
			add_header Cache-Control "no-cache, must-revalidate, max-age=0";
			add_header X-Proxy-Cache $upstream_cache_status;
			
			proxy_cache java-config;
			proxy_cache_use_stale error timeout invalid_header updating http_500 http_502 http_503 http_504 http_404;
			proxy_cache_lock on;
			proxy_cache_valid any 300s;
			proxy_ignore_headers X-Accel-Expires Expires Cache-Control;

			proxy_pass http://200.196.230.174:30088/r/java-hello/java-config;
			proxy_set_header Authorization "Basic VGhhbmsgeW91IGZvciByZWFkaW5nIGJuZWlqdC5ubAo=";
		}
		
		location /oracle-query {
			add_header Cache-Control "no-cache, must-revalidate, max-age=0";
			add_header X-Proxy-Cache $upstream_cache_status;
			
			proxy_cache oracle-query;
			proxy_cache_use_stale error timeout invalid_header updating http_500 http_502 http_503 http_504 http_404;
			proxy_cache_lock on;
			proxy_cache_valid any 300s;
			proxy_ignore_headers X-Accel-Expires Expires Cache-Control;

			proxy_pass http://200.196.230.174:30088/r/vivo-demo/vivo-ms-demo;
			proxy_set_header Authorization "Basic VGhhbmsgeW91IGZvciByZWFkaW5nIGJuZWlqdC5ubAo=";
		}
	}
}
```

O arquivo de configuração do nginx está configurado para dois serviços:

**Serviço 1**

* *URL no proxy*: **/java-config**
* *Backend*: <http://200.196.230.174:30088/r/java-hello/java-config>
* *Diretório do cache*: ``` /var/cache/nginx/java-config ```

**Serviço 2**

* *URL no proxy*: **/oracle-query**
* *Backend*: <http://200.196.230.174:30088/r/vivo-demo/vivo-ms-demo>
* *Diretório do cache*: ``` /var/cache/nginx/oracle-query ```

Customize este arquivo para os seus serviços.

Este arquivo será adicionado no kubernetes na seção config-map.

## Deploy to kubernetes
```bash
kubectl create -f -nginx.conf
```

## Find which port ngix is running
```bash
kubectl get services --namespace default
```

## Testing locally
```
docker run --name nginx-cache -p 80:80 -p 443:443 -v $PWD/nginx.conf:/etc/nginx/nginx.conf:ro nginx
```
```bash
