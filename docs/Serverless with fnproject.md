# Serverless with fnproject.io
The Fn project is a container native serverless platform that you can run anywhere -- any cloud or on-premise. It’s easy to use, supports every programming language, and is extensible and performant.

Alternatives: http://fission.io


### Install docker

### Install fn client

![user input](images/terminal64.png)

```bash
$ curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
```

### Deploying first example

![user input](images/terminal64.png)

```bash
$ mkdir first-example
```

```bash
$ fn init --runtime java
        ______
       / ____/___
      / /_  / __ \
     / __/ / / / /
    /_/   /_/ /_/

Runtime: java
Function boilerplate generated.
func.yaml created.
```

```bash
$ fn run
Building image first-example:0.0.1
Sending build context to Docker daemon  13.82kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-latest as build-stage
 ---> 4f9ffea5cc37
Step 2/11 : WORKDIR /function
 ---> Using cache
 ---> f7f40fd849b6
Step 3/11 : ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
 ---> Using cache
 ---> 1381ac584c6e
Step 4/11 : ADD pom.xml /function/pom.xml
 ---> Using cache
 ---> 17bd932f0bb3
Step 5/11 : RUN mvn package dependency:copy-dependencies -DincludeScope=runtime -DskipTests=true -Dmdep.prependGroupId=true -DoutputDirectory=target --fail-never
 ---> Using cache
 ---> a03c29dd9a2a
Step 6/11 : ADD src /function/src
 ---> Using cache
 ---> 3da6167d03a9
Step 7/11 : RUN mvn package
 ---> Using cache
 ---> d9764b7225a7
Step 8/11 : FROM fnproject/fn-java-fdk:jdk9-latest
 ---> d2027594962d
Step 9/11 : WORKDIR /function
 ---> Using cache
 ---> bb9f694c064d
Step 10/11 : COPY --from=build-stage /function/target/*.jar /function/app/
 ---> Using cache
 ---> 9e501c5a20b6
Step 11/11 : CMD com.example.fn.HelloFunction::handleRequest
 ---> Using cache
 ---> 16640cbbab27
Successfully built 16640cbbab27
Successfully tagged first-example:0.0.1
Hello, world!
```

```bash
$ docker images
REPOSITORY                    TAG                 IMAGE ID            CREATED             SIZE
first-example                 0.0.1               16640cbbab27        27 minutes ago      393MB
fnproject/fn-java-fdk         jdk9-latest         d2027594962d        21 hours ago        393MB
fnproject/fn-java-fdk-build   jdk9-latest         4f9ffea5cc37        21 hours ago        408MB
```

```bash
$ fn start
mount: permission denied (are you root?)
Could not mount /sys/kernel/security.
AppArmor detection and --privileged mode might break.
mount: permission denied (are you root?)
time="2017-12-01T13:21:58Z" level=info msg="datastore dialed" datastore=sqlite3 max_idle_connections=256
time="2017-12-01T13:21:58Z" level=info msg="started tracer" url=
time="2017-12-01T13:21:58Z" level=info msg="no docker auths from config files found (this is fine)" error="open /root/.dockercfg: no such file or directory"
time="2017-12-01T13:21:58Z" level=info msg="available memory" ram=7807500288
time="2017-12-01T13:21:58Z" level=info msg="Serving Functions API on address `:8080`"

        ______
       / ____/___
      / /_  / __ \
     / __/ / / / /
    /_/   /_/ /_/
        v0.3.209
        
$ fn apps list
```

