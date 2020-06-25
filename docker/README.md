# To build Docker image

```bash
cp ../bin/cgview.jar .
cp ../scripts/cgview_xml_builder/cgview_xml_builder.pl .
IMAGE=pstothard/cgview
VERSION=$(java -jar cgview.jar --version 2>&1 >/dev/null | awk '{print $2}')
docker build -t ${IMAGE}:${VERSION} .
docker tag ${IMAGE}:${VERSION} ${IMAGE}:latest
```