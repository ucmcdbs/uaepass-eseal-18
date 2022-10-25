# UAE Pass e-Seal Wrapper Service

Wrap e-Seal SOAP API and expose via ReST endpoint.

## Develop

```bash
source .env
mvn package
java -jar target/eseal-{version}.jar
```

## Test

```bash
source .env
curl -v -XPOST -F document=@src/main/resources/assets/document.pdf http://localhost:8080 -o /home/geordee/temp/sealed.pdf
```
