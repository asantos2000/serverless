# NGINX Cache
**source**: <https://github.com/asantos2000/serverless/tree/master/docs/nginx>

## Arquivo de configuração do kubernetes

nginx.yaml

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
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: nginx-config
data:
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
kubectl create -f -nginx.yaml
```

## Find which port ngix is running
```bash
kubectl get services --namespace default
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                         AGE
kubernetes         ClusterIP   10.96.0.1        <none>        443/TCP                         31d
nginxsvc           NodePort    10.99.157.231    <none>        80:30557/TCP                    48s <---------
vivo-poc-fn-api    NodePort    10.109.173.143   <none>        80:30021/TCP                    29d
vivo-poc-fn-flow   NodePort    10.100.170.207   <none>        81:31177/TCP                    29d
vivo-poc-fn-ui     NodePort    10.111.125.248   <none>        3000:31327/TCP,4000:30246/TCP   29d
vivo-poc-mysql     ClusterIP   10.111.78.103    <none>        3306/TCP                        29d
vivo-poc-redis     ClusterIP   10.107.207.124   <none>        6379/TCP                        29d

```

## Testing locally
```bash
docker run --name nginx-cache -p 80:80 -p 443:443 -v $PWD/nginx.conf:/etc/nginx/nginx.conf:ro nginx
```
