# jenkins-config
Jenkins config as code

### How to build and run

```
docker build -t myjenkins:latest .
mkdir jdata
docker run -p 8080:8080 -p 50000:50000 -v jdata:/var/jenkins_home myjenkins
```
