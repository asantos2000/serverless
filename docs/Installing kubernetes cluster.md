# Installing kubernetes cluster
References: [How to quickly install Kubernetes on Ubuntu](https://www.techrepublic.com/article/how-to-quickly-install-kubernetes-on-ubuntu/)
What we're going to install below must be installed on all machines that will be joining the cluster. 

## Infrastructure

```
master <- node01
       <- node02
       <- node03
```

### Machines
Virtual machines at Vivo Cloud, with 2vCPU, 2GB of RAM memory, 40GB of SSD storage, one network interface, and full access of internet.

#### Master
```
Hostname: ariel
inet addr:10.100.18.10  Bcast:10.100.18.255  Mask:255.255.255.0
inet6 addr: fe80::f816:8eff:fe81:24c6/64 Scope:Link

$ ssh linux@10.100.18.10 -i id_rsa
```

#### Node01
```
Hostname: mimas
inet addr:10.100.18.11  Bcast:10.100.18.255  Mask:255.255.255.0
inet6 addr: fe80::f816:8eff:fe92:17f5/64 Scope:Link

$ ssh linux@10.100.18.11 -i id_rsa
```

#### Node02
```
Hostname: thor
inet addr:10.100.18.12  Bcast:10.100.18.255  Mask:255.255.255.0
inet6 addr: fe80::f816:8eff:fe5e:636b/64 Scope:Link

$ ssh linux@10.100.18.12 -i id_rsa
```

#### Node03
```
Hostname: lupus
inet addr:10.100.18.13  Bcast:10.100.18.255  Mask:255.255.255.0
inet6 addr: fe80::f816:8eff:fe54:7e9/64 Scope:Link

$ ssh linux@10.100.18.13 -i id_rsa
```

#### Internet addresses
```
200.196.230.174 -> ariel (master)
200.196.251.26  -> mimas (node01)
200.196.251.119 -> thor  (node02)
200.196.251.243 -> lupus (node03)
```
Ports opened on firewall

```
TCP 80
TCP 443
TCP 6443
TCP 1883
TCP 2376
TCP 2377
TCP 2379
TCP 2380
UDP 4789
TCP 5000
TCP 5672
TCP 7946
UDP 7946
UDP 8472
TCP 8883
TCP 9765
TCP 10250
TCP 10251
TCP 10252
TCP 10255
TCP 30080
TCP 30088
```

#### Testando a conectividade
Execute o script abaixo para cada máquina do cluster:

```bash
$ vi test-conn.sh

nc -z -v -w5 $1 80
nc -z -v -w5 $1 443
nc -z -v -w5 $1 6443
nc -z -v -w5 $1 1883
nc -z -v -w5 $1 2376
nc -z -v -w5 $1 2377
nc -z -v -w5 $1 2379
nc -z -v -w5 $1 2380
nc -z -v -w5 $1 4789
nc -z -v -w5 $1 5000
nc -z -v -w5 $1 5672
nc -z -v -w5 $1 7946
nc -z -v -w5 $1 7946
nc -z -v -w5 $1 8472
nc -z -v -w5 $1 8883
nc -z -v -w5 $1 9765
nc -z -v -w5 $1 10250
nc -z -v -w5 $1 10251
nc -z -v -w5 $1 10252
nc -z -v -w5 $1 10255
nc -z -v -w5 $1 30080
nc -z -v -w5 $1 30088
```

## Installing docker

### Installing dependencies

```bash
sudo apt-get update

sudo apt-get install -y apt-transport-https
```

### Next dependency is Docker

```bash
$ sudo apt install docker.io
```

### Start and enable the Docker service

```bash
$ sudo systemctl start docker

$ sudo systemctl enable docker
```

Test your docker installation

```bash
$ docker --version
Docker version 1.13.1, build 092cba3

$ docker run hello-world
Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
ca4f61b1923c: Pull complete
Digest: sha256:be0cd392e45be79ffeffa6b05338b98ebb16c87b255f48e297ec7f98e123905c
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://cloud.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
 
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS                      PORTS               NAMES
1e8f8ed30b97        hello-world         "/hello"            39 seconds ago      Exited (0) 38 seconds ago                       quizzical_raman

$ docker container prune # clean things up
WARNING! This will remove all stopped containers.
Are you sure you want to continue? [y/N] y
Deleted Containers:
1e8f8ed30b97900d8ee72a1c5842b54a614e560e8e69fe33f42b3b7c655a9630

$ docker image rm hello-world # clean things up
Untagged: hello-world:latest
Untagged: hello-world@sha256:be0cd392e45be79ffeffa6b05338b98ebb16c87b255f48e297ec7f98e123905c
Deleted: sha256:f2a91732366c0332ccd7afd2a5c4ff2b9af81f549370f7a19acd460f87686bc7
Deleted: sha256:f999ae22f308fea973e5a25b57699b5daf6b0f1150ac2a5c2ea9d7fecee50fdf
```

### Add user to the docker group

```bash
$ sudo gpasswd -a $USER docker
Adding user linux to group docker
```

> Exit and log again.

## Installing Kubernetes

### Download and add the key for the Kubernetes install.

```bash
sudo curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add
```

### Add a repository
```bash
$ sudo vi /etc/apt/sources.list.d/kubernetes.list
```

Enter the following content:

```
deb http://apt.kubernetes.io/ kubernetes-xenial main 
```

Save and close that file.

### Install Kubernetes

```bash
$ sudo apt-get update

$ sudo apt-get install -y kubelet kubeadm kubectl kubernetes-cni
```

### Disabling linux swap

```bash
$ sudo swapoff -a 
```

Comment swap line at /etc/fstab.

### Initialize the cluster

#### Init master
> **Attention**: Execute the following commands only in the Master machine.

```bash
$ kubeadm init

[kubeadm] WARNING: kubeadm is in beta, please do not use it for production clusters.
[init] Using Kubernetes version: v1.8.4
[init] Using Authorization modes: [Node RBAC]
[preflight] Running pre-flight checks
...
[addons] Applied essential addon: kube-dns
[addons] Applied essential addon: kube-proxy

Your Kubernetes master has initialized successfully!

To start using your cluster, you need to run (as a regular user):

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  http://kubernetes.io/docs/admin/addons/

You can now join any number of machines by running the following on each node
as root:

  kubeadm join --token b679e7.a576312edad23ac1 10.100.18.10:6443 --discovery-token-ca-cert-hash sha256:add33852c5583e87c9a4224b47d8e5f6e62fcaa65e7f5cd36d39dd7b8c28cfab
```

##### Before join nodes
> Run on Master

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

##### Deploying a pod network
> Run following commands only on the Master machine.

```bash
$ sudo kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
```

##### Testing
```bash
$ kubectl get pods --all-namespaces
NAMESPACE     NAME                            READY     STATUS    RESTARTS   AGE
kube-system   etcd-ariel                      1/1       Running   0          41s
kube-system   kube-apiserver-ariel            1/1       Running   0          29s
kube-system   kube-controller-manager-ariel   1/1       Running   0          40s
kube-system   kube-dns-545bc4bfd4-7kx2s       2/3       Running   0          1m
kube-system   kube-flannel-ds-rpdbd           1/1       Running   0          57s
kube-system   kube-proxy-4sw98                1/1       Running   0          1m
kube-system   kube-scheduler-ariel            1/1       Running   0          41s
```

#### Installing dashboard
References: [Dashboard](https://github.com/kubernetes/dashboard), [Access Control](https://github.com/kubernetes/dashboard/wiki/Access-control)

Starts running: 

```bash
$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/master/src/deploy/recommended/kubernetes-dashboard.yaml
```

Afert pod is running

```bash
kubectl get pods --all-namespaces
NAMESPACE     NAME                                   READY     STATUS    RESTARTS   AGE
kube-system   etcd-ariel                             1/1       Running   0          22h
kube-system   kube-apiserver-ariel                   1/1       Running   0          22h
kube-system   kube-controller-manager-ariel          1/1       Running   0          22h
kube-system   kube-dns-545bc4bfd4-7fm6s              3/3       Running   0          22h
kube-system   kube-flannel-ds-2mmqv                  1/1       Running   0          22h
kube-system   kube-flannel-ds-c7xsb                  1/1       Running   0          22h
kube-system   kube-flannel-ds-gwph9                  1/1       Running   0          22h
kube-system   kube-flannel-ds-j2q5s                  1/1       Running   0          22h
kube-system   kube-proxy-6mszq                       1/1       Running   0          22h
kube-system   kube-proxy-ndmdm                       1/1       Running   0          22h
kube-system   kube-proxy-t4q89                       1/1       Running   0          22h
kube-system   kube-proxy-w4hg6                       1/1       Running   0          22h
kube-system   kube-scheduler-ariel                   1/1       Running   0          22h
kube-system   kubernetes-dashboard-747c4f7cf-ftmpz   1/1       Running   0          1m

```

Configure access control using [admin privileges](https://github.com/kubernetes/dashboard/wiki/Access-control#admin-privileges)

Create file dashboard-admin.yaml

```yaml
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: kubernetes-dashboard
  labels:
    k8s-app: kubernetes-dashboard
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: kubernetes-dashboard
  namespace: kube-system
```

And run the command

```bash
$ kubectl create -f dashboard-admin.yaml
```

On your computer run

```bash
mac-as:~$ kubectl proxy
Starting to serve on 127.0.0.1:8001
```

Open on your browser

```bash
http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/
```

On Login View (Authentication method screen) choose SKIP.

#### Configuring your computer to admin the cluster through command line.

Copy the /etc/kubernetes/admin.conf from the master and save it in your computer as kubeconfig.yml

Open the file ~/.kube/config in your computer, if it not exists just create it with the content of kubeconfig.yaml.

If the file exists (i.e. you're accessing Google Containers) you'll need to merge it with the content of the session in kubeconfig.yaml.

After that you're able to execute the same commands are in the master.

```bash
mac-as:~$ kubectl get pods --all-namespaces
NAMESPACE     NAME                                   READY     STATUS    RESTARTS   AGE
kube-system   etcd-ariel                             1/1       Running   0          22h
kube-system   kube-apiserver-ariel                   1/1       Running   0          22h
kube-system   kube-controller-manager-ariel          1/1       Running   0          22h
kube-system   kube-dns-545bc4bfd4-7fm6s              3/3       Running   0          22h
kube-system   kube-flannel-ds-2mmqv                  1/1       Running   0          22h
kube-system   kube-flannel-ds-c7xsb                  1/1       Running   0          22h
kube-system   kube-flannel-ds-gwph9                  1/1       Running   0          22h
kube-system   kube-flannel-ds-j2q5s                  1/1       Running   0          22h
kube-system   kube-proxy-6mszq                       1/1       Running   0          22h
kube-system   kube-proxy-ndmdm                       1/1       Running   0          22h
kube-system   kube-proxy-t4q89                       1/1       Running   0          22h
kube-system   kube-proxy-w4hg6                       1/1       Running   0          22h
kube-system   kube-scheduler-ariel                   1/1       Running   0          22h
kube-system   kubernetes-dashboard-747c4f7cf-ftmpz   1/1       Running   0          20m
```

#### Join nodes

```bash
$ kubeadm join --token b679e7.a576312edad23ac1 10.100.18.10:6443 --discovery-token-ca-cert-hash sha256:add33852c5583e87c9a4224b47d8e5f6e62fcaa65e7f5cd36d39dd7b8c28cfab
[kubeadm] WARNING: kubeadm is in beta, please do not use it for production clusters.
[preflight] Running pre-flight checks
[discovery] Trying to connect to API Server "10.100.18.10:6443"
[discovery] Created cluster-info discovery client, requesting info from "https://10.100.18.10:6443"
[discovery] Requesting info from "https://10.100.18.10:6443" again to validate TLS against the pinned public key
[discovery] Cluster info signature and contents are valid and TLS certificate validates against pinned roots, will use API Server "10.100.18.10:6443"
[discovery] Successfully established connection with API Server "10.100.18.10:6443"
[bootstrap] Detected server version: v1.8.4
[bootstrap] The server supports the Certificates API (certificates.k8s.io/v1beta1)

Node join complete:
* Certificate signing request sent to master and response
  received.
* Kubelet informed of new secure connection details.

Run 'kubectl get nodes' on the master to see this machine join.
```