```bash
$ fn deploy --app hello-java --local

Deploying first-example to app: hello-java at path: /first-example
Bumped to version 0.0.2
Building image first-example:0.0.2
Sending build context to Docker daemon  93.18kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-latest as build-stage
 ---> 4f9ffea5cc37
Step 2/11 : WORKDIR /function
 ---> d4dfee1376e9
Removing intermediate container e4bbd656d65b
Step 3/11 : ENV MAVEN_OPTS -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort= -Dhttp.nonProxyHosts= -Dmaven.repo.local=/usr/share/maven/ref/repository
 ---> Running in 260b6784f354
 ---> b9e6bf4d2f34
Removing intermediate container 260b6784f354
Step 4/11 : ADD pom.xml /function/pom.xml

...

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.805 s
[INFO] Finished at: 2017-12-01T13:34:53Z
[INFO] Final Memory: 14M/47M
[INFO] ------------------------------------------------------------------------
 ---> 7d2ac91469a6
Removing intermediate container c12758d3baee
Step 8/11 : FROM fnproject/fn-java-fdk:jdk9-latest
 ---> d2027594962d
Step 9/11 : WORKDIR /function
 ---> Using cache
 ---> bb9f694c064d
Step 10/11 : COPY --from=build-stage /function/target/*.jar /function/app/
 ---> 507d5dc19e8c
Step 11/11 : CMD com.example.fn.HelloFunction::handleRequest
 ---> Running in 329a1ff503ac
 ---> 204666855e71
Removing intermediate container 329a1ff503ac
Successfully built 204666855e71
Successfully tagged first-example:0.0.2
Updating route /first-example using image first-example:0.0.2...
```

#### Start FN dashboard

![user input](images/terminal64.png)

```bash
$ docker run --rm -it --link functions:api -p 4000:4000 -e "FN_API_URL=http://api:8080" fnproject/ui
```

#### Calling your function

**Local**

![user input](images/terminal64.png)

```bash
echo -n Anderson | fn run
Hello, Anderson!
```

**Remote**

![user input](images/terminal64.png)

```bash
$ echo -n Anderson | fn call hello-java first-example
Hello, Anderson!

$ curl -X POST -d 'Anderson' http://localhost:8080/r/hello-java/first-example
Hello, Anderson!
```

Open your browser at http://localhost:4000 and go to your app (hello-java) and run your function.

## Create an app

Fn supports grouping functions into a set that defines an application (or API), making it easy to organize and deploy.

This part is easy, just create an `app.yaml` file and put a name in it:

![user input](images/terminal64.png)

```sh
$ mkdir myapp2
$ cd myapp2
$ echo 'name: myapp2' > app.yaml
```

This directory will be the root of your application.

### Create a root function

The root function will be available at `/` on your application.

![user input](images/terminal64.png)

```sh
$ fn init --runtime ruby
```

Now we have a Ruby function alongside our `app.yaml`.

### Create a sub route

Now let's create a sub route at `/hello`:

![user input](images/terminal64.png)

```sh
$ fn init --runtime go hello
```

Now we have two functions in our app. Run:

![user input](images/terminal64.png)

```sh
$ ls
```

To see our root function, our `app.yaml` and a directory named `hello`.

### Deploy the entire app

Now we can deploy the entire application with one command:

![user input](images/terminal64.png)

```sh
$ fn deploy --all --local
```

Once the command is done, let's surf to our application:

* Root function at: http://localhost:8080/r/myapp2/
* And the hello function at: http://localhost:8080/r/myapp2/hello

### Wrapping Up

Congratulations! In this tutorial you learned how to group functions into an application and deploy them
with a single command.


## More examples

![user input](images/terminal64.png)

```bash
$ git clone https://github.com/asantos2000/serverless.git fn-examples
```

## References
1. [Fn Project home](https://fnproject.io/)
1. [Github - The container native, cloud agnostic serverless platform](https://github.com/fnproject/fn)
1. [Java API and runtime for fn](https://github.com/fnproject/fdk-java)
1. [CLI tool for fnproject](https://github.com/fnproject/cli)
1. [Serverless Architectures - Let's Ditch the Servers?](https://codeahoy.com/2016/06/25/serverless-architectures-lets-ditch-the-servers/)
1. [Database Connections in Lambda](http://blog.rowanudell.com/database-connections-in-lambda/)
1. [Best practices for Serverless: Connection Pooling your database](http://blog.spotinst.com/2017/11/19/best-practices-serverless-connection-pooling-database/)
1. [Top 10 JDBC Best Practices for Java Programmer](http://javarevisited.blogspot.com.br/2012/08/top-10-jdbc-best-practices-for-java.html)
1. [Best practices in Coding, Designing and Architecting Java Applications](https://github.com/in28minutes/java-best-practices#data-layer)
1. [Install Docker](https://docs.docker.com/engine/installation/)
 