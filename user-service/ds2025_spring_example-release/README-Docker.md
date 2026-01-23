#Docker Deployment for Demo application

## Docker Configuration Files

Two Docker configuration files are required for deployment:

1. [`demo/Dockerfile`](Dockerfile) – specifies how to build the application image  
   - Defines the base image, dependencies, src files, and the startup command

2. [`docker-compose.yml`](../../docker-compose.yml) – defines services (containers), environment variables, ports, volumes, and networks  
   - References the Docker image either by **name** (pre-built image) or by specifying the **Dockerfile** to build it automatically


## Commands
Create Docker network

```bash
docker network create demo_net --driver bridge
```


Open a terminal **in the root folder**, then run:

1. (Optional) Build the Docker Image


```bash
docker build -f demo/Dockerfile -t demo-image .
```

  

2. Create and Start Docker Containers

```bash
docker-compose up 
```
- Builds the image (if it doesn’t exist) 
- Starts all containers defined in **docker-compose.yml** file
- Logs are streamed directly to terminal

To start the containers in the background ("deatached" mode) use -d option:
```bash
docker-compose up -d 
```

To enforce the build step for any service that has a build section even if an image already exists use --build option:
```bash
docker-compose up -d --build 
```

2. Check Docker Images and Containers

- list images
```bash 
docker image ls
```
- list running containers
```bash 
docker image ps
```
- see logs for spring-demo running container 
```bash 
docker logs spring-demo
```

2. Stop Docker Containers
```bash 
docker-compose down
```


3. Remove stopped Docker Containers
```bash 
docker rm spring-demo
```


3. Remove unused image
```bash 
docker rmi demo-image
```